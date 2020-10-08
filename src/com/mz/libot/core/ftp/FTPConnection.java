package com.mz.libot.core.ftp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPConnection {

	private static final Logger LOG = LoggerFactory.getLogger(FTPConnection.class);
	@Nonnull
	private final FTPClient ftp;

	/**
	 * Creates a new FTP connection.
	 *
	 * @param host
	 *            target host
	 * @param port
	 *            target server's FTP port
	 * @param username
	 *            target user's username
	 * @param password
	 *            user's password
	 *
	 * @throws IOException
	 *             if something failed
	 */
	public FTPConnection(@Nonnull String host, int port, @Nonnull String username,
	                     @Nonnull String password) throws IOException {
		this.ftp = new FTPClient();
		this.ftp.connect(host, port);
		int reply = this.ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			this.ftp.disconnect();
			throw new IOException("Error connecting to the FTP Server");
		}

		if (!this.ftp.login(username, password))
			throw new IOException("Authentication failed");

		this.ftp.setFileType(FTP.BINARY_FILE_TYPE);
		this.ftp.enterLocalPassiveMode();
	}

	/**
	 * Uploads a file to the FTP server.
	 *
	 * @param file
	 *            the local file to upload
	 * @param remotePath
	 *            path to the remote file
	 *
	 * @throws IOException
	 *             if something failed
	 */
	public void upload(@Nonnull File file, @Nonnull String remotePath) throws IOException {
		try (InputStream is = new FileInputStream(file)) {
			this.ftp.storeFile(remotePath, is);
		}
	}

	/**
	 * Uploads a byte array to the FTP server.
	 *
	 * @param bytes
	 *            bytes to upload
	 * @param remotePath
	 *            path to the remote file
	 *
	 * @throws IOException
	 *             if something failed
	 */
	public void upload(@Nonnull byte[] bytes, @Nonnull String remotePath) throws IOException {
		try (InputStream is = new ByteArrayInputStream(bytes)) {
			this.ftp.storeFile(remotePath, is);
		}
	}

	/**
	 * Downloads a remote file from the FTP server.
	 *
	 * @param remotePath
	 *            path to the file on remote server
	 * @param destination
	 *            path to store file into
	 *
	 * @throws IOException
	 *             if something failed
	 */
	public void download(@Nonnull String remotePath, @Nonnull File destination) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(destination)) {
			this.ftp.retrieveFile(remotePath, fos);
		}
	}

	/**
	 * Reads a remote file into a byte array.
	 *
	 * @param remotePath
	 *            path to the remote file
	 *
	 * @return byte array
	 *
	 * @throws IOException
	 *             if something failed
	 */
	@SuppressWarnings("null")
	@Nonnull
	public byte[] readFile(@Nonnull String remotePath) throws IOException {
		try (InputStream retrieveFileStream = this.ftp.retrieveFileStream(remotePath)) {
			return IOUtils.toByteArray(retrieveFileStream);
		}
	}

	/**
	 * Lists all files and directories inside a remote directory.
	 *
	 * @param directory
	 *            path to directory to list
	 *
	 * @return array of objects inside the directory
	 *
	 * @throws IOException
	 *             if something failed
	 */
	@SuppressWarnings("null")
	@Nonnull
	public FTPFile[] listFiles(@Nonnull String directory) throws IOException {
		return this.ftp.listFiles(directory);
	}

	/**
	 * Disconnects from the FTP server
	 */
	public void disconnect() {
		if (this.ftp.isConnected()) {
			try {
				this.ftp.logout();
				this.ftp.disconnect();
			} catch (IOException e) {
				LOG.error("Failed to disconnect from the server", e);
			}
		}
	}
}
