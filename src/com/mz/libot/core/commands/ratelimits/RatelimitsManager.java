package com.mz.libot.core.commands.ratelimits;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mz.libot.core.commands.Command;

public class RatelimitsManager {

	// Ratelimits
	private static final Map<String, Ratelimits> RATELIMITS = new ConcurrentHashMap<>();

	/**
	 * Creates / retrieves ratelimits for a command. If ratelimits do not exist already,
	 * they will be created and configured with waiting time of 0 seconds.
	 *
	 * @param command
	 *            command to retrieve ratelimits for
	 * @return never-null ratelimits for that identifier
	 */
	public static Ratelimits getRatelimits(final Command command) {
		if (!RATELIMITS.containsKey(command.getRatelimitId())) {
			RATELIMITS.put(command.getRatelimitId(), new Ratelimits());
		}

		return RATELIMITS.get(command.getRatelimitId());
	}

	private RatelimitsManager() {}

}
