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

public class GlobalDisableCommand extends Command {

	public static void globalDisable(Parameters params, MessageChannel channel) {
		Command command = LiBotCore.commands.get(params.get(0));

		if (command == null) {
			throw new CommandException("Command " + params.get(0) + " does not exist!", false);
		}
		// Checks if target command exists

		if (CommandList.isDisabled(command)) {
			throw new CommandException("This command is already disabled!", false);
		}
		// Checks if target command is already disabled

		if (command.getName().equals("globalEnable")) {
			throw new CommandException("You cannot disable that.", false);
		}
		// Checks if target command is 'commandEnable'

		ProviderManager.CONFIGURATION.disable(command);
		channel.sendMessage("Command successfully disabled!").queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();

		globalDisable(params, channel);
	}

	@Override
	public String getInfo() {
		return "Globally disables a command.";
	}

	@Override
	public String getName() {
		return "GlobalDisableCommand";
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
		return Commands.toArray("gdisable");
	}

}
