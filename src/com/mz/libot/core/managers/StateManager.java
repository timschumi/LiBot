package com.mz.libot.core.managers;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public final class StateManager {

	public enum BotState {
		STOPPED,
		LOADING,
		RUNNING,
		MAINTENANCE;
	}

	private StateManager() {}

	public static void setState(@Nonnull BotState state, @Nonnull JDA jda) {
		switch (state) {
			case STOPPED:
				jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
				break;

			case LOADING:
				jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("--> Loading"));
				break;

			case RUNNING:
				jda.getPresence()
				    .setPresence(OnlineStatus.ONLINE,
				        Activity.playing("--> Running // @" + jda.getSelfUser().getName()));
				break;

			case MAINTENANCE:
				jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("--> Maintenance"));
				break;
		}
	}

}
