package com.mz.libot.core.handlers.command;

import com.mz.libot.core.commands.Command;
import com.mz.libot.core.handlers.HandlerParameter;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandHandlerParameter extends HandlerParameter {

	private Command command;
	private GuildMessageReceivedEvent event;

	public CommandHandlerParameter(Command command, GuildMessageReceivedEvent event, JDA jda) {
		this.command = command;
		this.event = event;
		this.jda = jda;
	}

	public Command getCommand() {
		return this.command;
	}

	public GuildMessageReceivedEvent getEvent() {
		return this.event;
	}

}
