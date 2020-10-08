package com.mz.libot.commands.administrative;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.CommandList;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GlobalEnableCommand extends Command {

	public static void globalEnable(Parameters params, MessageChannel channel) {
		Command command = LiBotCore.commands.get(params.get(0));

		if (command == null)
			throw new CommandException("Command " + params.get(0) + " does not exist!", false);
		// Checks if target command exists

		if (!CommandList.isDisabled(command))
			throw new CommandException("This command is already enabled!", false);
		// Checks if target command is already enabled

		ProviderManager.CONFIGURATION.enable(command);
		channel.sendMessage("Command successfully enabled!").queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();

		globalEnable(params, channel);
	}

	@Override
	public String getInfo() {
		return "Enables a globally disabled command.";
	}

	@Override
	public String getName() {
		return "GlobalEnableCommand";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.ADMINISTRATIVE;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("command");
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.isOwner(event);
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("genable");
	}

}
