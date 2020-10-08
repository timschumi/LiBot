package com.mz.libot.core.data.providers.impl;

import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.data.providers.SnowflakeProvider;
import com.mz.libot.utils.entities.Customization;

import net.dv8tion.jda.api.entities.Guild;

public class CustomizationsProvider extends SnowflakeProvider<Customization> {

	private static final Predicate<Long> FILTER = id -> BotData.getJDA().getGuildById(id.longValue()) == null;

	@Override
	public String getDataKey() {
		return "custconfig";
	}

	/**
	 * Returns customization configuration for provided guild. If there's currently no
	 * configuration for that guild, a new customization configuration will be created.
	 *
	 * @param guild
	 *            guild to get customization configuration for or null to remove that
	 *            customization configuration
	 * @return that guild's customization configuration or an empty customization
	 *         configuration if customization configuration for that guild does not exist
	 */
	public Customization getCustomization(Guild guild) {
		Customization cust = this.data.get(guild.getIdLong());
		if (cust == null) {
			cust = new Customization();
			this.data.put(guild.getIdLong(), cust);
		}

		return cust;
	}

	/**
	 * Applies a customization configuration for a guild. The new configuration is also
	 * stored into global property store.
	 *
	 * @param guild
	 *            guild to apply customization configuration for
	 * @param customization
	 *            new customization configuration
	 */
	public void setCustomization(Guild guild, Customization customization) {
		setCustomization(guild.getIdLong(), customization);
	}

	/**
	 * Applies a customization configuration for a guild. The new configuration is also
	 * stored into global property store.
	 *
	 * @param guildId
	 *            ID of the guild to apply customization configuration for
	 * @param customization
	 *            new customization configuration
	 */
	public void setCustomization(long guildId, Customization customization) {
		if (customization == null) {
			this.data.remove(guildId);
		} else {
			this.data.put(guildId, customization);
		}

		store(BotData.getProperties());
	}

	@Override
	public TypeToken<Map<Long, Customization>> getTypeToken() {
		return new TypeToken<>() {};
	}

	@Override
	protected Predicate<Long> getObsoleteFilter() {
		return FILTER;
	}

}
