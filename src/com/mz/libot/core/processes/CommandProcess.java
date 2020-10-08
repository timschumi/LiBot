package com.mz.libot.core.processes;

import org.apache.commons.lang3.math.NumberUtils;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.BotData;
import com.mz.libot.core.commands.Command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class CommandProcess {

	private int pid;
	private int commandId;
	private long authorId;
	private Thread thread;
	private long channelId;
	private long guildId;
	private String addData;

	/**
	 * Builds a new {@link CommandProcess} from a thread.
	 *
	 * @param t
	 *            thread
	 * @return a new CommandProcess
	 * @throws IllegalArgumentException
	 *             if a new CommandProcess can't be created from that thread (look
	 *             exception's message)
	 */
	public static CommandProcess valueOf(Thread t) {
		String[] values = t.getName().split("&&");

		if (values.length != 6 && values.length != 7)
			throw new IllegalArgumentException("Invalid thread name; expected 6 or 7 values, found " + values.length);

		Integer id;
		try {
			id = Integer.valueOf(values[1]);
			if (LiBotCore.commands.getById(id) == null)
				throw new IllegalArgumentException("Invalid command ID; command " + id + " does not exist!");
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid command ID; '" + values[1] + "' is not a parsable number!");
		}

		String pidString = values[2];
		if (!NumberUtils.isParsable(pidString))
			throw new IllegalArgumentException("Invalid PID; PID must be a number!");

		String authorIdString = values[3];
		if (!NumberUtils.isParsable(authorIdString))
			throw new IllegalArgumentException("Invalid author id; author id must be a number!");

		String channelIdString = values[4];
		if (!NumberUtils.isParsable(channelIdString))
			throw new IllegalArgumentException("Invalid channel id; channel id must be a number!");

		String guildIdString = values[5];
		if (!NumberUtils.isParsable(guildIdString))
			throw new IllegalArgumentException("Invalid guild id; guild id must be a number!");

		return new CommandProcess(id, Integer.parseInt(pidString), Long.parseLong(authorIdString),
		    Long.parseLong(channelIdString), Long.parseLong(guildIdString), t, values.length == 7 ? values[6] : null);
	}

	/**
	 * Builds a new {@link CommandProcess} from raw data.
	 *
	 * @param commandId
	 *            {@link CommandProcess}'s {@link Command} ID
	 * @param pid
	 *            this {@link CommandProcess}'s process id. A PID should be unique for
	 *            each process
	 * @param authorId
	 *            ID of the {@link User} that launched this {@link CommandProcess}
	 * @param channelId
	 *            ID of the {@link TextChannel} the {@link CommandProcess} has been
	 *            launched in
	 * @param guildId
	 *            ID of the {@link Guild} the {@link CommandProcess} has been launched in
	 * @param thread
	 *            this {@link CommandProcess}'s {@link Thread}
	 * @param additionalData
	 *            additional data in the thread name (specified by
	 *            {@link Command#getAdditionalData()}, may be modified)
	 */
	public CommandProcess(int commandId, int pid, long authorId, long channelId, long guildId, Thread thread,
	                      String additionalData) {
		if (LiBotCore.commands.getById(commandId) == null)
			throw new IllegalArgumentException("Invalid command ID; command " + commandId + " does not exist!");

		this.commandId = commandId;
		this.pid = pid;
		this.authorId = authorId;
		this.thread = thread;
		this.channelId = channelId;
		this.guildId = guildId;
		this.addData = additionalData;
	}

	/**
	 * @return this {@link CommandProcess}'s PID
	 */
	public int getPid() {
		return this.pid;
	}

	/**
	 * @return the {@link Command} running in this process
	 */
	public Command getCommand() {
		return LiBotCore.commands.getById(this.commandId);
	}

	/**
	 * @return the {@link User} that launched this command or null if provided
	 *         {@link User} no longer exist
	 */
	public User getAuthor() {
		return BotData.getJDA().getUserById(this.authorId);
	}

	/**
	 * @return this {@link CommandProcess}'s {@link Thread}
	 */
	public Thread getThread() {
		return this.thread;
	}

	/**
	 * @return the {@link TextChannel} this {@link Command} was executed from or null if
	 *         that {@link TextChannel} does no longer exist
	 */
	public TextChannel getChannel() {
		return BotData.getJDA().getTextChannelById(this.channelId);
	}

	/**
	 *
	 * @return the {@link Guild} this {@link Command} was executed from or null if the
	 *         {@link Guild} does no longer exist
	 */
	public Guild getGuild() {
		return BotData.getJDA().getGuildById(this.guildId);
	}

	/**
	 * @return additional data in the thread name (specified by
	 *         {@link Command#getAdditionalData()}, may be modified)
	 */
	public String getAdditionalData() {
		return this.addData;
	}

	/**
	 * Kills this {@link CommandProcess} (interrupts the {@link Thread}). This operation
	 * is equivalent to {@link ProcessManager#kill(CommandProcess)}.
	 *
	 * @return true if the {@link CommandProcess} has been successfully killed, false if
	 *         {@link CommandProcess} has already died/been killed or if the current
	 *         {@link Thread} doesn't have access to the {@link CommandProcess}'s
	 *         {@link Thread}
	 */
	public boolean kill() {
		return ProcessManager.kill(this);
	}

	/**
	 * Runs the current {@link CommandProcess}. Note that a command process can only be
	 * executed once.
	 *
	 * @return true if the {@link CommandProcess} has been launched, false if its
	 *         {@link Thread} is either already alive or has already died
	 */
	public boolean run() {
		if (this.thread.isInterrupted() || this.thread.isAlive())
			return false;

		try {
			this.thread.start();
		} catch (IllegalThreadStateException e) {
			return false;
		}

		return true;
	}
}
