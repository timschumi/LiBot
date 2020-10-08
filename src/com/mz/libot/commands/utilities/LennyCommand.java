package com.mz.libot.commands.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LennyCommand extends Command {

	public static final List<String> LENNYS;
	public static final int LENNYS_AMOUNT = 20;
	static {
		List<String> lennys = new ArrayList<>();
		try {
			try (InputStream in = new URL("https://api.lenny.today/v1/random?limit=" + LENNYS_AMOUNT).openConnection()
			    .getInputStream()) {
				new JSONArray(IOUtils.toString(in, StandardCharsets.UTF_8))
				    .forEach(lenny -> lennys.add(((JSONObject) lenny).get("face").toString()));
			}
		} catch (IOException e) {
			// No lennyz for us! Gotta use the hardcoded ones :(

			lennys.add("(⌐▀͡ ̯ʖ▀)");
			lennys.add("( T ʖ̯ T)");
			lennys.add("( ͡° ͜ʖ ͡°)");
			lennys.add("( ‾ʖ̫‾)");
			lennys.add("( ཀ ʖ̯ ཀ)");
			lennys.add("( ◔ ʖ̯ ◔ )");
			lennys.add("ლ(▀̿̿Ĺ̯̿̿▀̿ლ)");
			lennys.add("( ͡ʘ╭͜ʖ╮͡ʘ)");
			lennys.add("凸( ͡° ͜ʖ ͡°)");
			lennys.add("( ͡⚆ ͜ʖ ͡⚆)");
			lennys.add("( ͡~ ͜ʖ ͡°)");
			lennys.add("( ͡ʘ ͜ʖ ͡ʘ)");
			lennys.add("(͡ ͡° ͜ つ ͡͡°)");
			lennys.add("ᕦ( ͡͡~͜ʖ ͡° )ᕤ");
			lennys.add("( ͡◉ ͜ʖ ͡◉)");
		}

		LENNYS = Collections.unmodifiableList(lennys);
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		event.getChannel().sendMessage(LENNYS.get(BotUtils.getRandom().nextInt(LENNYS.size() - 1))).queue();
	}

	@Override
	public String getInfo() {
		return "Prints out a random lenny _(might not display correctly on mobile devices)_.";
	}

	@Override
	public String getName() {
		return "Lenny";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

}
