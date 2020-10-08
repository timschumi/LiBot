package com.mz.libot.core.data.providers.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.data.providers.Provider;
import com.mz.libot.core.data.providers.impl.ConfigurationProvider.BotConfiguration;

public class ConfigurationProvider extends Provider<BotConfiguration> {

	public static class BotConfiguration {

		private final List<Integer> disabledCommands;
		private boolean maintenance;

		public BotConfiguration() {
			this(new ArrayList<>(), false);
		}

		public BotConfiguration(List<Integer> disabledCommands, boolean maintenance) {
			this.disabledCommands = Collections.synchronizedList(disabledCommands);
			this.maintenance = maintenance;
		}

		public List<Integer> getDisabledCommands() {
			return this.disabledCommands;
		}

		public boolean isMaintenance() {
			return this.maintenance;
		}

		public void setMaintenance(boolean maintenance) {
			this.maintenance = maintenance;
		}

	}

	/**
	 * Will throw an {@link UnsupportedOperationException}.
	 */
	@Override
	public BotConfiguration getData() {
		throw new UnsupportedOperationException("Direct access to this provider's data is not permitted.");
	}

	/**
	 * Checks if the command was disabled using moduleDisable command.
	 *
	 * @param command
	 *            command to test
	 * @return true if command is disabled, false if it isn't
	 */
	public boolean isDisabled(Command command) {
		return this.data.getDisabledCommands().contains(command.getId());
	}

	/**
	 * Adds a command to the list of disabled commands.
	 *
	 * @param command
	 *            command to disable
	 * @return true if command was disabled, false if not (is already disabled)
	 */
	public boolean disable(Command command) {
		if (isDisabled(command))
			return false;

		this.data.getDisabledCommands().add(command.getId());

		store(BotData.getProperties());
		return true;
	}

	/**
	 * Removes a command from the list of disabled commands (enables it).
	 *
	 * @param command
	 *            command to enable
	 * @return true if command was enabled, false if not (is not disabled)
	 */
	public boolean enable(final Command command) {
		if (!isDisabled(command))
			return false;

		this.data.getDisabledCommands().remove((Integer) command.getId());

		store(BotData.getProperties());
		return true;
	}

	/**
	 * Sets LiBot's maintenance state.
	 *
	 * @param maintenanceState
	 */
	public void setMaintenance(boolean maintenanceState) {
		this.data.maintenance = maintenanceState;

		store(BotData.getProperties());
	}

	/**
	 * @return bot's current maintenance state
	 */
	public boolean isMaintenance() {
		return this.data.maintenance;
	}

	@Override
	public String getDataKey() {
		return "configuration";
	}

	@Override
	protected BotConfiguration getDefaultData() {
		return new BotConfiguration();
	}

	@Override
	public TypeToken<BotConfiguration> getTypeToken() {
		return new TypeToken<>() {};
	}

}
