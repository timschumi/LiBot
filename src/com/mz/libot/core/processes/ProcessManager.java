package com.mz.libot.core.processes;

import java.util.Set;
import java.util.stream.Collectors;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.ratelimits.RatelimitsManager;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.handlers.Handlers;
import com.mz.libot.core.handlers.exception.ExceptionHandlerParameter;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ProcessManager {

	public static final int MAX_PID = 9999;

	/**
	 * Creates a thread name from the given parameters.
	 *
	 * @param command
	 * @param event
	 * @return a valid thread name for given parameters
	 */
	public static String getThreadName(Command command, GuildMessageReceivedEvent event) {
		return "Command&&"
		    + command.getId()
		    + "&&"
		    + getNewPid()
		    + "&&"
		    + event.getAuthor().getId()
		    + "&&"
		    + event.getChannel().getId()
		    + "&&"
		    + event.getGuild().getId()
		    + (command.getAdditionalData() != null ? "&&" + command.getAdditionalData() : "");
	}

	/**
	 * Wraps a Command and an MessageReceivedEvent into a runnable CommandProcess.
	 *
	 * @param command
	 * @param event
	 * @return CommandProcess
	 */
	public static CommandProcess wrap(Command command, GuildMessageReceivedEvent event) {
		return CommandProcess.valueOf(new Thread(() -> {
			try {
				Parameters params = Commands.generateParameters(command, event);
				// Generates parameters

				command.startupCheck(event, params);
				// Checks if the command can be launched

				command.execute(event, params);
				// Executes the command

				if (command.getRatelimit() != 0)
					RatelimitsManager.getRatelimits(command).register(event.getAuthor().getId());
				// Registers the ratelimit if the command has finished

				Handlers.COMMAND_HANDLER.runOnCommandFinished(event, command);

			} catch (Throwable t) {
				Handlers.EXCEPTION_HANDLER.handle(new ExceptionHandlerParameter(event, t, command));
				// Handles the exception on exception
			}
		}, getThreadName(command, event)));
	}

	/**
	 * Returns a new free PID. Each PID is unique for the process and will not be used
	 * for any other task until the holder process is killed
	 *
	 * @return a new free PID
	 */
	public static int getNewPid() {
		int pid = 0;

		while (getProcesses().stream().map(CommandProcess::getPid).collect(Collectors.toList()).contains(pid)
		    || pid == 0) {
			pid = BotUtils.getRandom().nextInt(MAX_PID + 1);

		}

		return pid;
	}

	/**
	 * Returns a list of currently running commands
	 *
	 * @return a list of currently running commands
	 */
	public static Set<CommandProcess> getProcesses() {
		return Thread.getAllStackTraces()
		    .keySet()
		    .stream()
		    .filter(t -> t.getName().startsWith("Command") && t != Thread.currentThread())
		    .map(CommandProcess::valueOf)
		    .collect(Collectors.toSet());
	}

	/**
	 * Retrieves process assigned to the given PID
	 *
	 * @param pid
	 *            PID to search for
	 * @return process assigned to that PID or null if such process does not exist
	 */
	public static CommandProcess getProcess(int pid) {
		for (CommandProcess proc : getProcesses()) {
			if (proc.getPid() == pid) {
				return proc;
			}
		}

		return null;
	}

	/**
	 * Interrupts a CommandProcess.
	 *
	 * @param process
	 *            command process to kill
	 * @return true if process was killed, false if either the process has already died
	 */
	public static boolean kill(CommandProcess process) {
		if (!process.getThread().isAlive())
			return false;

		try {
			process.getThread().interrupt();
			return true;
		} catch (SecurityException e) {
			return false;
		}
	}

	private ProcessManager() {}

}
