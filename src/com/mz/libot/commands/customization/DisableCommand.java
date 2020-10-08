package com.mz.libot.commands.customization;

import java.util.List;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.utils.entities.Customization;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class DisableCommand extends Command {

	public static void disable(Parameters params, Guild guild, MessageChannel channel) {
		Command cmd = LiBotCore.commands.get(params.get(0));
		CommandCategory category = CommandCategory.getCategory(params.get(0));
		if (cmd != null) {
			Customization cc = ProviderManager.CUSTOMIZATIONS.getCustomization(guild);

			if (cmd.getClass().equals(EnableCommand.class))
				throw new CommandException(cmd.getName() + " can't be disabled!", false);

			if (!cc.disable(cmd))
				throw new CommandException("That command is already disabled!", false);

			channel
			    .sendMessage(
			        BotUtils.buildEmbed("Success", cmd.getName() + " was successfully disabled!", Constants.SUCCESS))
			    .queue();

		} else if (category != null) {
			Customization cc = ProviderManager.CUSTOMIZATIONS.getCustomization(guild);
			List<Command> ofCategory = LiBotCore.commands.getAll(category);
			int i = 0;
			StringBuilder output = new StringBuilder();
			for (Command command : ofCategory) {
				if (command.getClass().equals(EnableCommand.class)) {
					output.append("\n**" + command.getName() + " can't be disabled**");
					continue;
				}

				if (cc.disable(command)) {
					output.append("\nDisabled " + command.getName() + "");
					i++;

				} else {
					output.append("\n**" + command.getName() + " is already disabled**");
				}
			}

			channel
			    .sendMessage(BotUtils.buildEmbed("Successfully disabled " + i + " commands", output.toString(),
			        Constants.SUCCESS))
			    .queue();

		} else {
			throw new CommandException("Command/category \"" + params.get(0) + "\" does not exist!", false);
		}

	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		disable(params, guild, channel);
	}

	@Override
	public String getInfo() {
		return "Disables a command or a category for the current guild. Disabled commands can not be used until re-enabled with `@"
		    + BotData.getName()
		    + " enable <command>`.";
	}

	@Override
	public String getName() {
		return "DisableCommand";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.CUSTOMIZATION;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.MANAGE_SERVER);
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("command or category");
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("disable");
	}

}
