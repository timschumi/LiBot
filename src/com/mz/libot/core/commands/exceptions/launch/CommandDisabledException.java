package com.mz.libot.core.commands.exceptions.launch;

public class CommandDisabledException extends CommandLaunchException {

	private static final long serialVersionUID = 6061652210335374930L;

	private final boolean global;

	public CommandDisabledException(boolean global) {
		this.global = global;
	}

	public boolean isGlobal() {
		return this.global;
	}
}
