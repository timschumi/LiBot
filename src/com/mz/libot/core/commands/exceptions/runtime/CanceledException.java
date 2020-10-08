package com.mz.libot.core.commands.exceptions.runtime;

import com.mz.libot.core.commands.exceptions.CommandException;

public class CanceledException extends CommandException {

	private static final long serialVersionUID = 3755262076113234079L;

	public CanceledException() {
		super(false);
	}
}
