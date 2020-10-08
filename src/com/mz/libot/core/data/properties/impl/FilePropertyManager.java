package com.mz.libot.core.data.properties.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import com.mz.libot.core.data.properties.PropertyManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A PropertyManager implementation that stores properties into a file when writing
 * and loads them when reading (also loads them when writing to them as it is
 * required to load them in order to create modifications). This implementation only
 * stores Properties into RAM when accessing the properties.
 *
 * @author Marko Zajc
 */
public class FilePropertyManager implements PropertyManager {

	protected final File propertiesFile;

	@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	public FilePropertyManager(File propertiesFile) throws IOException {
		this.propertiesFile = propertiesFile;

		Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(propertiesFile.getAbsolutePath())));
		if (!propertiesFile.createNewFile() && !propertiesFile.exists())
			throw new IOException("Can't access the properties file");
	}

	@Override
	public void setProperty(String key, String value) throws IOException {
		Properties props = new Properties();
		loadProperties(props);
		props.setProperty(key, value);
		storeProperties(props);
	}

	@Override
	public void removeProperty(String key) throws IOException {
		Properties props = new Properties();
		loadProperties(props);
		props.remove(key);
		storeProperties(props);
	}

	@Override
	public String getProperty(String key) throws IOException {
		Properties props = new Properties();
		loadProperties(props);
		return props.getProperty(key);
	}

	/**
	 * Loads properties from the declared properties file into a Properties object.
	 *
	 * @param props
	 *            properties to load to
	 *
	 * @throws FileNotFoundException
	 *             if the properties file does not exist
	 * @throws IOException
	 */
	protected void loadProperties(Properties props) throws IOException {
		try (FileReader reader = new FileReader(this.propertiesFile)) {
			props.load(reader);
		}
	}

	/**
	 * Stores properties from a Properties file into the declared properties file.
	 *
	 * @param props
	 *            properties to load from
	 *
	 * @throws IOException
	 */
	protected void storeProperties(Properties props) throws IOException {
		try (FileWriter writer = new FileWriter(this.propertiesFile)) {
			props.store(writer, null);
		}
	}
}
