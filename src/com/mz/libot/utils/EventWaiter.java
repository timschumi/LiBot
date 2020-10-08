package com.mz.libot.utils;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.exceptions.runtime.TimeoutException;
import com.mz.libot.core.listeners.EventWaiterListener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * A class used to await certain events. This requires command execution to be
 * multi-threaded!
 */
public class EventWaiter {

	private int timeout;
	private final User user;
	private final MessageChannel channel;

	/**
	 * Creates a new EventWaiter. Timeout will be disabled by default.
	 *
	 * @param user
	 * @param channel
	 */
	public EventWaiter(User user, MessageChannel channel) {
		this.user = user;
		this.channel = channel;
	}

	/**
	 * Creates a new EventWaiter.
	 *
	 * @param user
	 * @param channel
	 * @param timeout
	 *            timeout to use
	 */
	public EventWaiter(User user, MessageChannel channel, int timeout) {
		this(user, channel);
		this.timeout = timeout;
	}

	/**
	 * @param message
	 *
	 * @return the first (un)reaction from the user to the given message
	 */
	public MessageReaction getReaction(Message message) {
		return EventWaiterListener.awaitEvent(p -> {
			GenericMessageReactionEvent e = (GenericMessageReactionEvent) p;
			return e.getUserIdLong() == this.user.getIdLong() && e.getMessageIdLong() == message.getIdLong();
		}, p -> {
			try {
				message.getChannel().retrieveMessageById(message.getIdLong()).complete();
				return false;
			} catch (RuntimeException e) {
				return true;
			}
		}, this.timeout, TimeUnit.SECONDS, GenericMessageReactionEvent.class).getReaction();
	}

	/**
	 * Lets the user pick a message with (un)reacting to it with the specified emoji.
	 * Please send a help message before calling this because the user might get confused
	 * (message must be manually reacted to) and possibly end up not doing anything,
	 * resulting in an unused thread if no timeout is specified.
	 *
	 * @param emoji
	 *
	 * @return a message
	 *
	 * @throws RuntimeException
	 *             on InterruptedException
	 * @throws TimeoutException
	 */
	public Message getMessage(String emoji) {
		GenericMessageReactionEvent event = EventWaiterListener.awaitEvent(p -> {

			GenericMessageReactionEvent e = (GenericMessageReactionEvent) p;

			return e.getUserIdLong() == this.user.getIdLong() && e.getChannel().getIdLong() == this.channel.getIdLong()
			    && e.getReactionEmote().getName().equals(emoji);

		}, null, this.timeout, TimeUnit.SECONDS, GenericMessageReactionEvent.class);

		return event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
	}

	/**
	 * Same as {@link EventWaiter#getString()} but it returns Message instead of String
	 *
	 * @return a message
	 *
	 * @throws RuntimeException
	 *             on InterruptedException
	 * @throws TimeoutException
	 */
	public Message getMessage() {
		return EventWaiterListener.awaitEvent(p -> {

			MessageReceivedEvent e = (MessageReceivedEvent) p;

			return e.getAuthor().getIdLong() == this.user.getIdLong()
			    && e.getChannel().getIdLong() == this.channel.getIdLong();

		}, null, this.timeout, TimeUnit.SECONDS, MessageReceivedEvent.class).getMessage();
	}

	/**
	 * Creates a new true/false question in chat (useful for confirmirations). If the
	 * response is from the question issuer and equals "✅", it resumes the listener,
	 * gives true to it and deletes the question, if it equals "❎", it does the same, but
	 * returns false, if it is not "✅" or "❎", does nothing. <br>
	 * <strong>This will automatically include the timeout footer</strong>
	 *
	 * @param questionEmbed
	 *            question message as an embed
	 *
	 * @return boolean from reaction
	 *
	 * @throws RuntimeException
	 *             on InterruptedException
	 * @throws TimeoutException
	 *             if a timeout is set and it expires
	 *
	 * @see EventWaiter#getBoolean(String)
	 * @see EventWaiter#getBoolean(Message)
	 * @see EventWaiter#getBoolean(MessageEmbed, boolean)
	 */
	public boolean getBoolean(@Nonnull MessageEmbed questionEmbed) {
		return getBoolean(questionEmbed, true);
	}

