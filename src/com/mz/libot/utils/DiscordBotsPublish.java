package com.mz.libot.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.mz.utils.Counter;

public class DiscordBotsPublish {

	public static final Map<String, String> WEBSITES;
	static {
		Map<String, String> websites = new HashMap<>();

		String tokenUser = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
		websites.put("https://bots.discord.pw/", tokenUser + ""); // [DATA REDACTED]
		websites.put("https://discordbots.org/", tokenUser + ""); // [DATA REDACTED]
		// TODO remove credentials

		WEBSITES = Collections.unmodifiableMap(websites);
	}

	/**
	 * Publishes guild count to discord bot server(s)
	 *
	 * @param count
	 *            guild count
	 * @param id
	 *            bot's id
	 * @return number of failures (websites that the count could not be published to for
	 *         unknown reasons)
	 */
	public static int publishCount(int count, long id) {
		Counter errors = new Counter();

		String idString = String.valueOf(id);

		try (CloseableHttpClient client = HttpClientBuilder.create().disableCookieManagement().build()) {
			WEBSITES.entrySet().forEach(data -> {

				HttpPost post = new HttpPost(data.getKey() + "api/bots/" + idString + "/stats");
				post.addHeader("Authorization", data.getValue());
				post.addHeader("Content-Type", "application/json");

				post.setEntity(new StringEntity("{\"server_count\":" + count + "}", "UTF-8"));

				try {
					try (CloseableHttpResponse response = client.execute(post)) {

						if (response.getStatusLine().getStatusCode() != 200)
							errors.count();
					}
				} catch (IOException e) {
					errors.count();
				}
			});
		} catch (IOException e) {
			return WEBSITES.size();
		}

		return errors.getCount();
	}

	private DiscordBotsPublish() {}
}
