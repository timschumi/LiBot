package com.mz.libot.core.data.providers.impl;

import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.data.providers.SnowflakeProvider;
import com.mz.libot.core.data.providers.impl.WGMOProvider.WelcomeGoodbyeMessageOptions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class WGMOProvider extends SnowflakeProvider<WelcomeGoodbyeMessageOptions> {
	// WGMO stands for Welcome/Goodbye Message Options

	private static final Predicate<Long> FILTER = id -> BotData.getJDA().getGuildById(id.longValue()) == null;

	public static class WelcomeGoodbyeMessageOptions {

		private String welcomeMessage = null;
		private String goodbyeMessage = null;
		private long channelId = 0;

		public WelcomeGoodbyeMessageOptions() {}

		public WelcomeGoodbyeMessageOptions(String welcomeMessage, String goodbyeMessage) {
			this.welcomeMessage = welcomeMessage;
			this.goodbyeMessage = goodbyeMessage;
		}

		public long getChannelId() {
			return this.channelId;
		}

		public void setChannel(TextChannel channel) {
			this.channelId = channel.getIdLong();
		}

		public String getWelcomeMessage() {
			return this.welcomeMessage;
		}

		public String getGoodbyeMessage() {
			return this.goodbyeMessage;
		}

		public void setWelcomeMessage(String welcomeMessage) {
			this.welcomeMessage = welcomeMessage;
		}

		public void setGoodbyeMessage(String goodbyeMessage) {
			this.goodbyeMessage = goodbyeMessage;
		}

	}

	@Override
	public TypeToken<Map<Long, WelcomeGoodbyeMessageOptions>> getTypeToken() {
		return new TypeToken<>() {};
	}

	@Override
	protected Predicate<Long> getObsoleteFilter() {
		return FILTER;
	}

	@Override
	public String getDataKey() {
		return "wgmo";
	}

	public WelcomeGoodbyeMessageOptions get(Guild guild) {
		return this.data.getOrDefault(guild.getIdLong(), new WelcomeGoodbyeMessageOptions());
	}

	public void register(Guild guild, WelcomeGoodbyeMessageOptions wgmo) {
		this.data.put(guild.getIdLong(), wgmo);

		super.store(BotData.getProperties());
	}

	public void remove(long guildId) {
		this.data.remove(guildId);

		super.store(BotData.getProperties());
	}

	public void remove(Guild guild) {
		this.remove(guild.getIdLong());
	}

	public static String parse(String message, User user, Guild guild) {
		return message.replace("{user}", user.getAsMention())
		    .replace("{usernm}", user.getName())
		    .replace("{server}", guild.getName());
	}

}
