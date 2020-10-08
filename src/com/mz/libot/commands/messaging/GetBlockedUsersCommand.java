package com.mz.libot.commands.messaging;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GetBlockedUsersCommand extends Command {

	private static ArrayList<String> getBlocked(String userId) throws IOException {
		String blockedJson = BotData.getProperties().getProperty(Utils.BLOCKED_KEY, "{}");
		Gson gson = new Gson();
		Type type = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();

		HashMap<String, ArrayList<String>> allBlocked = gson.fromJson(blockedJson, type);

		ArrayList<String> blocked = null;
		if (allBlocked.containsKey(userId)) {
			blocked = allBlocked.get(userId);

		} else {
			blocked = new ArrayList<>();

		}

		return blocked;
	}

	public static void getBlocked(User author, MessageChannel channel) throws IOException {
		StringBuilder sb = new StringBuilder();

		ArrayList<String> blocked = getBlocked(author.getId());
		if (blocked.isEmpty()) {
			sb.append("_(you haven't blocked anyone)_");
		} else {
			for (String blockedId : blocked) {
				User user = BotData.getJDA().getUserById(blockedId);
				if (user == null)
					user = Utils.getUnknownUser(blockedId);

				sb.append(user.getName() + "#" + user.getDiscriminator() + " (" + user.getId() + ")\n");
			}
		}

		channel.sendMessage(BotUtils.buildEmbed("List of blocked users", sb.toString().trim(), Constants.LITHIUM))
		    .queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		getBlocked(author, channel);
	}

	@Override
	public String getInfo() {
		return "Lists all your blocked users (blocked in LiBot's mailing service).";
	}

	@Override
	public String getName() {
		return "GetBlocked";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MESSAGING;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("getblocked", "blocks");
	}

}
