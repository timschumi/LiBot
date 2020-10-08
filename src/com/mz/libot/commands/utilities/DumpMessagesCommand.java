package com.mz.libot.commands.utilities;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class DumpMessagesCommand extends Command {

	private static final int MESSAGES_CAP = 500;

	@SuppressWarnings("null")
	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		int limit = params.getAsInteger(0);

		if (limit > MESSAGES_CAP)
			throw new CommandException("You can only dump up to **" + MESSAGES_CAP + "** messages!", false);

		StringBuilder sb = new StringBuilder();
		List<String> messages = event.getChannel().getIterableHistory().stream().limit(limit + 1L).map(msg -> {
			if (msg.getIdLong() == event.getMessage().getIdLong())
				return null;

			sb.delete(0, sb.length());
			// 'Recycles' the StringBuilder

			Member member = msg.getMember();
			if (member != null)
				sb.append((msg.getMember() == null ? msg.getAuthor().getName() : member.getEffectiveName()) + ": ");
			// Appends message's author

			if (msg.getContentDisplay().trim().length() > 0)
				sb.append(msg.getContentDisplay());
			// Appends message's content

			for (MessageEmbed embed : msg.getEmbeds()) {
				// Iterates over message's embeds

				sb.append(embed.getTitle() != null ? ("\n" + embed.getTitle()).replace("\n", "\n\t") : "");
				// Appends embed's title (if present)

				sb.append(embed.getDescription() != null ? ("\n" + embed.getDescription()).replace("\n", "\n\t") : "");
				// Appends embed's description

				for (Field field : embed.getFields()) {
					sb.append("\n");
					sb.append(field.getName() != null ? ("\n" + field.getName()).replace("\n", "\n\t\t") : "");
					// Appends field's name (if present)

					sb.append(field.getValue() != null ? ("\n" + field.getValue()).replace("\n", "\n\t\t") : "");
					// Appends field's value (if present)
				}
				// Appends all embed's fields' data

				sb.append("\n");

				Footer footer = embed.getFooter();
				sb.append(footer != null ? ("\n" + footer.getText()).replace("\n", "\n\t") : "");
				// Appends embed's footer
			}
			// Appends all embeds from the message

			sb.append(
			    msg.getAttachments().stream().map(Attachment::getFileName).collect(Collectors.joining("\n", "", "\n")));
			// Appends all attachments (their filenames)

			return sb.toString();

		}).filter(Objects::nonNull).collect(Collectors.toList());

		Collections.reverse(messages);

		event.getChannel()
		    .sendMessage("Here's a dump of " + messages.size() + " messages:")
		    .addFile(StringUtils.join(messages.toArray()).getBytes(StandardCharsets.UTF_8), "messagedump.txt")
		    .queue();
	}

	@Override
	public String getInfo() {
		return "Dumps a number of messages into a file and uploads it into chat.";
	}

	@Override
	public String getName() {
		return "DumpMessages";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public int getRatelimit() {
		return 60;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("number of messsages");
	}

}
