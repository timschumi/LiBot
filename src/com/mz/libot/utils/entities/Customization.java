package com.mz.libot.utils.entities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Customization {

	@Nonnull
	private List<Integer> disabledCommands;
	@Nullable
	private String commandPrefix;
	@Nullable
	private Long djRoleId;

	// TODO document this
	public Customization() {
		this.disabledCommands = new ArrayList<>();
	}

	public Customization(@Nullable Customization customization) {
		this();
		if (customization != null) {
			this.disabledCommands.addAll(customization.disabledCommands);
			this.commandPrefix = customization.commandPrefix;
			this.djRoleId = customization.djRoleId;
		}
	}

	/**
	 * Flags a command as disabled
	 *
	 * @param command
	 *            command to flag as disabled
	 *
	 * @return true if the command was flagged, false if it's already flagged as disabled
	 */
	public boolean disable(@Nonnull Command command) {
		if (isDisabled(command)) {
			return false;

		}
		this.disabledCommands.add(command.getId());
		return true;
	}

	/**
	 * Flags a command as enabled
	 *
	 * @param command
	 *            command to flag as enabled
	 *
	 * @return true if the command was flagged, false if it's already flagged as enabled
	 */
	public boolean enable(@Nonnull Command command) {
		if (!isDisabled(command)) {
			return false;

		}
		this.disabledCommands.remove((Integer) command.getId());
		return true;
	}

	/**
	 * Checks if a command is flagged as disabled
	 *
	 * @param command
	 *            command to check
	 *
	 * @return true if this command is flagged as disabled, false if this command is
	 *         flagged as enabled
	 */
	public boolean isDisabled(@Nonnull Command command) {
		return this.disabledCommands.contains(command.getId());
	}

	/**
	 * Returns the preferred command prefix or default command prefix if none was
	 * explicitly set
	 *
	 * @return command prefix
	 */
	@Nonnull
	public String getCommandPrefix() {
		String configuredCommandPrefix = this.commandPrefix;
		if (configuredCommandPrefix == null)
			return Constants.DEFAULT_COMMAND_PREFIX;
		return configuredCommandPrefix;
	}

	@Nonnull
	public Customization setCommandPrefix(String commandPrefix) {
		this.commandPrefix = commandPrefix;

		return this;
	}

	/**
	 * @return ID of the "DJ role"
	 */
	@Nullable
	public Long getDjRoleId() {
		return this.djRoleId;
	}

	/**
	 * @param member
	 *            member to check
	 *
	 * @return whether that member is DJ and can play music
	 */
	public boolean isDj(Member member) {
		if (this.getDjRoleId() == null)
			return true;
		// If there's no DJ role

		if (member.hasPermission(Permission.ADMINISTRATOR))
			return true;
		// If the member is server administrator or owner

		return member.getRoles().stream().anyMatch(r -> r.getIdLong() == this.getDjRoleId());
	}

	/**
	 * Sets the "DJ-role"
	 *
	 * @param djRole
	 *            new DJ role
	 *
	 * @return self, used for chaining
	 */
	public Customization setDjRole(@Nullable Role djRole) {
		if (djRole == null) {
			this.djRoleId = null;
		} else {
			this.djRoleId = djRole.getIdLong();
		}

		return this;
	}

}
