package com.mz.libot.core.data.providers;

import com.mz.libot.core.data.properties.PropertyManager;
import com.mz.libot.core.data.providers.impl.AutoRoleProvider;
import com.mz.libot.core.data.providers.impl.ConfigurationProvider;
import com.mz.libot.core.data.providers.impl.CustomizationsProvider;
import com.mz.libot.core.data.providers.impl.MoneyProviders.AdditionalMoneyProvider;
import com.mz.libot.core.data.providers.impl.MoneyProviders.MoneyProvider;
import com.mz.libot.core.data.providers.impl.PollProvider;
import com.mz.libot.core.data.providers.impl.TimerProvider;
import com.mz.libot.core.data.providers.impl.WGMOProvider;

import net.dv8tion.jda.api.JDA;

public final class ProviderManager {

	public static final AutoRoleProvider AUTO_ROLE = new AutoRoleProvider();
	public static final ConfigurationProvider CONFIGURATION = new ConfigurationProvider();
	public static final CustomizationsProvider CUSTOMIZATIONS = new CustomizationsProvider();
	public static final PollProvider POLL = new PollProvider();
	public static final MoneyProvider MONEY = new MoneyProvider();
	public static final AdditionalMoneyProvider ADDITIONAL_MONEY = new AdditionalMoneyProvider();
	public static final WGMOProvider WGMO = new WGMOProvider();
	public static final TimerProvider TIMERS = new TimerProvider();

	private ProviderManager() {}

	/**
	 * Loads data from a {@link PropertyManager} into all providers.
	 *
	 * @param pm
	 */
	public static void loadAll(PropertyManager pm) {
		AUTO_ROLE.load(pm);
		CONFIGURATION.load(pm);
		CUSTOMIZATIONS.load(pm);
		POLL.load(pm);
		MONEY.load(pm);
		ADDITIONAL_MONEY.load(pm);
		WGMO.load(pm);
		TIMERS.load(pm);
	}

	/**
	 * Cleans obsolete data from all available providers.
	 *
	 * @param jda
	 * @return combined number of elements removed
	 */
	public static int cleanAll(JDA jda) {
		return AUTO_ROLE.clean(jda) + CUSTOMIZATIONS.clean(jda) + POLL.clean(jda) + MONEY.clean(jda)
		    + ADDITIONAL_MONEY.clean(jda) + WGMO.clean(jda) + TIMERS.clean(jda);
	}

}
