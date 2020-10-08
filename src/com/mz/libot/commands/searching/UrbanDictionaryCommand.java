package com.mz.libot.commands.searching;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mz.libot.core.BotData;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.utils.HttpUtils;
import com.mz.utils.entities.HttpEasyResponse;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class UrbanDictionaryCommand extends Command {

	public static final String API_URL = "http://api.urbandictionary.com/v0/define?term=";

	public static String crop(String text, int maxLength) {
		if (text.length() > maxLength)
			return text.substring(0, maxLength - 4) + "...";
		return text;
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		JSONObject json = null;
		String term = params.get(0);
		try {
			HttpEasyResponse resp = HttpUtils.sendGet(API_URL + URLEncoder.encode(term, "UTF-8"), BotData.getName());

			if (resp.getResponseCode() != 200)
				throw new IOException();

			json = new JSONObject(resp.getResponseBody());
		} catch (IOException e) {
			throw new CommandException("Could not access UrbanDictionary!", Constants.FAILURE, false);
		}

		JSONArray definitions = json.getJSONArray("list");
		if (definitions.length() == 0)
			throw new CommandException("Looks like UrbanDictionary can't define that term.", Constants.DISABLED, false);

		JSONObject definition = definitions.getJSONObject(0);

		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor("A definition by " + definition.getString("author"));
		builder.setTitle("The definition of \"" + definition.getString("word") + "\"",
		    definition.getString("permalink"));

		builder
		    .setDescription("_\"" + crop(definition.getString("definition"), MessageEmbed.TEXT_MAX_LENGTH - 4) + "\"_");

		String example = crop(definition.getString("example"), MessageEmbed.VALUE_MAX_LENGTH);
		if (example.length() > 0)
			builder.addField("Usage example", example, true);

		builder.addField("Votes",
		    "\uD83D\uDC4D\uD83C\uDFFC"
		        + definition.getInt("thumbs_up")
		        + "\n\uD83D\uDC4E\uD83C\uDFFC "
		        + definition.getInt("thumbs_down"),
		    true);

		builder.setColor(Constants.LITHIUM);

		event.getChannel().sendMessage(builder.build()).queue();
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("word or expression to define");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.SEARCHING;
	}

	@Override
	public String getInfo() {
		return "Lets you browse definitions from the Urban Dictionary.";
	}

	@Override
	public String getName() {
		return "UrbanDictionary";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("urban", "ud");
	}

}
