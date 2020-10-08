package com.mz.libot.core.handlers;

/**
 * A handler that would handle a specific event (exception, command launched, etc.)
 *
 * @author Marko Zajc
 *
 * @param <P>
 *            type of the parameter
 */
@FunctionalInterface
public interface Handler<P extends HandlerParameter> {

	/**
	 * Handles an event.
	 *
	 * @param parameter
	 */
	void handle(P parameter);

}
