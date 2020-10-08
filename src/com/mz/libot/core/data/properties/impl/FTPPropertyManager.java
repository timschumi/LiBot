package com.mz.libot.core.data.properties.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mz.libot.core.ftp.FTPManager;

/**
 * A slightly extended FilePropertyManager with the ability to use FTP as its
 * storage. The remote properties file will be equal to
 * {@code FTPManager#getRemoteConfigurationPath() + "/props.ini"}. <br>
 * <br>
 * Note that running 2 instances (either on the same or on different machines) of
 * this and using the same properties file may be hazardous as the local properties
 * file is being constantly uploaded while it is only downloaded on initialization of
 * this object.
 *
 * @author Marko Zajc
 */
public final class FTPPropertyManager extends FilePropertyManager {

	private static final Logger LOG = LoggerFactory.getLogger(FTPPropertyManager.class);
	private static final ThreadFactory UPLOAD_THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat("FTP uploader")
	    .setPriority(8)
	    .build();

	private volatile boolean shouldUpload = false;

	private final Runnable uploadProperties = () -> {
		while (this.shouldUpload) {
			this.shouldUpload = false;

			try {
				uploadBlocking();
			} catch (IOException e) {
				LOG.warn("Failed to async-ly upload properties to the FTP server", e);
			}
		}
	};

	private Thread uploadThread = null;

	private final FTPManager manager;
	private final String filename;

	/**
	 * Creates a new FTPPropertyManager.
	 *
	 * @param manager
	 *            FTPManager to use
	 * @param propertiesFile
	 *            local properties file to use
	 * @throws IOException
	 */
	public FTPPropertyManager(FTPManager manager, File propertiesFile) throws IOException {
		this(manager, propertiesFile, "props.ini");
	}

	/**
	 * Creates a new FTPPropertyManager.
	 *
	 * @param manager
	 *            FTPManager to use
	 * @param propertiesFile
	 *            local properties file to use
	 * @param remoteFilename
	 *            name of the remote file
	 * @throws IOException
	 */
	public FTPPropertyManager(FTPManager manager, File propertiesFile, String remoteFilename) throws IOException {
		super(propertiesFile);

		manager.getFtpConnection();
		// Tests FTP connection

		this.manager = manager;
		this.filename = remoteFilename;

		download();
	}

	@Override
	public void setProperty(String key, String value) throws IOException {
		setProperty(key, value, true);
	}

	public void setProperty(String key, String value, boolean async) throws IOException {
		super.setProperty(key, value);

		upload(async);
	}

	@Override
	public void removeProperty(String key) throws IOException {
		removeProperty(key, true);
	}

	public void removeProperty(String key, boolean async) throws IOException {
		super.removeProperty(key);

		upload(async);
	}

	public FTPManager getManager() {
		return this.manager;
	}

	/**
	 * Downloads the properties file from the FTP server and merges it with the old one.
	 *
	 * @throws IOException
	 */
	private void download() throws IOException {
		this.manager.getFtpConnection()
		    .download(this.manager.getRemoteConfigurationPath() + "/" + this.filename, this.propertiesFile);
	}

	/**
	 * Uploads the properties file to the FTP server and overwrites the on the FTP
	 * server.
	 */
	private void uploadAsync() {
		this.shouldUpload = true;

		if (this.uploadThread != null && this.uploadThread.isAlive())
			return;

		this.uploadThread = UPLOAD_THREAD_FACTORY.newThread(this.uploadProperties);
		this.uploadThread.start();
	}

	/**
	 * Uploads the properties file to the FTP server and overwrites the on the FTP
	 * server. This method blocks the thread until the file is actually uploaded.
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 *             if the properties file does not exist
	 */
	private void uploadBlocking() throws IOException {
		if (!this.propertiesFile.exists())
			throw new FileNotFoundException(
			    "Properties file was not found at " + this.propertiesFile.getAbsolutePath() + "!");

		this.manager.getFtpConnection()
		    .upload(this.propertiesFile, this.manager.getRemoteConfigurationPath() + "/" + this.filename);
	}

	/**
	 * Uploads the properties file to the FTP server and overwrites the on the FTP
	 * server.
	 *
	 * @param async
	 *            whether to upload properties asynchronously
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 *             if the properties file does not exist
	 */
	private void upload(boolean async) throws IOException {
		if (async) {
			uploadAsync();
		} else {
			uploadBlocking();
		}
	}

}
