package com.mz.libot.commands.automod;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AutoRoleRemoveCommand extends Command {

	public static void autoRoleRemove(MessageChannel channel, Guild guild) {
		if (!ProviderManager.AUTO_ROLE.getData().containsKey(guild.getIdLong())) {
			throw new CommandException("No AutoRole", "AutoRole is not configured for your guild (yet)!",
			    Constants.DISABLED, false);
		}
		// Creates confirmiration prompt

		ProviderManager.AUTO_ROLE.remove(guild);

		channel.sendMessage(BotUtils.buildEmbed("AutoRole removed",
		    "AutoRole successfully was removed from your guild. New members will no longer be promoted to any role!",
		    Constants.SUCCESS)).queue();
		// Sends completion report
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		autoRoleRemove(channel, guild);
	}

	@Override
	public String getInfo() {
		return "Disables AutoRole feature.";
	}

	@Override
	public String getName() {
		return "AutoRoleRemove";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.AUTOMOD;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.MANAGE_ROLES);
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("removeautorole");
	}

}
