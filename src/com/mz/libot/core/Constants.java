package com.mz.libot.core;

import java.awt.Color;
import java.io.File;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.gson.Gson;

public class Constants {

	// Configuration
	@Nonnull
	public static String DEFAULT_COMMAND_PREFIX = "*";

	// Colors
	public static final Color LITHIUM = new Color(73, 140, 255);
	public static final Color SUCCESS = new Color(0, 255, 0);
	public static final Color WARN = new Color(255, 255, 0);
	public static final Color FAILURE = new Color(255, 0, 0);
	public static final Color DISABLED = new Color(198, 198, 198);

	// Status emojis
	public static final String ACCEPT_EMOJI = "\u2705";
	public static final String DENY_EMOJI = "\u274E";
	public static final String FAILURE_EMOJI = "\u274C";

	// Misc
	public static final String VERSION = "4.0.1";
	public static final File PROPERTIES_DIRECTORY = new File("config");
	public static final Gson GSON = new Gson();
	public static final Consumer<Throwable> EMPTY_FAIL_CONSUMER = e -> {};
	private static final String[] MARKDOWN = {
	    "`", "*", "_", "~"
	};

	private Constants() {}

	public static String[] getMarkdown() {
		return MARKDOWN.clone();
	}

}
