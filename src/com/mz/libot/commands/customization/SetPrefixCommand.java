package com.mz.libot.commands.customization;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.utils.EventWaiter;
import com.mz.libot.utils.entities.Customization;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetPrefixCommand extends Command {

	public static void setPrefix(Guild guild, Parameters params, MessageChannel channel, User author) {
		EventWaiter ew = new EventWaiter(author, channel);
		if (!params.check()) {
			if (!ew.getBoolean(BotUtils.buildEmbed("Are you sure?",
			    "Are you sure you want remove custom command prefix and revert it to the default one (currently `"
			        + Constants.DEFAULT_COMMAND_PREFIX
			        + "`)?",
			    Constants.LITHIUM))) {
				throw new CommandException(false);
			}

			Customization cust = ProviderManager.CUSTOMIZATIONS.getCustomization(guild);
			cust.setCommandPrefix(null);
			ProviderManager.CUSTOMIZATIONS.setCustomization(guild, cust);

			channel
			    .sendMessage(BotUtils.buildEmbed("Success!",
			        "Custom command prefix for this guild was successfully removed!", Constants.SUCCESS))
			    .queue();
		} else {
			String prefix = params.get(0).trim();
			if (prefix.length() > 10) {
				throw new CommandException("Maximal length of a prefix is 10 characters!", false);
			}

			if (!ew.getBoolean(BotUtils.buildEmbed("Are you sure?",
			    "Are you sure you want to change LiBot's command prefix for this guild to `" + prefix + "`?",
			    Constants.LITHIUM))) {
				throw new CommandException(false);
			}

			Customization cust = ProviderManager.CUSTOMIZATIONS.getCustomization(guild);
			cust.setCommandPrefix(prefix);
			ProviderManager.CUSTOMIZATIONS.setCustomization(guild, cust);

			channel
			    .sendMessage(BotUtils.buildEmbed("Success!",
			        "LiBot's command prefix for this guild was successfully changed!", Constants.SUCCESS))
			    .queue();
		}
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();
		Guild guild = event.getGuild();

		setPrefix(guild, params, channel, author);
	}

	@Override
	public String getInfo() {
		return "Lets you change LiBot's command prefix for your guild. "
		    + "This will also affect the help message sent from your guild.";
	}

	@Override
	public String getName() {
		return "SetPrefix";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.CUSTOMIZATION;
	}

	@Override
	public boolean pausesThread() {
		return true;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.MANAGE_SERVER);
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("new prefix (optional)");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

}