	/**
	 * Creates a new true/false question in chat (useful for confirmirations). If the
	 * response is from the question issuer and equals "✅", it resumes the listener,
	 * gives true to it and deletes the question, if it equals "❎", it does the same, but
	 * returns false, if it is not "✅" or "❎", does nothing.
	 *
	 * @param questionEmbed
	 *            question message as an embed
	 * @param includeTimeoutFooter
	 *            whether to include timeout in the footer // TODO doesn't work
	 *
	 * @return boolean from reaction
	 *
	 * @throws RuntimeException
	 *             on InterruptedException
	 * @throws TimeoutException
	 *             if a timeout is set and it expires
	 *
	 * @see EventWaiter#getBoolean(String)
	 * @see EventWaiter#getBoolean(Message)
	 * @see EventWaiter#getBoolean(MessageEmbed)
	 */
	@SuppressWarnings("null")
	public boolean getBoolean(@Nonnull MessageEmbed questionEmbed, boolean includeTimeoutFooter) {
		Message question = this.channel.sendMessage(questionEmbed).complete();
		// Sends the question

		if (includeTimeoutFooter && this.getTimeout() != 0) {
			EmbedBuilder eb = new EmbedBuilder();
			String footerValue = "This dialog will time out in " + this.getTimeout() + " seconds";

			Footer footer = questionEmbed.getFooter();
			if (footer != null && footer.getText() != null && !"".equals(footer.getText())) {
				eb.setFooter(footer.getText() + " | " + footerValue, null);

			} else {
				eb.setFooter(footerValue, null);
			}
		}

		return getBoolean(question);
	}

	/**
	 * Creates a new true/false question in chat (useful for confirmirations). If the
	 * response is from the question issuer and equals "✅", it resumes the listener,
	 * gives true to it and deletes the question, if it equals "❎", it does the same, but
	 * returns false, if it is not "✅" or "❎", does nothing.
	 *
	 * @param questionText
	 *            question to ask
	 *
	 * @return boolean from reaction
	 *
	 * @throws TimeoutException
	 *             if a timeout is set and it expires
	 * @throws RuntimeException
	 *             on InterruptedException
	 *
	 * @see EventWaiter#getBoolean(MessageEmbed)
	 * @see EventWaiter#getBoolean(MessageEmbed, boolean)
	 * @see EventWaiter#getBoolean(Message)
	 */
	@SuppressWarnings("null")
	public boolean getBoolean(@Nonnull String questionText) {
		Message question = this.channel.sendMessage(questionText).complete();
		// Sends the question

		return getBoolean(question);
	}

	/**
	 * Creates a new true/false question in chat (useful for confirmirations). If the
	 * response is from the question issuer and equals "✅", it resumes the listener,
	 * gives true to it and deletes the question, if it equals "❎", it does the same, but
	 * returns false, if it is not "✅" or "❎", does nothing.
	 *
	 * @param question
	 *            already sent message to refer to
	 *
	 * @return boolean from the reaction
	 *
	 * @throws TimeoutException
	 *             if a timeout is set and it expires
	 * @throws RuntimeException
	 *             on InterruptedException
	 *
	 * @see EventWaiter#getBoolean(String)
	 * @see EventWaiter#getBoolean(MessageEmbed)
	 * @see EventWaiter#getBoolean(MessageEmbed, boolean)
	 */
	public boolean getBoolean(@Nonnull Message question) {

		question.addReaction(Constants.ACCEPT_EMOJI).queue();
		question.addReaction(Constants.DENY_EMOJI).queue();
		// Reacts with "✅" and "❎" to the question (to make it easier to answer the
		// question)

		boolean result = Constants.ACCEPT_EMOJI.equals(EventWaiterListener.awaitEvent(p -> {

			MessageReactionAddEvent e = (MessageReactionAddEvent) p;
			String emote = e.getReactionEmote().getName();

			return e.getUserIdLong() == this.user.getIdLong() && e.getMessageIdLong() == question.getIdLong()
			    && (Constants.ACCEPT_EMOJI.equals(emote) || Constants.DENY_EMOJI.equals(emote));

		}, p -> {

			try {
				question.getChannel().retrieveMessageById(question.getIdLong()).complete();
				return false;
			} catch (RuntimeException e) {
				return true;
			}

		}, this.timeout, TimeUnit.SECONDS, MessageReactionAddEvent.class).getReactionEmote().getName());

		question.delete().queue();

		return result;
	}

	/**
	 * Creates a new answer listener. Listener will go off once the issuer types in
	 * anything.
	 *
	 * @return issuer's answer
	 *
	 * @throws RuntimeException
	 *             on InterruptedException
	 * @throws TimeoutException
	 *             if a timeout is set and it expires
	 */
	public String getString() {
		return this.getMessage().getContentDisplay();
	}

	/**
	 * @return current timeout
	 */
	public int getTimeout() {
		return this.timeout;
	}

	/**
	 * Sets a new timeout (in seconds) for events to occur. Set to a value below 1 to
	 * deactivate timeout (not recommended as it may/will result in obsolete threads).
	 * getX() will throw TimeoutException if timeout expires.
	 *
	 * @param timeout
	 *            new timeout in seconds, 0 to deactivate timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
