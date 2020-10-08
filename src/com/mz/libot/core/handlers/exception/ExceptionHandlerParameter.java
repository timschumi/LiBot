package com.mz.libot.core.handlers.exception;

import com.mz.libot.core.commands.Command;
import com.mz.libot.core.handlers.HandlerParameter;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ExceptionHandlerParameter extends HandlerParameter {

	private Throwable cause;
	private Command command;
	private GuildMessageReceivedEvent event;

	public ExceptionHandlerParameter(GuildMessageReceivedEvent event, Throwable cause, Command command) {
		this.event = event;
		this.cause = cause;
		this.command = command;
		this.jda = event.getJDA();
	}

	public Throwable getThrowable() {
		return this.cause;
	}

	public Command getCommand() {
		return this.command;
	}

	public GuildMessageReceivedEvent getEvent() {
		return this.event;
	}

}
