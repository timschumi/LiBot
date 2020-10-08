package com.mz.libot.commands.messaging;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;
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

public class UnblockUserCommand extends Command {

	public static void unblock(MessageChannel channel, User author, Parameters params) throws IOException {
		String targetId = params.get(0);

		if (!NumberUtils.isParsable(targetId)) {
			channel.sendMessage("ID is a number").queue();
			throw new CommandException(false);
		}

		if (!Utils.isBlocked(author.getId(), targetId)) {
			channel
			    .sendMessage(BotUtils.buildEmbed("Not blocking", "You are not blocking that user!", Constants.DISABLED))
			    .queue();
			throw new CommandException(false);
		}

		User target = BotData.getJDA().getUserById(targetId);
		if (target == null) {
			target = Utils.getUnknownUser(targetId);
		}

		String blockedJson = BotData.getProperties().getProperty(Utils.BLOCKED_KEY, "{}");
		Gson gson = new Gson();
		Type type = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();

		HashMap<String, ArrayList<String>> allBlocked = gson.fromJson(blockedJson, type);

		ArrayList<String> blocked = allBlocked.get(author.getId());

		if (blocked == null) {
			blocked = new ArrayList<>();
		}

		blocked.remove(targetId);
		allBlocked.put(author.getId(), blocked);
		BotData.getProperties().setProperty("blocks", gson.toJson(allBlocked));

		channel.sendMessage(BotUtils.buildEmbed("Unblocked",
		    "User "
		        + target.getName()
		        + "#"
		        + target.getDiscriminator()
		        + " was unblocked, they can now send you messages via LiBot!",
		    Constants.SUCCESS)).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		unblock(channel, author, params);
	}

	@Override
	public String getInfo() {
		return "Lets you unblock a blocked user.";
	}

	@Override
	public String getName() {
		return "UnblockUser";
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
		return Commands.toArray("unblock");
	}

}
