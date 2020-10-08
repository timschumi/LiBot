package com.mz.libot.core.data.properties;

import java.io.IOException;

/**
 * A class used to represent a property manager that can read/write properties.
 * Actions of each method are not restricted and depend on the implementation (for
 * example, FTPPropertyManager will also upload the whole properties file to the
 * remote FTP server when writing).
 *
 * @author Marko Zajc
 */
public interface PropertyManager {

	/**
	 * Sets a property.
	 *
	 * @param key
	 *            key of the property
	 * @param value
	 *            value of the property
	 * @throws IOException
	 */
	void setProperty(String key, String value) throws IOException;

	/**
	 * Removes a property.
	 *
	 * @param key
	 *            key of the property
	 * @throws IOException
	 */
	void removeProperty(String key) throws IOException;

	/**
	 * Retrieves a property.
	 *
	 * @param key
	 *            key of the property to retrieve
	 * @return property's value or null if that property does not exist
	 * @throws IOException
	 *             if something fails
	 */
	String getProperty(String key) throws IOException;

	/**
	 * Retrieves a property with a default (null) value.
	 *
	 * @param key
	 *            key of the property to retrieve
	 * @param defaultVal
	 *            value that will be used if key does not exist
	 * @return property's value or provided default if that property does not exist
	 * @throws IOException
	 */
	public default String getProperty(String key, String defaultVal) throws IOException {
		String property = getProperty(key);
		if (property == null)
			return defaultVal;
		return property;
	}

}
