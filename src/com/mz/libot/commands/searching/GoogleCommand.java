package com.mz.libot.commands.searching;

import java.io.IOException;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.GoogleSearch;
import com.mz.libot.utils.GoogleSearch.SafeSearch;
import com.mz.libot.utils.entities.SearchResult;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GoogleCommand extends Command {

	public static void google(Parameters params, TextChannel txtChannel) {
		String query = params.get(0);

		SearchResult results;

		try {
			results = GoogleSearch.doSearch(query, txtChannel.isNSFW() ? SafeSearch.DISABLED : SafeSearch.ENABLED);
		} catch (IOException e) {
			throw new CommandException("Google", "Google is currently not accessible. Please try again later!",
			    Constants.DISABLED, false);
		}

		if (results == null) {
			txtChannel.sendMessage(BotUtils.buildEmbed("No results", "Google apparently couldn't answer your question."
			    + (!txtChannel.isNSFW()
			        ? " If you're searching for a NSFW topic, please note that results in non-nsfw channels are filtered!"
			        : ""),
			    Constants.DISABLED)).queue();
			throw new CommandException(true);
		}

		txtChannel.sendMessage(results.getTitle() + "\n" + results.getUrl()).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		TextChannel txtChannel = event.getChannel();

		google(params, txtChannel);
	}

	@Override
	public String getInfo() {
		return "Searches for the provided query on Google. "
		    + "All NSFW search results will be blocked on non-NSFW channels (using Google SafeSearch).";
	}

	@Override
	public String getName() {
		return "Google";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.SEARCHING;
	}

	@Override
	public int getRatelimit() {
		return 5;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("query");
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("g");
	}

}
