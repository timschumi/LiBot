package com.mz.libot.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mz.libot.core.BotUtils;
import com.mz.libot.utils.entities.SearchResult;
import com.mz.utils.HttpUtils;
import com.mz.utils.entities.HttpEasyResponse;

public class GoogleSearch {

	private static final String ENCODING = "UTF-8";
	private static final Logger LOG = LoggerFactory.getLogger(GoogleSearch.class);
	private static final String[] API_KEYS = {}; // [DATA REDACTED]
	public static final Cache<SearchData, SearchResult> CACHE = CacheBuilder.newBuilder()
	    .maximumSize(API_KEYS.length * 100L)
	    .build();

	public static class SearchData {

		private SafeSearch safesearch;
		private String query;

		public SafeSearch getSafeSearch() {
			return this.safesearch;
		}

		public String getQuery() {
			return this.query;
		}

		public SearchData(String query, SafeSearch safesearch) {
			this.safesearch = safesearch;
			this.query = query;
		}

	}

	public enum SafeSearch {
		ENABLED,
		DISABLED;
	}

	public static SearchResult doSearch(String query, SafeSearch level) throws IOException {
		String ua = generateUserAgent();
		SearchData sd = new SearchData(query.toLowerCase(), level);

		try {
			return CACHE.get(sd, () -> {

				for (String key : API_KEYS) {
					try {
						SearchResult result = searchFromApi(query, ua, level, key);
						CACHE.put(sd, result);
						return result;
					} catch (IOException e) {
						continue;
					}
				}

				throw new IOException();
			});
		} catch (ExecutionException e) {
			throw new IOException("Could not access Google!");
		}

	}

	public static SearchResult searchFromApi(String query, String userAgent, SafeSearch level,
	                                         String apiKey) throws IOException {
		String searchUrl = "https://www.googleapis.com/customsearch/v1?safe="
		    + (level.equals(SafeSearch.ENABLED) ? "high" : "off")
		    + "&cx=018291224751151548851%3Ajzifriqvl1o&key="
		    + apiKey
		    + "&num=1&q="
		    + URLEncoder.encode(query, ENCODING);

		HttpEasyResponse resp = HttpUtils.sendGet(searchUrl, userAgent);
		String json = resp.getResponseBody();
		if (resp.getResponseCode() == 403) {
			JSONArray jsonErrors = new JSONObject(json).getJSONArray("items");
			StringBuilder errors = new StringBuilder("Errors occurred when requesting this link:");

			jsonErrors.forEach(e -> {
				if (e instanceof JSONObject) {
					JSONObject error = (JSONObject) e;
					errors.append("\n"
					    + error.optString("domain", "unknownDomain")
					    + "."
					    + error.optString("reason", "unknownReason")
					    + ": "
					    + error.optString("message", "No message provided."));
				}

			});

			throw new IOException(errors.toString());
		}

		try {
			JSONArray jsonResults = new JSONObject(json).getJSONArray("items");
			return jsonToSearchResult(jsonResults.getJSONObject(0));
		} catch (JSONException e) {
			return null;
		}
	}

	private static SearchResult jsonToSearchResult(JSONObject googleResult) {
		try {
			return new SearchResult(cleanString(googleResult.getString("title")),
			    URLDecoder.decode(cleanString(googleResult.getString("link")), ENCODING));
		} catch (UnsupportedEncodingException e) {
			LOG.error("Invalid encoding detected", e);
			return null;
		}
	}

	private static String cleanString(String uncleanString) {
		return StringEscapeUtils.unescapeJava(StringEscapeUtils
		    .unescapeHtml4(uncleanString.replaceAll("\\s+", " ").replaceAll("\\<.*?>", "").replaceAll("\"", "")));
	}

	private static String generateUserAgent() {
		char[] characters = new char[] {
		    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
		    'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
		};

		StringBuilder builder = new StringBuilder();
		builder.append("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:39.0) Gecko/20100101 DiscordBot/");
		for (int i = 0; i < 10; i++) {
			builder.append(characters[BotUtils.getRandom().nextInt(characters.length)]);
		}
		return builder.toString();
	}
}
