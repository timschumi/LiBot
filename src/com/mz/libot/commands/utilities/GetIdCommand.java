package com.mz.libot.commands.utilities;

import java.util.function.Predicate;

import org.apache.commons.lang3.math.NumberUtils;

import com.mz.libot.core.BotData;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GetIdCommand extends Command {

	public static void getId(MessageChannel channel, Parameters params) {
		String[] userString = params.get(0).split("#");

		if (userString.length != 2)
			throw new CommandException("Please format the first parameter like this:\n`username#4-digit discriminator`",
			    false);

		if (userString[1].length() != 4)
			throw new CommandException("Discriminator has **4** digits", false);

		if (!NumberUtils.isParsable(userString[1]))
			throw new CommandException("Discriminator is a **number**", false);

		Predicate<User> isTarget = u -> userString[0].equals(u.getName()) && userString[1].equals(u.getDiscriminator());

		User target = BotData.getJDA()
		    .getUserCache()
		    .stream()
		    .filter(isTarget)
		    .findAny()
		    .orElse(BotData.getJDA().getUsers().stream().filter(isTarget).findAny().orElse(null));

		if (target == null)
			throw new CommandException(null,
			    "User " + params.get(0) + " does not exist or is not in at least one guild" + " with LiBot!",
			    Constants.FAILURE, false);

		channel.sendMessage(new EmbedBuilder().setColor(Constants.LITHIUM)
		    .setDescription(target.getName() + "'s ID is:")
		    .setFooter(target.getId(), null)
		    .build()).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();

		getId(channel, params);
	}

	@Override
	public String getInfo() {
		return "Retrieves someone's ID for you. ID can be used for some commands like mail, "
		    + "dm, etc. By the way, this does not work for bots.";
	}

	@Override
	public String getName() {
		return "GetId";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("target's username#discriminator "
		    + "(discriminator are the 4 digits after the username on user's profile)");
	}

}
