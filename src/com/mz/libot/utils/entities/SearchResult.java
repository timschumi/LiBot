package com.mz.libot.utils.entities;

public class SearchResult {

	private String title = "";
	private String url = "";

	public SearchResult(String title, String url) {
		this.url = url;
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public String getUrl() {
		return this.url;
	}
}
