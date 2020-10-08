package com.mz.libot.commands.utilities;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ChatbotCommand extends Command {

	private static final Pattern XML_REGEX = Pattern.compile("<(.*?)((?= \\/>)|>)");
	private static final Logger LOG = LoggerFactory.getLogger(ChatbotCommand.class);
	private static final ChatterBot CHATTER_BOT;

	static {
		ChatterBot chatterBot;
		try {
			chatterBot = new ChatterBotFactory().create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
		} catch (Exception e) {
			chatterBot = null;
			LOG.error("Failed to load the chatbot", e);
		}

		CHATTER_BOT = chatterBot;
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		if (CHATTER_BOT == null)
			throw new CommandException(
			    "We're sorry, but this feature is currently not available. Please try again later!", Constants.DISABLED,
			    false);

		ChatterBotSession cbs = CHATTER_BOT.createSession();

		event.getChannel()
		    .sendMessage(BotUtils.buildEmbed("You're connected!",
		        "You can now start chatting with Chomsky. Say hi!\n"
		            + "Also, if you want to say something without the message being registered,"
		            + " say it with `>` prefix (something like `> heya`).",
		        "Type in EXIT to quit", Constants.SUCCESS))
		    .queue();

		EventWaiter ew = new EventWaiter(event.getAuthor(), event.getChannel());
		while (true) {
			Message response = ew.getMessage();
			if (response.getContentStripped().equalsIgnoreCase("exit")) {
				response.addReaction(Constants.ACCEPT_EMOJI).queue();
				break;
			}

			if (response.getContentRaw().startsWith(">"))
				continue;

			String reply = cbs.think(response.getContentStripped());

			if (reply.length() == 0) {
				event.getChannel().sendMessage("...").queue();
			} else {
				event.getChannel().sendMessage(XML_REGEX.matcher(reply).replaceAll("")).queue();
			}
		}

	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public String getInfo() {
		return "Lets you chat with Chomsky.";
	}

	@Override
	public String getName() {
		return "Chatbot";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("chat", "talk");
	}

}
