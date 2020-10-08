package com.mz.libot.commands.messaging;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class BlockUserCommand extends Command {

	public static void block(MessageChannel channel, User author, Parameters params) throws IOException {
		String targetId = params.get(0);

		if (!NumberUtils.isParsable(targetId)) {
			throw new CommandException("ID is a number!", false);
		}

		if (Utils.isBlocked(author.getId(), targetId)) {
			throw new CommandException("Already blocking", "You are already blocking that user!", Constants.DISABLED,
			    false);
		}

		User target = BotData.getJDA().getUserById(targetId);
		if (target == null) {
			throw new CommandException("Nonexistent user",
			    "User with ID of " + targetId + " does not exist or is not in a guild with LiBot!", Constants.FAILURE,
			    false);
		}

		if (author.getId().equals(targetId)) {
			throw new CommandException("Paradox",
			    "Blocking yourself would probably result in a quantum paradox, so please don't do it. Why would you want to anyways?",
			    Constants.FAILURE, false);
		}

		String blockedJson = BotData.getProperties().getProperty(Utils.BLOCKED_KEY, "{}");
		Type type = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();

		HashMap<String, ArrayList<String>> allBlocked = Constants.GSON.fromJson(blockedJson, type);

		ArrayList<String> blocked = allBlocked.get(author.getId());

		if (blocked == null) {
			blocked = new ArrayList<>();
		}

		blocked.add(targetId);
		allBlocked.put(author.getId(), blocked);
		BotData.getProperties().setProperty("blocks", Constants.GSON.toJson(allBlocked));

		channel.sendMessage(BotUtils.buildEmbed("Blocked",
		    "User "
		        + target.getName()
		        + "#"
		        + target.getDiscriminator()
		        + " was blocked, you will no longer receive any messages sent from him via LiBot!",
		    Constants.SUCCESS)).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		block(channel, author, params);
	}

	@Override
	public String getInfo() {
		return "Lets you block an user from sending you messages via LiBot's direct-messaging features. "
		    + "To stress it again, this will **NOT** prevent that user from sending you messages at all, "
		    + "it will only prevent him from sending you messages via "
		    + BotData.getName()
		    + "'s messaging services (dm, mail, etc.).";
	}

	@Override
	public String getName() {
		return "Block";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MESSAGING;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("user's ID");
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("block");
	}

}
