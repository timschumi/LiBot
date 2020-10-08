package com.mz.libot.commands.messaging;

import java.io.IOException;
import java.util.List;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.FinderUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MessageCommand extends Command {

	public static void dm(Parameters params, MessageChannel channel, User author, Guild guild) throws IOException {
		String dmMessage = params.get(1);

		List<User> targets = FinderUtils.findUsers(params.get(0));
		if (targets.isEmpty()) {
			throw new CommandException("Nonexistent user",
			    "User '"
			        + BotUtils.escapeMarkdown(params.get(0))
			        + "' was not found. Use ``"
			        + BotUtils.getCommandPrefix(guild)
			        + "getId`` to get someone's ID!",
			    Constants.FAILURE, false);
		}

		User target = targets.get(0);

		if (target.isBot())
			throw new CommandException("Cannot message a bot", false);
		// Checks if targetis bot

		if (Utils.isBlocked(target.getId(), author.getId())) {
			throw new CommandException("Blocked",
			    target.getName() + " has blocked you from sending them any messages via LiBot!", Constants.FAILURE,
			    false);
		}

		target.openPrivateChannel()
		    .queue(
		        t -> t.sendMessage("Message from " + author.getAsTag() + " (" + author.getId() + "): " + dmMessage)
		            .queue(
		                m -> channel
		                    .sendMessage(BotUtils.buildEmbed("Message delivered",
		                        "Message has been delivered successfully", Constants.SUCCESS))
		                    .queue(),
		                e -> channel.sendMessage(BotUtils.buildEmbed("Message delivery failure",
		                    "Could not deliver that mail, "
		                        + "the user you are trying to reach to is either not accepting messages or is blocking "
		                        + BotData.getName()
		                        + "!",
		                    Constants.FAILURE)).queue()));
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();
		Guild guild = event.getGuild();

		dm(params, channel, author, guild);
	}

	@Override
	public String getInfo() {
		return "A simple way to direct-message any user that is in at least one of guilds that "
		    + BotData.getName()
		    + " is connected to.";
	}

	@Override
	public String getName() {
		return "Message";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MESSAGING;
	}

	@Override
	public int getRatelimit() {
		return 60;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("user", "message");
	}

	@Override
	public String getRatelimitId() {
		return "mail";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("dm");
	}

}
