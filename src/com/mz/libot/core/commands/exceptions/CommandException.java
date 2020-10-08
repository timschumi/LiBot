package com.mz.libot.core.commands.exceptions;

import java.awt.Color;

import com.mz.libot.core.BotUtils;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * An exceptions used to indicate that the command failed and avoid ratelimit
 * registration
 *
 * @author Marko Zajc
 */
public class CommandException extends RuntimeException {

	private final transient Message errorMessage;
	private final boolean registerRatelimit;

	public CommandException(String title, String message, Color color, boolean registerRatelimit) {
		this.errorMessage = new MessageBuilder(BotUtils.buildEmbed(title, message, color)).build();
		this.registerRatelimit = registerRatelimit;
	}

	public CommandException(String message, Color color, boolean registerRatelimit) {
		this(null, message, color, registerRatelimit);
	}

	public CommandException(String message, boolean registerRatelimit) {
		this.errorMessage = new MessageBuilder(message).build();
		this.registerRatelimit = registerRatelimit;
	}

	public CommandException(boolean registerRatelimit) {
		this.errorMessage = null;
		this.registerRatelimit = registerRatelimit;
	}

	/**
	 * If a ratelimit should be registered upon throwing this exception.
	 *
	 * @return whether ratelimit should be registered
	 */
	public boolean doesRegisterRatelimit() {
		return this.registerRatelimit;
	}

	/**
	 * Sends the given message to a MessageChannel. If a message wasn't specified, this
	 * won't do anything.
	 *
	 * @param channel
	 */
	public void sendMessage(MessageChannel channel) {
		if (this.errorMessage != null)
			channel.sendMessage(this.errorMessage).queue();
	}
}
