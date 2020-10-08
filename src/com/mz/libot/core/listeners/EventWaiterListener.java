package com.mz.libot.core.listeners;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.mz.libot.core.commands.exceptions.runtime.TimeoutException;
import com.mz.libot.core.processes.CommandProcess;
import com.mz.libot.utils.MessageLock;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class EventWaiterListener implements EventListener {

	protected static final Map<CommandProcess, Triple<Predicate<GenericEvent>, MessageLock<GenericEvent>, Predicate<Void>>> EVENT_WAITERS = new ConcurrentHashMap<>();

	/**
	 * Pauses the current thread and awaits a certain event.<br>
	 * <strong>This will throw a {@link IllegalArgumentException} if you do not access it
	 * from a command thread</strong>
	 *
	 * @param <T>
	 *            type of event to await
	 *
	 * @param predicate
	 *            predicate that will be tested before the event is returned. The event
	 *            is ignored if testing the predicate returns false
	 * @param eventClass
	 *            the event class (eg. MessageReceivedEvent.class)
	 * @param nullableCleanupPredicate
	 *            a predicate that will also be tested upon cleanup asides from the
	 *            predicate that tests if command's author and channel still exist. No
	 *            predicate will take place if this is {@code null}
	 * @param timeout
	 *            timeout (0 for no timeout)
	 * @param timeoutUnit
	 *            timeout init (can be <code>null</code> if timeout is 0)
	 *
	 * @return the event
	 *
	 * @throws TimeoutException
	 *             if timeout occurs
	 */
	@SuppressWarnings("unchecked")
	public static <T extends GenericEvent> T awaitEvent(Predicate<GenericEvent> predicate,
	                                                    @Nullable Predicate<Void> nullableCleanupPredicate, int timeout,
	                                                    TimeUnit timeoutUnit,
	                                                    final Class<T> eventClass) throws TimeoutException {
		Predicate<Void> cleanupPredicate = nullableCleanupPredicate;
		if (cleanupPredicate == null)
			cleanupPredicate = p -> false;

		MessageLock<GenericEvent> lock = new MessageLock<>();

		CommandProcess proc = CommandProcess.valueOf(Thread.currentThread());

		Predicate<GenericEvent> isInstance = eventClass::isInstance;

		EVENT_WAITERS.put(proc, new ImmutableTriple<>(isInstance.and(predicate), lock, cleanupPredicate));

		GenericEvent awaited = lock.receive(timeout, timeoutUnit);

		EVENT_WAITERS.remove(proc);

		if (!eventClass.getSuperclass().isInstance(awaited))
			throw new IllegalStateException("Something went wrong; onEvent() returned "
			    + awaited.getClass().getName()
			    + " instead of "
			    + eventClass.getName());

		return (T) awaited;
	}

//   @formatter:off
// TODO test this
//	/**
//	 * Pauses the current thread and awaits a certain event.<br>
//	 * <strong>This will throw a {@link IllegalArgumentException} if you do not access it
//	 * from a command thread</strong>
//	 *
//	 * @param <T>
//	 *            superclass of all acceptable event classes
//	 *
//	 * @param predicate
//	 *            predicate that will be tested before the event is returned. The event
//	 *            is ignored if testing the predicate returns false
//	 * @param eventClasses
//	 *            all acceptable return types
//	 * @param nullableCleanupPredicate
//	 *            a predicate that will also be tested upon cleanup asides from the
//	 *            predicate that tests if command's author and channel still exist. No
//	 *            predicate will take place if this is {@code null}
//	 * @param timeout
//	 *            timeout (0 for no timeout)
//	 * @param timeoutUnit
//	 *            timeout init (can be <code>null</code> if timeout is 0)
//	 *
//	 * @return the event
//	 *
//	 * @throws TimeoutException
//	 *             if timeout occurs
//	 */
//	@SuppressWarnings("unchecked")
//	public static <T extends Event> T awaitAnyEvent(Predicate<Event> predicate, @Nullable Predicate<Void> nullableCleanupPredicate, int timeout, TimeUnit timeoutUnit, Class<? extends T>... eventClasses)
//			throws TimeoutException {
//		Predicate<Void> cleanupPredicate = nullableCleanupPredicate;
//		if (cleanupPredicate == null)
//			cleanupPredicate = p -> false;
//
//		MessageLock<Event> lock = new MessageLock<>();
//
//		CommandProcess proc = CommandProcess.valueOf(Thread.currentThread());
//
//		Predicate<Event> isInstance = e -> {
//			for (Class<? extends T> eventClass : eventClasses) {
//				if (eventClass.isInstance(e))
//					return true;
//			}
//
//			return false;
//		};
//
//		EVENT_WAITERS.put(proc, new ImmutableTriple<>(isInstance.and(predicate), lock, cleanupPredicate));
//
//		Event awaited = lock.receive(timeout, timeoutUnit);
//
//		EVENT_WAITERS.remove(proc);
//
//		boolean isResultInstance = false;
//		for (Class<? extends T> eventClass : eventClasses)
//			if (!eventClass.getSuperclass().isInstance(awaited))
//				isResultInstance = true;
//
//		if (isResultInstance)
//			throw new IllegalStateException("Something went wrong; onEvent() returned " + awaited.getClass().getName()
//					+ " instead of " + Arrays.toString(eventClasses));
//
//		return (T) awaited;
//	}
//  @formatter:on

	@Override
	public void onEvent(GenericEvent event) {
		if (!BootListener.isReady())
			return;

		for (Entry<CommandProcess, Triple<Predicate<GenericEvent>, MessageLock<GenericEvent>, Predicate<Void>>> e : EVENT_WAITERS
		    .entrySet()) {
			if (e.getValue().getLeft().test(event))
				e.getValue().getMiddle().send(event);
		}
	}

}
