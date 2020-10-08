package com.mz.libot.main;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import com.mz.libot.LiBotCore;

public class LiBotCli {

	private static final String FALSE_STRING = "false";
	private static final String TRUE_STRING = "true";

	static {
		System.setProperty("org.slf4j.simpleLogger.showDateTime", FALSE_STRING);
		System.setProperty("org.slf4j.simpleLogger.showLogName", FALSE_STRING);
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", TRUE_STRING);
		System.setProperty("org.slf4j.simpleLogger.showThreadName", FALSE_STRING);
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
		System.setProperty("org.slf4j.simpleLogger.levelInBrackets", TRUE_STRING);
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
	}

	public static void main(String[] args) throws LoginException, IOException {
		LiBotCore.run(System.getenv());
	}

}
