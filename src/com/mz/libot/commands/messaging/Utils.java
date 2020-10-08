package com.mz.libot.commands.messaging;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.utils.EventWaiter;
import com.mz.utils.FormatAs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;

final class Utils {

	private static final int MAIL_MESSAGE_CAP = 1500;
	static final String BLOCKED_KEY = "blocks";
	private static final int MAIL_WRAP = 27;
	private static final int MAIL_SUBJECT_CAP = 30;

	private Utils() {}

	private static StringBuilder generateMail(User target, String subject, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("Subject: " + subject);
		sb.append("\nTo: " + target.getName() + " (" + target.getId() + ")");
		sb.append("\n---------------------------");
		for (String line : FormatAs.wrap(message, MAIL_WRAP)) {
			sb.append("\n" + line);
		}

		return sb;
	}

	static MessageEmbed getMail(EventWaiter ew, MessageChannel channel, User author, User target) {
		StringBuilder mail = generateMail(target, "", "");
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor("From " + author.getName() + "#" + author.getDiscriminator(), null, author.getAvatarUrl());
		builder.setTitle("LiBot mailing service");
		builder.setColor(Constants.LITHIUM);
		builder.setFooter("Sender's ID: " + author.getId(), null);

		mail.append("\n_Type in a subject or EXIT to abort_");
		builder.setDescription(mail.toString());
		channel.sendMessage(builder.build()).complete();
		// Reviews the mail

		String subject = null;
		while (subject == null) {
			String subjectCheck = ew.getString();
			if (subjectCheck.length() > MAIL_SUBJECT_CAP) {
				channel.sendMessage("Subject can only contain up to 30 characters").queue();
				continue;
			}

			subject = BotUtils.escapeMarkdown(subjectCheck);
		}
		// Retrieves subject

		if ("exit".equalsIgnoreCase(subject) && ew.getBoolean("Are you sure you want to abort this mail?"))
			throw new CanceledException();
		// Aborts if user wants to

		mail = generateMail(target, subject, "");
		mail.append("\n_Type in the message or EXIT to abort_");
		builder.setDescription(mail.toString());
		channel.sendMessage(builder.build()).complete();
		// Reviews the mail

		String message = null;
		while (message == null) {
			String messageCheck = ew.getString();
			if (messageCheck.length() > MAIL_MESSAGE_CAP) {
				channel.sendMessage("Message can only contain up to " + MAIL_MESSAGE_CAP + " characters").queue();
				continue;
			}

			message = BotUtils.escapeMarkdown(messageCheck);
		}
		// Retrieves message

		if ("exit".equalsIgnoreCase(message) && ew.getBoolean("Are you sure you want to abort this mail?"))
			throw new CanceledException();
		// Aborts if user wants to

		mail = generateMail(target, subject, message);
		builder.setDescription(mail.toString());
		channel.sendMessage(builder.build()).complete();
		// Reviews the mail

		return builder.build();
	}

	static boolean isBlocked(String userId, String targetId) throws IOException {
		String blockedJson = BotData.getProperties().getProperty(BLOCKED_KEY, "{}");
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, List<String>>>() {}.getType();

		Map<String, ArrayList<String>> allBlocked = gson.fromJson(blockedJson, type);

		if (!allBlocked.containsKey(userId))
			return false;

		List<String> blocked = allBlocked.get(userId);

		return blocked.contains(targetId);
	}

	@Nonnull
	static User getUnknownUser(@Nonnull String id) {
		return new User() {

			@Override
			public String getAsMention() {
				return "<@:UNKNOWN>";
			}

			@Override
			public long getIdLong() {
				return Long.parseLong(id);
			}

			@Override
			public RestAction<PrivateChannel> openPrivateChannel() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isBot() {
				return false;
			}

			@Override
			public boolean hasPrivateChannel() {
				return false;
			}

			@Override
			public String getName() {
				return "UNKNOWN";
			}

			@Override
			public List<Guild> getMutualGuilds() {
				return new ArrayList<>();
			}

			@SuppressWarnings("null")
			@Override
			public JDA getJDA() {
				return BotData.getJDA();
			}

			@Override
			public String getEffectiveAvatarUrl() {
				return "";
			}

			@Override
			public String getDiscriminator() {
				return "0000";
			}

			@Override
			public String getDefaultAvatarUrl() {
				return "";
			}

			@Override
			public String getDefaultAvatarId() {
				return "0";
			}

			@Override
			public String getAvatarUrl() {
				return "";
			}

			@Override
			public String getAvatarId() {
				return "0";
			}

			@Override
			public String getAsTag() {
				return "";
			}

			@SuppressWarnings("null")
			@Override
			public EnumSet<UserFlag> getFlags() {
				return EnumSet.noneOf(UserFlag.class);
			}

			@Override
			public int getFlagsRaw() {
				return 0;
			}

			@Deprecated
			@Override
			public boolean isFake() {
				return false;
			}
		};
	}

}
