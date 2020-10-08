package com.mz.libot.core.commands.exceptions.runtime;

import com.mz.libot.core.commands.exceptions.CommandException;

public class TimeoutException extends CommandException {

	private static final long serialVersionUID = 8108211317148665086L;

	public TimeoutException() {
		super(false);
	}

}
