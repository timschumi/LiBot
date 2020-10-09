package com.mz.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.mz.utils.entities.HttpEasyResponse;

public class HttpUtils {

	private HttpUtils() {}

	/**
	 * Sends a blank HTTP GET request
	 *
	 * @param site
	 *            site to request
	 * @throws IOException
	 *             if website couldn't be reached
	 */
	public static void sendBlankGet(String site) throws IOException {
		URL url = new URL(site);
		try (InputStream is = url.openStream()) {/* just open the stream */}
	}

	/**
	 * Requests data from a website and returns it into a HttpEasyResponse format
	 *
	 * @param site
	 *            website to send request to
	 * @param headers
	 *            headers to use
	 * @return website's response
	 * @throws IOException
	 *             if website couldn't be accessed
	 */
	@SuppressWarnings("resource")
	public static HttpEasyResponse sendGet(String site, Map<String, String> headers) throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build();) {
			HttpGet get = new HttpGet(site);

			for (Entry<String, String> entry : headers.entrySet()) {
				get.addHeader(entry.getKey(), entry.getValue());
			}

			HttpResponse response = client.execute(get);

			return new HttpEasyResponse(response.getEntity(), response);
		}
	}

	/**
	 * Requests data from a website and returns it into a HttpEasyResponse format
	 *
	 * @param site
	 *            website to send request to
	 * @param userAgent
	 *            UserAgent to use
	 * @return website's response
	 * @throws IOException
	 *             if website couldn't be accessed
	 */
	public static HttpEasyResponse sendGet(String site, String userAgent) throws IOException {
		HashMap<String, String> headers = new HashMap<>();
		headers.put("User-Agent", userAgent);

		return sendGet(site, headers);
	}

	/**
	 * Sends a new HTTP POST request
	 *
	 * @param site
	 *            website to send request to
	 * @param headers
	 *            headers to use
	 * @param jsonParameters
	 *            JSON encoded parameters to use
	 * @return website's response
	 * @throws IOException
	 *             if website couldn't be accessed
	 */
	@SuppressWarnings("resource")
	public static HttpEasyResponse sendPost(String site, Map<String, String> headers, String jsonParameters)
			throws IOException {
		HttpClient client = HttpClientBuilder.create().build();

		HttpPost post = new HttpPost(site);

		for (Entry<String, String> entry : headers.entrySet()) {
			post.addHeader(entry.getKey(), entry.getValue());
		}

		post.setEntity(new StringEntity(jsonParameters));

		HttpResponse response = client.execute(post);

		return new HttpEasyResponse(response.getEntity(), response);
	}
}
