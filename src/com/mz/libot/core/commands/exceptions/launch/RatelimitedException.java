package com.mz.libot.core.commands.exceptions.launch;

public class RatelimitedException extends CommandLaunchException {

	private static final long serialVersionUID = 2846267061333290069L;

	private final long remaining;

	public RatelimitedException(long remaining) {
		this.remaining = remaining;
	}

	public long getRemaining() {
		return this.remaining;
	}

}
