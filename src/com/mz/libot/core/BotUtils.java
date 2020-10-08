package com.mz.libot.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.utils.entities.Customization;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.entities.User;

public class BotUtils {

	private static final Random GLOBAL_RANDOM = new Random();

	public static boolean isOwner(@Nonnull User user) {
		return user.getIdLong() == Bot.getBot().getInfo().getOwnerId();
	}

	/**
	 * Returns a prefix specified by the given guild, if the guild does not explicitly
	 * specify a prefix, default command prefix will be returned.
	 *
	 * @param guild
	 *
	 * @return the current command prefix used for that guild.
	 *
	 * @see Constants#DEFAULT_COMMAND_PREFIX
	 * @see Customization#setCommandPrefix(String)
	 */
	@Nonnull
	public static String getCommandPrefix(@Nullable Guild guild) {
		if (guild == null) {
			return Constants.DEFAULT_COMMAND_PREFIX;
		}
		return ProviderManager.CUSTOMIZATIONS.getCustomization(guild).getCommandPrefix();
	}

	/**
	 * The same as {@link #getCommandPrefix(Guild)} except that it escapes all known
	 * markdown in it.
	 *
	 * @param guild
	 *
	 * @return the current command prefix used for that guild.
	 *
	 * @see Constants#DEFAULT_COMMAND_PREFIX
	 * @see Customization#setCommandPrefix(String)
	 */
	@Nonnull
	public static String getCommandPrefixEscaped(@Nullable Guild guild) {
		return escapeMarkdown(getCommandPrefix(guild));
	}

	/**
	 * Builds a simple embedded message.
	 *
	 * @param title
	 *            title of the embed
	 * @param message
	 *            message (description) of the embed
	 * @param color
	 *            color of the embed
	 * @param footer
	 *            footer to use
	 *
	 * @return embedded message
	 */
	@Nonnull
	public static MessageEmbed buildEmbed(@Nullable String title, @Nonnull String message, @Nullable 	String footer,
	                                      @Nullable Color color) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.appendDescription(message);
		builder.setFooter(footer, null);
		builder.setColor(color);

		return builder.build();
	}

	/**
	 * Builds a simple embedded message.
	 *
	 * @param message
	 *            message (description) of the embed
	 * @param color
	 *            color of the embed
	 *
	 * @return embedded message
	 */
	@Nonnull
	public static MessageEmbed buildEmbed(@Nonnull String message, @Nullable Color color) {
		return buildEmbed(null, message, null, color);
	}

	/**
	 * Builds a simple embedded message.
	 *
	 * @param title
	 *            title of the embed
	 * @param message
	 *            message (description) of the embed
	 * @param color
	 *            color of the embed
	 *
	 * @return embedded message
	 */
	@Nonnull
	public static MessageEmbed buildEmbed(@Nullable String title, @Nonnull  String message, @Nullable Color color) {
		return buildEmbed(title, message, null, color);
	}

	/**
	 * Returns text with properly escaped markdown.
	 *
	 * @param text
	 *            text with markdown
	 *
	 * @return text with escaped markdown
	 */
	@SuppressWarnings("null")
	@Nonnull
	public static String escapeMarkdown(@Nonnull String text) {
		String result = text;
		for (String markdown : Constants.getMarkdown()) {
			result = result.replace(markdown, "\\" + markdown);
		}

		return result;
	}

	/**
	 * Returns text with properly unescaped markdown.
	 *
	 * @param text
	 *            text with markdown
	 *
	 * @return text with unescaped markdown
	 */
	@SuppressWarnings("null")
	@Nonnull
	public static String unescapeMarkdown(@Nonnull String text) {
		String result = text;
		for (String markdown : Constants.getMarkdown())
			result = result.replace("\\" + markdown, markdown);

		return result;
	}

	/**
	 * Returns the default escaped command prefix. If no prefix was assigned before, this
	 * will return '\*'.
	 *
	 * @return the default escaped command prefix
	 *
	 * @see #getCommandPrefix(Guild)
	 */
	public static String getDefaultCommandPrefixEscaped() {
		return escapeMarkdown(Constants.DEFAULT_COMMAND_PREFIX);
	}

	/**
	 * @return the global random generator for LiBot
	 */
	public static Random getRandom() {
		return GLOBAL_RANDOM;
	}

	public static List<Message> embedToMessage(MessageEmbed embed) {
		StringBuilder sb = new StringBuilder();

		sb.append(embed.getTitle() != null ? ("\n**" + embed.getTitle() + "**").replace("\n", "\n\t") : "");
		// Appends embed's title (if present)

		sb.append(embed.getDescription() != null ? ("\n" + embed.getDescription()).replace("\n", "\n\t") : "");
		// Appends embed's description

		for (Field field : embed.getFields()) {
			sb.append("\n");
			sb.append(field.getName() != null ? ("\n**" + field.getName() + "**").replace("\n", "\n\t\t") : "");
			// Appends field's name (if present)

			sb.append(field.getValue() != null ? ("\n" + field.getValue()).replace("\n", "\n\t\t") : "");
			// Appends field's value (if present)
		}
		// Appends all embed's fields' data

		sb.append("\n");

		Footer footer = embed.getFooter();
		sb.append(footer != null ? ("\n_" + footer.getText() + "_").replace("\n", "\n\t") : "");
		// Appends embed's footer

		MessageBuilder mb = new MessageBuilder(sb.toString());

		return Collections.unmodifiableList(new ArrayList<>(mb.buildAll()));
	}

	private BotUtils() {}
}
