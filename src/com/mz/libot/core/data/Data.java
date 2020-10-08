package com.mz.libot.core.data;

import com.mz.libot.core.data.properties.PropertyManager;

public interface Data {

	/**
	 * Loads data of that class from the provided PropertyManager. Results may vary; one
	 * class may overwrite all of its configuration with the one from the provided
	 * PropertyManager, other may just merge it.
	 * 
	 * @param pm
	 *            PropertyManager
	 */
	void load(PropertyManager pm);

	/**
	 * Stores data of that class into the provided PropertyManager.
	 * 
	 * @param pm
	 *            PropertyManager
	 */
	void store(PropertyManager pm);
}
