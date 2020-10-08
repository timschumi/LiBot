package com.mz.libot.core.data.providers.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.mz.libot.commands.utilities.PollCommand.Poll;
import com.mz.libot.commands.utilities.PollCommand.Poll.InvalidPollException;
import com.mz.libot.core.BotData;
import com.mz.libot.core.data.properties.PropertyManager;
import com.mz.libot.core.data.providers.SnowflakeProvider;

import net.dv8tion.jda.api.entities.Guild;

public class PollProvider extends SnowflakeProvider<Poll> {

	private static final Predicate<Long> FILTER = id -> BotData.getJDA().getGuildById(id.longValue()) == null;

	private Thread pmt;
	// Poll Manager Thread

	private static final Logger LOG = LoggerFactory.getLogger(PollProvider.class);

	/**
	 * Restarts current PollManagerThread. This is used to reload new changes from the
	 * data.
	 */
	public void restartPollServiceThread() {
		interruptPollService();

		this.pmt = new Thread(this::pollManager, "poll-service-thread");

		this.pmt.start();
	}

	/**
	 * Interrupts current poll manager thread
	 *
	 * @return true if thread was interrupted, false if thread is already interrupted /
	 *         doesn't exist
	 */
	public boolean interruptPollService() {
		if (this.pmt == null || this.pmt.isInterrupted()) {
			return false;
		}

		this.pmt.interrupt();
		return true;
	}

	/**
	 * Poll manager method. To be used as a new thread.
	 */
	private void pollManager() {
		LOG.debug("Starting poll manager..");

		Map<Long, Poll> polls = new HashMap<>(this.data);
		// Creates an own copy of provider's polls

		while (!polls.isEmpty()) {
			// Loops as long as there are still active polls

			try {
				Thread.sleep(Collections.min(
				    polls.values().stream().map(p -> Long.valueOf(p.getRemainingTime())).collect(Collectors.toList())));
				// Waits for the minimal amount of time
			} catch (InterruptedException e) {
				// If poll manager gets interrupted

				LOG.debug("Poll manager was interrupted");
				Thread.currentThread().interrupt();
				return;
			}

			List<Long> toRemove = new ArrayList<>();
			for (Entry<Long, Poll> entry : polls.entrySet()) {
				// Cycles all the polls

				Poll poll = entry.getValue();
				try {
					if (poll.submit()) {
						// Attempts to submit the poll. This won't do anything if the poll hansn't ended
						// yet

						toRemove.add(entry.getKey());
						// Adds that poll to the removal list

						LOG.debug("Successfully submitted poll from {}.", poll.getGuild().getName());
						continue;
					}
				} catch (InvalidPollException e) {
					// Catches if anything is wrong with that poll

					poll.check();
					toRemove.add(entry.getKey());
					// Adds that poll to the removal list

					LOG.error("Poll from {} is invalid! Removing this poll.", poll.getGuild().getName());
					// TODO actually log the reason the poll is invalid
					continue;
				}
			}

			if (!toRemove.isEmpty()) {
				// Removes all of the entries in the removal list

				toRemove.forEach(e -> {

					polls.remove(e);
					this.data.remove(e); // Direct access

				});
				// Removes all the entries

				toRemove.clear();
				// Clears the removal list

				this.store(BotData.getProperties());
				// Attempts to post the updated global polls list
			}

		}
	}

	public void putPoll(@Nonnull Guild guild, @Nonnull Poll poll) {
		this.data.put(guild.getIdLong(), poll);

		update();
	}

	public void remove(long guildId) {
		this.data.remove(guildId);

		update();
	}

	public void remove(Guild guild) {
		this.remove(guild.getIdLong());
	}

	private void update() {
		restartPollServiceThread();

		super.store(BotData.getProperties());
	}

	@Override
	public String getDataKey() {
		return "polls";
	}

	@Override
	protected void onDataLoaded(PropertyManager pm) {
		this.restartPollServiceThread();
	}

	@Override
	public TypeToken<Map<Long, Poll>> getTypeToken() {
		return new TypeToken<>() {};
	}

	@Override
	protected Predicate<Long> getObsoleteFilter() {
		return FILTER;
	}

}
