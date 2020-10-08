package com.mz.libot.core.ftp;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.mz.libot.core.entities.ftp.FTPCredentials;

public class FTPManager {

	private static final int FTP_PORT = 21;
	@Nonnull
	private FTPCredentials credentials;

	/**
	 * Creates a new FTPManager using username + password credentials.
	 *
	 * @param credentials
	 *            credentials to use for FTP connections
	 */
	public FTPManager(@Nonnull FTPCredentials credentials) {
		this.credentials = credentials;
	}

	/**
	 * Retrieves current configuration path on remote FTP settings server.
	 *
	 * @return current configuration path on remote FTP settings server
	 */
	@Nonnull
	public String getRemoteConfigurationPath() {
		return this.credentials.getConfigurationPath();
	}

	/**
	 * Creates a new FTP connection to the configuration server. The connection can be
	 * used to store data permanently and globally.
	 *
	 * @return a new FTP connection to the configuration server
	 * @throws IOException
	 */
	@Nonnull
	public FTPConnection getFtpConnection() throws IOException {
		return new FTPConnection(this.credentials.getHost(), FTP_PORT, this.credentials.getUsername(),
		    this.credentials.getPassword());
	}

}
