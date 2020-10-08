package com.mz.libot.core.commands.exceptions.startup;

import com.mz.libot.core.commands.exceptions.CommandException;

public class NotOwnerException extends CommandException {

	private static final long serialVersionUID = -4147965734180445423L;

	public NotOwnerException() {
		super(false);
	}

}
