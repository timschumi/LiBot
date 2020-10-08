package com.mz.libot.core.processes.tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task {

	private static final Logger LOG = LoggerFactory.getLogger(Task.class);

	private final String name;
	private final Runnable runnable;
	private final Task[] children;

	public Task(Runnable runnable, String name) {
		this.name = name;
		this.runnable = runnable;
		this.children = null;
	}

	public Task(Runnable runnable, String name, Task[] children) {
		this.name = name;
		this.runnable = runnable;
		this.children = children.clone();
	}

	public Future<Void> execute() {
		LOG.debug("Executing {}...", this.name);

		Thread thread = new Thread(() -> {
			this.runnable.run();
			LOG.debug("Executed {}.", this.name);

			if (this.children != null) {
				if (this.children.length == 1) {
					// Doesn't take as much overhead if there is only 1 child task
					try {
						this.children[0].execute().get();
					} catch (ExecutionException e) {
						LOG.error("Failed to execute {}.", this.children[0].getName());
						LOG.error("", e);

					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

				} else {
					// Makes children execution async, essentially speeding up the execution
					@SuppressWarnings("rawtypes")
					Future[] futures = new Future[this.children.length];

					for (int i = 0; i < this.children.length; i++) {
						futures[i] = this.children[i].execute();
					}

					for (int i = 0; i < futures.length; i++) {
						try {
							futures[i].get();
						} catch (ExecutionException e) {
							LOG.error("Failed to execute {}.", this.children[i].getName());
							LOG.error("", e);

						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
			}

		}, this.name);
		thread.setDaemon(true);
		thread.start();

		return new Future<>() {

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				if (isDone())
					return false;

				if (mayInterruptIfRunning) {
					thread.interrupt();
				} else {
					try {
						get();
					} catch (ExecutionException e) {
						return false;

					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return false;
					}
				}

				return true;
			}

			@Override
			public boolean isCancelled() {
				return thread.isInterrupted();
			}

			@Override
			public boolean isDone() {
				return !thread.isAlive();
			}

			@Override
			public Void get() throws InterruptedException, ExecutionException {
				thread.join();
				return null;
			}

			@Override
			public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
			                                             TimeoutException {
				thread.join(unit.toNanos(timeout));
				if (!isDone())
					throw new TimeoutException();

				return null;
			}
		};

	}

	public String getName() {
		return this.name;
	}

}
