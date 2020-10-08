package com.mz.libot.commands.administrative;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ShutdownCommand extends Command {

	public static void shutdown(User author, MessageChannel channel, Guild guild) {
		EventWaiter ew = new EventWaiter(author, channel);
		if (ew.getBoolean(
		    "Are you sure you want to shut down LiBot? This action requires complete reboot, for temporary shutdown, use "
		        + BotUtils.getCommandPrefixEscaped(guild)
		        + "maintenance.")) {
			channel.sendMessage("Shutting down..").complete();
			LiBotCore.interrupt();
		}
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();
		Guild guild = event.getGuild();

		shutdown(author, channel, guild);
	}

	@Override
	public String getInfo() {
		return "Shuts down LiBot. This will require rebooting from online console"
		    + " as it does not shut down the web service.";
	}

	@Override
	public String getName() {
		return "Shutdown";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.ADMINISTRATIVE;
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.isOwner(event);
	}

}
