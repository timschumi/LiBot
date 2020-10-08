package com.mz.libot.commands.customization;

import java.util.List;

import com.mz.libot.LiBotCore;
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

public class EnableCommand extends Command {

	public static void disable(Parameters params, Guild guild, MessageChannel channel) {
		Command cmd = LiBotCore.commands.get(params.get(0));
		CommandCategory category = CommandCategory.getCategory(params.get(0));
		if (cmd != null) {
			Customization cc = ProviderManager.CUSTOMIZATIONS.getCustomization(guild);

			if (!cc.enable(cmd))
				throw new CommandException("That command is already enabled!", false);

			channel
			    .sendMessage(
			        BotUtils.buildEmbed("Success", cmd.getName() + " was successfully enabled!", Constants.SUCCESS))
			    .queue();

		} else if (category != null) {
			Customization cc = ProviderManager.CUSTOMIZATIONS.getCustomization(guild);
			List<Command> ofCategory = LiBotCore.commands.getAll(category);
			int i = 0;
			StringBuilder output = new StringBuilder();
			for (Command command : ofCategory) {
				if (cc.enable(command)) {
					output.append("\nEnabled " + command.getName() + "");
					i++;
				} else {
					output.append("\n**" + command.getName() + " is already enabled**");
				}
			}

			channel
			    .sendMessage(BotUtils.buildEmbed("Successfully enabled " + i + " commands", output.toString(),
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
		return "Enables a command or a category for the current guild.";
	}

	@Override
	public String getName() {
		return "EnableCommand";
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
		return Commands.toArray("enable");
	}

}
