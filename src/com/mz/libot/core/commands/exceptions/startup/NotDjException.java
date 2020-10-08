package com.mz.libot.core.commands.exceptions.startup;

import com.mz.libot.core.commands.exceptions.CommandException;

import net.dv8tion.jda.api.entities.Role;

public class NotDjException extends CommandException {

	private final transient Role djRole;

	public NotDjException(Role djRole) {
		super(false);
		this.djRole = djRole;
	}

	public Role getDjRole() {
		return this.djRole;
	}

}
