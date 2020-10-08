package com.mz.libot.commands.administrative;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.commands.exceptions.startup.NotOwnerException;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

class Utils {

	private static final NotOwnerException NOT_OWNER_EXCEPTION = new NotOwnerException();

	public static void isOwner(GuildMessageReceivedEvent event) {
		if (!BotUtils.isOwner(event.getAuthor()))
			throw NOT_OWNER_EXCEPTION;
	}

}
