package com.mz.libot.core.data.properties.impl;

import java.io.IOException;
import java.util.Properties;

import com.mz.libot.core.data.properties.PropertyManager;

/**
 * The simplest PropertyManager implementation you can imagine. This PropertyManager
 * will keep the Properties object in RAM and access it when needed.
 *
 * @author Marko Zajc
 */
public class SimplePropertyManager implements PropertyManager {

	private final Properties properties;

	public SimplePropertyManager() {
		this.properties = new Properties();
	}

	@Override
	public void setProperty(String key, String value) throws IOException {
		this.properties.setProperty(key, value);
	}

	@Override
	public void removeProperty(String key) throws IOException {
		this.properties.remove(key);
	}

	@Override
	public String getProperty(String key) throws IOException {
		return this.properties.getProperty(key);
	}

}
