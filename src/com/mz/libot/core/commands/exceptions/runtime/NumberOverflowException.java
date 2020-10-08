package com.mz.libot.core.commands.exceptions.runtime;

import com.mz.libot.core.commands.exceptions.CommandException;

public class NumberOverflowException extends CommandException {

	private static final long serialVersionUID = 1L;

	public NumberOverflowException() {
		super(false);
	}

}
