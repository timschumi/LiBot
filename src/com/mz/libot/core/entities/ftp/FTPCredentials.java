package com.mz.libot.core.entities.ftp;

import javax.annotation.Nonnull;

public class FTPCredentials {

	@Nonnull
	private String password;
	@Nonnull
	private String username;
	@Nonnull
	private String host;
	@Nonnull
	private String configPath;

	public FTPCredentials(@Nonnull String password, @Nonnull String username, @Nonnull String host,
	                      @Nonnull String configPath) {
		this.password = password;
		this.username = username;
		this.host = host;
		this.configPath = configPath;
	}

	@Nonnull
	public String getPassword() {
		return this.password;
	}

	@Nonnull
	public String getUsername() {
		return this.username;
	}

	@Nonnull
	public String getHost() {
		return this.host;
	}

	@Nonnull
	public String getConfigurationPath() {
		return this.configPath;
	}

}
