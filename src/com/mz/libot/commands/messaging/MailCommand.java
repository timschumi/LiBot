package com.mz.libot.commands.messaging;

import java.io.IOException;
import java.util.List;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.FinderUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MailCommand extends Command {

	public static void mail(MessageChannel channel, User author, Parameters params, Guild guild) throws IOException {
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

		if (target.isBot()) {
			throw new CommandException("Cannot message a bot", false);
		}

		if (Utils.isBlocked(target.getId(), author.getId())) {
			throw new CommandException("Blocked",
			    target.getName() + " has blocked you from sending them any messages via LiBot!", Constants.FAILURE,
			    false);
		}

		EventWaiter ew = new EventWaiter(author, channel);
		MessageEmbed mailEmbed = Utils.getMail(ew, channel, author, target);

		if (ew.getBoolean(
		    "Are you sure you want to send this mail to " + target.getName() + "#" + target.getDiscriminator() + "?")) {
			target.openPrivateChannel()
			    .queue(dm -> dm.sendMessage(mailEmbed)
			        .queue(
			            m -> channel.sendMessage(BotUtils.buildEmbed("Mail delivered",
			                "Message was delivered successfully", Constants.SUCCESS)).queue(),
			            t -> channel.sendMessage(BotUtils.buildEmbed("Mail delivery failure",
			                "Could not deliver that mail, "
			                    + "the user you are trying to reach to is probably blocking LiBot _(How could they!?)_!",
			                Constants.FAILURE)).queue()));
		}

	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();
		Guild guild = event.getGuild();

		mail(channel, author, params, guild);
	}

	@Override
	public String getInfo() {
		return "Lets you send a mail-like message to someone "
		    + "(they must be in at least one guild with LiBot!). No markdown supported, sorry!";
	}

	@Override
	public String getName() {
		return "Mail";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("sendmail");
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
		return Commands.toArray("user");
	}

	@Override
	public String getRatelimitId() {
		return "mail";
	}
}
