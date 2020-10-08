package com.mz.libot.core.handlers.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.libot.core.commands.Command;
import com.mz.libot.core.handlers.Handler;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandHandler implements Handler<CommandHandlerParameter> {

	private static final Logger LOG = LoggerFactory.getLogger(CommandHandler.class);

	private final Collection<CommandListener> commandListeners = Collections
	    .synchronizedList(new ArrayList<CommandListener>());

	/**
	 * A listener with some actions for a command
	 *
	 * @author Marko Zajc
	 */
	@SuppressWarnings("unused")
	public static interface CommandListener {

		/**
		 * Called for each registered listener when a command is invoked. Note that the
		 * command launcher is a listener by itself too so do not rely on command already
		 * being executed when using this and use
		 * {@link #onCommandFinished(GuildMessageReceivedEvent, Command)} instead!
		 *
		 * @param event
		 * @param command
		 *
		 * @throws Throwable
		 */
		default void onCommand(@Nonnull GuildMessageReceivedEvent event, @Nonnull Command command) throws Throwable {}

		/**
		 * Called for each registered listener when a command execution is finished.
		 *
		 * @param event
		 * @param command
		 *
		 * @throws Throwable
		 */
		default void onCommandFinished(@Nonnull GuildMessageReceivedEvent event,
		                               @Nonnull Command command) throws Throwable {}

	}

	public void runOnCommandFinished(@Nonnull GuildMessageReceivedEvent event, @Nonnull Command command) {
		this.commandListeners.forEach(listener -> {
			try {
				listener.onCommandFinished(event, command);
			} catch (Throwable e) {
				LOG.error("Caught exception in a command finish listener", e);
			}
		});
	}

	/**
	 * Registers a new CommandListener
	 *
	 * @param listener
	 */
	public void registerListener(@Nonnull CommandListener listener) {
		Collection<CommandListener> toRemove = this.commandListeners.stream()
		    .filter(l -> listener.getClass().isInstance(l))
		    .collect(Collectors.toList());
		toRemove.forEach(this::removeListener);

		this.commandListeners.add(listener);
	}

	/**
	 * Unregisters a CommandListener
	 *
	 * @param listener
	 */
	public void removeListener(@Nonnull CommandListener listener) {
		this.commandListeners.remove(listener);
	}

	@SuppressWarnings("null")
	@Override
	public void handle(CommandHandlerParameter parameter) {
		this.commandListeners.forEach(listener -> {
			try {
				listener.onCommand(parameter.getEvent(), parameter.getCommand());
			} catch (Throwable e) {
				LOG.error("Caught exception in a command listener", e);
			}
		});
	}

}
