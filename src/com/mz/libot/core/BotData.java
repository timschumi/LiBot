package com.mz.libot.core;

import javax.annotation.Nonnull;

import com.mz.libot.core.data.properties.PropertyManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class BotData {

	private BotData() {}

	public static PropertyManager getProperties() {
		return Bot.getBot().getProperties();
	}

	public static JDA getJDA() {
		return Bot.getBot().getJDA();
	}

	@Nonnull
	public static String getName() {
		return Bot.getBot().getInfo().getName();
	}

	/**
	 * @deprecated Use {@link Constants#VERSION}.
	 */
	@Deprecated(
	    since = "3.5.5-3",
	    forRemoval = true)
	public static String getVersion() {
		return Bot.getBot().getInfo().getVersion();
	}

	public static long getOwnerId() {
		return Bot.getBot().getInfo().getOwnerId();
	}

	public static User getOwner() {
		return getJDA().getUserById(getOwnerId());
	}

}
