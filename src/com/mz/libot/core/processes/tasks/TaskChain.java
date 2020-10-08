package com.mz.libot.core.processes.tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskChain {

	private static final Logger LOG = LoggerFactory.getLogger(Task.class);

	private final Task[] tasks;
	private final String name;

	public TaskChain(Task[] tasks, String name) {
		this.tasks = tasks.clone();
		this.name = name;
	}

	@SuppressWarnings("rawtypes")
	public void executeAll(boolean awaitCompletion) {
		Future[] futures = new Future[this.tasks.length];
		for (int i = 0; i < this.tasks.length; i++) {
			futures[i] = this.tasks[i].execute();
		}

		if (awaitCompletion)
			for (int i = 0; i < futures.length; i++) {
				try {
					LOG.info("[{}%] Executing {} (task chain)", (int) ((float) i / (float) futures.length * 100f),
					    this.name);

					futures[i].get();
				} catch (ExecutionException e) {
					LOG.error("Failed to execute {}.", this.tasks[i].getName());
					LOG.error("", e);

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

		LOG.info("[100%] Executing {} (task chain)", this.name);
	}

}
