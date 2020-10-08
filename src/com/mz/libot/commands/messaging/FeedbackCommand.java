package com.mz.libot.commands.messaging;

import java.io.IOException;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class FeedbackCommand extends Command {

	public static void feedback(MessageChannel channel, User author) throws IOException {
		if (Utils.isBlocked(BotData.getOwnerId() + "", author.getId())) {
			throw new CommandException("Blocked",
			    "Bot's developer has blocked you. "
			        + "This means that you cannot message them with LiBot (no, not even with mail command).",
			    Constants.FAILURE, false);
		}

		EventWaiter ew = new EventWaiter(author, channel);
		MessageEmbed mailEmbed = Utils.getMail(ew, channel, author, BotData.getOwner());

		if (ew.getBoolean("Are you sure you want to send this form to bot's developer?")) {

			BotData.getOwner().openPrivateChannel().queue(t -> {
				t.sendMessage(mailEmbed).queue(m -> {

					channel.sendMessage(BotUtils.buildEmbed("Feedback", "Thanks for your feedback", Constants.SUCCESS))
					    .queue();

				}, e -> {

					channel.sendMessage(BotUtils.buildEmbed("Feedback",
					    "Could not deliver that mail, the target user is probably blocking LiBot!", Constants.FAILURE))
					    .queue();

				});

			});
		}
		// Sends the mail to the developer
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		feedback(channel, author);
	}

	@Override
	public String getInfo() {
		return "Lets you contact LiBot's developer and spam them with ~~memes~~ bug reports & new feature requests.";
	}

	@Override
	public String getName() {
		return "Feedback";
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
	public boolean pausesThread() {
		return true;
	}

	@Override
	public String getRatelimitId() {
		return "mail";
	}
}
