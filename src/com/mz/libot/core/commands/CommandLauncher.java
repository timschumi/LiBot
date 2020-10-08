package com.mz.libot.core.commands;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.libot.core.commands.exceptions.launch.CommandDisabledException;
import com.mz.libot.core.commands.exceptions.launch.CommandLaunchException;
import com.mz.libot.core.commands.exceptions.launch.RatelimitedException;
import com.mz.libot.core.commands.ratelimits.Ratelimits;
import com.mz.libot.core.commands.ratelimits.RatelimitsManager;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.handlers.command.CommandHandler.CommandListener;
import com.mz.libot.core.processes.CommandProcess;
import com.mz.libot.core.processes.ProcessManager;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandLauncher implements CommandListener {

	public static final int MAX_COMMANDS_PER_USER = 2;
	private static final Logger LOG = LoggerFactory.getLogger(CommandLauncher.class);

	/**
	 * Checks if that command is eligible for launch regarding the given
	 * {@link MessageReceivedEvent}. If the command is not eligible for launch, this will
	 * throw a {@link CommandLaunchException}. This method is also ran when the
	 * CommandLauncher attempts to run a command.
	 *
	 * @param event
	 * @param command
	 *            command to check
	 *
	 * @throws CommandLaunchException
	 *             if the command is not eligible
	 */
	public static void checkCommand(@Nonnull GuildMessageReceivedEvent event,
	                                @Nonnull Command command) throws CommandLaunchException {
		if (CommandList.isDisabled(command)) {
			throw new CommandDisabledException(true);
		}
		// Checks if command is globally disabled

		if (ProviderManager.CUSTOMIZATIONS.getCustomization(event.getGuild()).isDisabled(command)) {
			throw new CommandDisabledException(false);
		}
		// Checks if command is internally disabled

		if (command.getRatelimit() != 0) {
			Ratelimits ratelimits = RatelimitsManager.getRatelimits(command);
			long remaining = ratelimits.setWaitingSeconds(command.getRatelimit()).check(event.getAuthor().getId());
			if (remaining != -1) {
				throw new RatelimitedException(remaining);
			}
		}
		// Checks if the user is ratelimited
	}

	@Override
	public void onCommand(GuildMessageReceivedEvent event, Command command) throws Throwable {

		checkCommand(event, command);

		ProcessManager.getProcesses().stream().filter(cp -> {

			User author = cp.getAuthor();
			if (author == null)
				return false;

			return author.getIdLong() == event.getAuthor().getIdLong();

		}).skip(MAX_COMMANDS_PER_USER - 1L).forEach(CommandProcess::kill);
		// Checks if that user owns another command and kills it if so

		CommandProcess proc = ProcessManager.wrap(command, event);

		LOG.debug("Launching {} (pid={}, author={}, guild={})", command.getName(), proc.getPid(),
		    event.getAuthor().getId(), event.getGuild().getId());
		// Logs the details

		proc.run();
		// Executes the command in a new thread
	}

}
