package com.mz.libot.commands.money;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LeaderboardCommand extends Command {

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list)
			result.put(entry.getKey(), entry.getValue());

		return result;
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		if (params.check()) {
			EventWaiter ew = new EventWaiter(event.getAuthor(), event.getChannel());
			if (ProviderManager.ADDITIONAL_MONEY.getData(event.getAuthor()).isLeaderboard()) {
				if (ew.getBoolean(
				    "Are you sure you want to opt out of leaderboards? **(if you do, your name won't be shown in the leaderboard anymore)**")) {
					ProviderManager.ADDITIONAL_MONEY.setData(event.getAuthor(),
					    ProviderManager.ADDITIONAL_MONEY.getData(event.getAuthor()).setLeaderboard(false));
					event.getMessage().addReaction(Constants.ACCEPT_EMOJI).queue();
				}

			} else {
				if (ew.getBoolean("Are you sure you want to opt into leaderboards?")) {
					ProviderManager.ADDITIONAL_MONEY.setData(event.getAuthor(),
					    ProviderManager.ADDITIONAL_MONEY.getData(event.getAuthor()).setLeaderboard(true));
					event.getMessage().addReaction(Constants.ACCEPT_EMOJI).queue();
				}
			}

			return;
		}

		Map<Long, Long> moneys = sortByValue(ProviderManager.MONEY.getData());

		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (Entry<Long, Long> money : moneys.entrySet()) {
			if (i >= 5)
				break;

			if (!ProviderManager.ADDITIONAL_MONEY.getData(money.getKey()).isLeaderboard())
				continue;

			User user = BotData.getJDA().getUserById(money.getKey());
			if (user == null)
				continue;

			sb.append(user.getName() + "#" + user.getDiscriminator() + " - **" + money.getValue() + " Ł**\n");

			i++;
		}

		event.getChannel().sendMessage(BotUtils.buildEmbed("Top 5 users", sb.toString(), Constants.LITHIUM)).queue();
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MONEY;
	}

	@Override
	public String getInfo() {
		return "Lets you see top 5 people with the most Ł. To opt out of leaderboards, use the `opt` parameter. "
		    + "To opt in again, use the `opt` parameter once more.";
	}

	@Override
	public String getName() {
		return "Leaderboard";
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("opt (optional)");
	}

}
