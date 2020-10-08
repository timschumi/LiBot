package com.mz.libot.commands.utilities;

import org.apache.commons.lang3.math.NumberUtils;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.handlers.exception.ExceptionHandler;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PurgeCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		if (!NumberUtils.isParsable(params.get(0)))
			throw new CommandException("Number of messages to delete is a **number**!", false);

		int messageNumber = params.getAsInteger(0);

		if (0 > messageNumber)
			throw new CommandException("Number of messages to purge must bigger than 0", false);
		// Checks if messageNumber is too small or too big

		event.getChannel()
		    .getIterableHistory()
		    .takeAsync(messageNumber + 1)
		    .thenAccept(msgs -> event.getChannel().purgeMessages(msgs))
		    .exceptionally(e -> {
			    ExceptionHandler.HandleThrowable.handleThrowable(PurgeCommand.this, ExceptionHandler.unpackThrowable(e),
			        event);
			    return null;
		    })
		    .thenAccept(v -> event.getChannel()
		        .sendMessage(BotUtils.buildEmbed("Success!",
		            "Purged " + messageNumber + " message" + (messageNumber == 1 ? "" : "s"), Constants.LITHIUM))
		        .queue());
	}

	@Override
	public String getInfo() {
		return "Purges/deletes messages from a channel.";
	}

	@Override
	public String getName() {
		return "Purge";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.MESSAGE_MANAGE);
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("number of messages");
	}

}
