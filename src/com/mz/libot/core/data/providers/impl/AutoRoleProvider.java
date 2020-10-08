package com.mz.libot.core.data.providers.impl;

import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.data.providers.SnowflakeProvider;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class AutoRoleProvider extends SnowflakeProvider<Long> {

	private static final Predicate<Long> FILTER = id -> BotData.getJDA().getGuildById(id.longValue()) == null;

	@Override
	public String getDataKey() {
		return "autorole";
	}

	@Override
	public TypeToken<Map<Long, Long>> getTypeToken() {
		return new TypeToken<>() {};
	}

	@Override
	protected Predicate<Long> getObsoleteFilter() {
		return FILTER;
	}

	public void register(Guild guild, Role role) {
		this.data.put(guild.getIdLong(), role.getIdLong());

		super.store(BotData.getProperties());
	}

	public void remove(long guildId) {
		this.data.remove(guildId);

		super.store(BotData.getProperties());
	}

	public void remove(Guild guild) {
		this.remove(guild.getIdLong());
	}

}
