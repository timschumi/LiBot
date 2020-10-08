package com.mz.libot.core.data.providers.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.data.properties.PropertyManager;
import com.mz.libot.core.data.providers.SnowflakeProvider;
import com.mz.libot.core.data.providers.impl.TimerProvider.Timer;

import net.dv8tion.jda.api.entities.User;

public class TimerProvider extends SnowflakeProvider<Timer> {

	public static class Timer {

		private final String text;
		private final long endTime;

		public Timer(String text, long endTime) {
			this.text = text;
			this.endTime = endTime;
		}

		public String getText() {
			return this.text;
		}

		public long getEndTime() {
			return this.endTime;
		}

	}

	private static final Predicate<Long> FILTER = id -> BotData.getJDA().getUserById(id.longValue()) == null;

	private Thread pmt;
	// Timer Manager Thread

	private static final Logger LOG = LoggerFactory.getLogger(TimerProvider.class);

	/**
	 * Restarts current TimerManagerThread. This is used to reload new changes from the
	 * data.
	 */
	public void restartTimerServiceThread() {
		interruptTimerService();

		this.pmt = new Thread(() -> timerManager(), "TimerServiceThread");

		this.pmt.start();
	}

	/**
	 * Interrupts current timer manager thread
	 *
	 * @return true if thread was interrupted, false if thread is already interrupted /
	 *         doesn't exist
	 */
	public boolean interruptTimerService() {
		if (this.pmt == null || this.pmt.isInterrupted()) {
			return false;
		}

		this.pmt.interrupt();
		return true;
	}

	/**
	 * Timer manager method. To be used as a new thread.
	 */
	private void timerManager() {
		LOG.debug("Starting timer manager..");

		Map<Long, Timer> timers = new HashMap<>(this.data);
		// Creates an own copy of provider's timers

		while (!timers.isEmpty()) {
			// Loops as long as there are still active timers

			long current = System.currentTimeMillis();

			try {
				Thread.sleep(Collections.min(timers.values()
				    .stream()
				    .map(p -> p.getEndTime() - current < 0 ? 0 : p.getEndTime() - current)
				    .collect(Collectors.toList())));
				// Waits for the minimal amount of time
			} catch (InterruptedException e) {
				// If timer manager gets interrupted

				LOG.debug("Timer manager was interrupted");

				return;
			}

			List<Long> toRemove = new ArrayList<>();
			for (Entry<Long, Timer> entry : timers.entrySet()) {
				// Cycles all the timers

				Timer timer = entry.getValue();
				if (current > timer.getEndTime()) {
					User user = BotData.getJDA().getUserById(entry.getKey());
					if (user != null)
						user.openPrivateChannel()
						    .queue(pc -> pc.sendMessage("**\u23F3 Timer:** " + timer.getText()).queue());

					toRemove.add(entry.getKey());
					// Adds that timer to the removal list

					continue;
				}
			}

			if (!toRemove.isEmpty()) {
				// Removes all of the entries in the removal list

				toRemove.forEach(e -> {

					timers.remove(e);
					this.data.remove(e); // Direct access

				});
				// Removes all the entries

				toRemove.clear();
				// Clears the removal list

				this.store(BotData.getProperties());
				// Attempts to post the updated global timers list
			}

		}
	}

	public void putTimer(User user, Timer timer) {
		this.data.put(user.getIdLong(), timer);

		update();
	}

	public void remove(long userId) {
		this.data.remove(userId);

		update();
	}

	public void remove(User user) {
		this.remove(user.getIdLong());
	}

	private void update() {
		restartTimerServiceThread();

		super.store(BotData.getProperties());
	}

	@Override
	public String getDataKey() {
		return "timers";
	}

	@Override
	protected void onDataLoaded(PropertyManager pm) {
		this.restartTimerServiceThread();
	}

	@Override
	public TypeToken<Map<Long, Timer>> getTypeToken() {
		return new TypeToken<>() {};
	}

	@Override
	protected Predicate<Long> getObsoleteFilter() {
		return FILTER;
	}

}
