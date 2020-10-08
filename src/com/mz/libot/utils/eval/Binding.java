package com.mz.libot.utils.eval;

public class Binding {

	private String key;
	private Object value;

	/**
	 * Creates a new object representing a shortcut binding
	 * 
	 * @param key
	 * @param value
	 */
	public Binding(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public Object getValue() {
		return this.value;
	}

}
