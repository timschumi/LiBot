package com.mz.libot.core.entities;

import java.lang.management.ManagementFactory;

import com.mz.libot.core.commands.Command;
import com.mz.libot.core.handlers.Handlers;
import com.mz.libot.core.handlers.command.CommandHandler.CommandListener;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Stats implements CommandListener {

	private String lastCommandName;
	private int totalLaunchedCommands;
	private final long startupTimestamp = ManagementFactory.getRuntimeMXBean().getStartTime();

	public Stats() {
		this.totalLaunchedCommands = 0;
		this.lastCommandName = "_(this is the first launched command!)_";
		Handlers.COMMAND_HANDLER.registerListener(this); // NOSONAR it's alright
	}

	public String getLastCommandName() {
		return this.lastCommandName;
	}

	public int getTotalLaunchedCommands() {
		return this.totalLaunchedCommands;
	}

	public long getStartupTimestamp() {
		return this.startupTimestamp;
	}

	@Override
	public void onCommandFinished(GuildMessageReceivedEvent event, Command command) throws Throwable {
		this.totalLaunchedCommands++;
		this.lastCommandName = command.getName();
	}

}
