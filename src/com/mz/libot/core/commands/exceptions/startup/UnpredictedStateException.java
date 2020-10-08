package com.mz.libot.core.commands.exceptions.startup;

import com.mz.libot.core.commands.exceptions.CommandException;

public class UnpredictedStateException extends CommandException {

	public UnpredictedStateException() {
		super(false);
	}

}