package com.mz.libot.core;

import javax.annotation.Nonnull;

import com.mz.libot.core.data.properties.PropertyManager;

import net.dv8tion.jda.api.JDA;

public final class Bot {

	@Nonnull
	private static final Bot BOT = new Bot();

	@Nonnull
	private final BotInfo info;
	private JDA jda;
	private PropertyManager properties;

	private Bot() {
		this.info = new BotInfo();
	}

	@Nonnull
	public static Bot getBot() {
		return BOT;
	}

	public Bot setProperties(PropertyManager properties) {
		this.properties = properties;
		return this;
	}

	public JDA getJDA() {
		return this.jda;
	}

	public Bot setJDA(@Nonnull JDA jda) {
		this.jda = jda;
		return this;
	}

	@Nonnull
	public BotInfo getInfo() {
		return this.info;
	}

	public PropertyManager getProperties() {
		return this.properties;
	}

	public static class BotInfo {

		@Nonnull
		private String name;
		private long ownerId;

		BotInfo() {
			this.name = "Bot";
		}

		@Nonnull
		public String getName() {
			return this.name;
		}

		public long getOwnerId() {
			return this.ownerId;
		}

		/**
		 * @deprecated Use {@link Constants#VERSION}.
		 */
		@Deprecated(
		    since = "3.5.5-3",
		    forRemoval = true)
		@Nonnull
		public String getVersion() {
			return Constants.VERSION;
		}

		@Nonnull
		public BotInfo setName(@Nonnull String name) {
			this.name = name;
			return this;
		}

		@Nonnull
		public BotInfo setOwnerId(long ownerId) {
			this.ownerId = ownerId;
			return this;
		}

	}
}
