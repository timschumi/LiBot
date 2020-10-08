package com.mz.libot.commands.utilities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.mz.libot.commands.utilities.PollCommand.Poll.Choice;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.exceptions.runtime.NumberOverflowException;
import com.mz.libot.core.commands.exceptions.startup.UnpredictedStateException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.utils.EventWaiter;
import com.mz.libot.utils.Parser;
import com.mz.utils.MapUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class PollCommand extends Command {

	private static final int DESCRIPTION_CAP = 500;
	public static final Map<String, Integer> EMOJI;
	static {
		Map<String, Integer> emojis = new HashMap<>();
		emojis.put("\u0031\u20E3", 0);
		emojis.put("\u0032\u20E3", 1);
		emojis.put("\u0033\u20E3", 2);
		emojis.put("\u0034\u20E3", 3);
		emojis.put("\u0035\u20E3", 4);
		emojis.put("\u0036\u20E3", 5);
		emojis.put("\u0037\u20E3", 6);
		emojis.put("\u0038\u20E3", 7);
		emojis.put("\u0039\u20E3", 8);
		emojis.put("\uD83D\uDD1F", 9);

		EMOJI = Collections.unmodifiableMap(emojis);
	}
	private static final int DURATION_DAYS_CAP = 60;
	private static final int CHOICE_DESCRIPTION_LENGTH_CAP = 300;

	public static class Poll {

		/**
		 * An exception that indicates that the action could not be executed because the poll
		 * is no longer valid
		 *
		 * @author Marko Zajc
		 */
		public static class InvalidPollException extends Exception {

			private static final long serialVersionUID = 1;

			public InvalidPollException() {
				super();
			}

		}

		/**
		 * A class representing a choice (with a count of users who chose it)
		 *
		 * @author Marko Zajc
		 */
		public static class Choice {

			private int peopleCount;
			private int number;
			private String title;

			public Choice(int number, String name) {
				this.number = number;
				this.title = name;
			}

			public void setPeopleCount(int peopleCount) {
				this.peopleCount = peopleCount;
			}

			public int getPeopleCount() {
				return this.peopleCount;
			}

			public int getNumber() {
				return this.number;
			}

			public String getTitle() {
				return this.title;
			}

		}

		/**
		 * A class representing a poll submit action
		 *
		 * @author Marko Zajc
		 */
		public static class PollAction {

			@Nonnull
			private User author;
			@Nonnull
			private List<Choice> choices;
			@Nonnull
			private String title;
			@Nonnull
			private TextChannel txtChannel;
			private boolean disclosePublicly;

			public PollAction(@Nonnull User author, @Nonnull List<Choice> choices, @Nonnull String title,
			                  @Nonnull TextChannel txtChannel, boolean disclosePublicly) {
				this.author = author;
				this.choices = new ArrayList<>(choices);
				this.title = title;
				this.txtChannel = txtChannel;
				this.disclosePublicly = disclosePublicly;
			}

			/**
			 * Submits the poll to its author
			 */
			public void submit() {
				int allVotes = this.choices.stream().mapToInt(Choice::getPeopleCount).sum();

				List<Choice> newChoices = new ArrayList<>(this.choices);

				String results = newChoices.stream()
				    .map(choice -> "**#"
				        + (choice.getNumber() + 1)
				        + " - "
				        + choice.getTitle()
				        + "**: "
				        + choice.getPeopleCount()
				        + (choice.getPeopleCount() == 1 ? " person" : " people")
				        + " ("
				        + (int) ((double) choice.getPeopleCount() / (double) allVotes * 100)
				        + "%)")
				    .collect(Collectors.joining("\n"));

				newChoices.sort((c1, c2) -> Integer.compare(c2.getPeopleCount(), c1.getPeopleCount()));

				EmbedBuilder builder = new EmbedBuilder(
				    BotUtils.buildEmbed("Results for \"" + this.title + "\"", "", Constants.SUCCESS));

				builder.addField("Results", results, true);

				this.author.openPrivateChannel().queue(dm -> dm.sendMessage(builder.build()).queue());

				if (this.disclosePublicly)
					this.txtChannel.sendMessage(builder.build()).queue();
			}

		}

		@Nonnull
		private String title;
		@Nonnull
		private String description;

		@Nonnull
		private Collection<Choice> choices;

		private long authorId;
		private long guildId;
		private long textChannelId;
		private long messageId;

		private long endsOn;

		private boolean allowMoreVotes;
		private boolean disclosePublicly;

		private boolean submitted;

		/**
		 * Creates a new Poll
		 *
		 * @param txtChannel
		 *            text channel to post the poll into
		 * @param choices
		 *            list of choices for this poll
		 * @param author
		 *            author of this poll
		 * @param description
		 *            description of this poll
		 * @param name
		 *            name of this poll
		 * @param endsIn
		 *            milliseconds till poll's end
		 * @param allowMoreVotes
		 *            whether to allow more votes per user
		 * @param mentionEveryone
		 *            whether to mention everyone when the poll starts
		 * @param disclosePublicly
		 *            whether to also send the results to the {@code txtChannel}
		 */
		@SuppressWarnings("null")
		public Poll(TextChannel txtChannel, @Nonnull List<Choice> choices, User author, @Nonnull String name,
		            @Nonnull String description, long endsIn, boolean allowMoreVotes, boolean mentionEveryone,
		            boolean disclosePublicly) {
			String choicesString = convertChoices(choices);
			// Converts the choices into a string for easier display

			this.endsOn = System.currentTimeMillis() + endsIn;
			// Calculates the end date

			Message message;
			{
				MessageAction unsentMessage = txtChannel
				    .sendMessage(generateEmbed(name, description, choicesString, author, this.endsOn));
				if (mentionEveryone
				    && txtChannel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MENTION_EVERYONE))
					unsentMessage = unsentMessage.append(txtChannel.getGuild().getPublicRole().getAsMention());

				message = unsentMessage.complete();

			}
			// Creates & sends a poll embed

			for (int i = 0; i < choices.size(); i++)
				message.addReaction(MapUtils.getKeyFromMap(EMOJI, i).get(0)).complete();
			// Adds all allowed reactions to the poll for easier voting

			this.title = name;
			this.description = description;

			this.choices = new ArrayList<>(choices);

			this.authorId = author.getIdLong();
			this.guildId = txtChannel.getGuild().getIdLong();
			this.textChannelId = txtChannel.getIdLong();
			this.messageId = message.getIdLong();

			this.allowMoreVotes = allowMoreVotes;
			this.disclosePublicly = disclosePublicly;

			this.submitted = false;
			// Sets all fields
		}

		@SuppressWarnings("null")
		public Poll() {
			this.title = "";
			this.description = "";
			this.choices = Collections.emptyList();
		}
		// An empty constructor for Gson

		/**
		 * @return name of this poll
		 */
		public String getTitle() {
			return this.title;
		}

		/**
		 * @return the description of this poll
		 */
		public String getDescription() {
			return this.description;
		}

		/**
		 * Returns a list of choices for this poll
		 *
		 * @return choices
		 */
		public Collection<Choice> getChoices() {
			return Collections.unmodifiableCollection(this.choices);
		}

		/**
		 * Returns author of this poll
		 *
		 * @return author
		 *
		 * @throws NullPointerException
		 *             if either the guild was deleted or the author has left it
		 */
		public User getAuthor() {
			Member member = getGuild().getMemberById(this.authorId);

			if (member == null)
				throw new NullPointerException();

			return member.getUser();
		}

		/**
		 * Returns the guild this poll was created in
		 *
		 * @return guild
		 *
		 * @throws NullPointerException
		 *             if the guild was deleted
		 */
		public Guild getGuild() {
			Guild guild = BotData.getJDA().getGuildById(this.guildId);

			if (guild == null) {
				throw new NullPointerException();
			}

			return guild;
		}

		/**
		 * Returns the text channel this poll was created in
		 *
		 * @return text channel
		 *
		 * @throws NullPointerException
		 *             if either the guild or the text channel was deleted
		 */
		public TextChannel getTextChannel() {
			TextChannel txtChannel = getGuild().getTextChannelById(this.textChannelId);

			if (txtChannel == null) {
				throw new NullPointerException();
			}

			return txtChannel;
		}

		/**
		 * Returns the poll message
		 *
		 * @return message
		 *
		 * @throws NullPointerException
		 *             if either the guild, the text channel or the message were deleted or
		 *             are inaccessible
		 */
		public Message getMessage() {
			try {
				return getTextChannel().retrieveMessageById(this.messageId).complete();
			} catch (Exception e) {
				throw new NullPointerException();
			}
		}

		/**
		 * An indicator whether this poll allows users to cast a vote on more than one choice
		 *
		 * @return boolean
		 */
		public boolean allowsMoreVotes() {
			return this.allowMoreVotes;
		}

		/**
		 * Returns the time remaining for this poll to end
		 *
		 * @return time as epoch milliseconds
		 */
		public long getRemainingTime() {
			long time = this.endsOn - System.currentTimeMillis();

			if (time < 0)
				return 0;

			return time;
		}

		/**
		 * Submits the poll results and deletes the poll message if poll ended
		 *
		 * @return true if any action was taken, false if poll hasn't ended yet
		 *
		 * @throws InvalidPollException
		 *             if the poll is no longer valid for some reason {@link Poll#check()}
		 *             returns false
		 *
		 * @see Poll#forceSubmit()
		 */
		public boolean submit() throws InvalidPollException {
			if (!check()) {
				throw new InvalidPollException();
			}

			if (getRemainingTime() != 0) {
				return false;
			}

			forceSubmit();

			return true;
		}

		/**
		 * Force-submits the poll results to the poll author and deletes the poll message
		 * without checking if the poll has actually ended or if it's invalid. <br>
		 * <br>
		 * <strong>This method might throw {@link NullPointerException} in case
		 * {@link Poll#check()} returns <code>false</code>!</strong>
		 *
		 * @see Poll#submit()
		 */
		@SuppressWarnings("null")
		public void forceSubmit() {
			if (this.submitted)
				throw new IllegalStateException("This poll has already been submitted!");

			Message message = getMessage();

			List<Choice> newChoices = new ArrayList<>(this.choices);
			newChoices.sort((c1, c2) -> Integer.compare(c2.getNumber(), c1.getNumber()));

			List<MessageReaction> reactionsList = message.getReactions();

			Map<Integer, MessageReaction> reactions = new HashMap<>();
			reactionsList.forEach(r -> {

				Integer i = EMOJI.get(r.getReactionEmote().getName());
				if (i != null) // Ignores invalid reactions
					reactions.put(i, r);

			});

			if (allowsMoreVotes()) {
				newChoices.forEach(choice -> choice.setPeopleCount(reactions.get(choice.getNumber()).getCount() - 1));

			} else {
				Map<Integer, Choice> choicesMap = new HashMap<>();
				newChoices.forEach(c -> choicesMap.put(c.getNumber(), c));

				Map<Long, Integer> userVotes = new HashMap<>();
				reactions.entrySet()
				    .forEach(entry -> entry.getValue()
				        .retrieveUsers()
				        .complete()
				        .forEach(user -> userVotes.put(user.getIdLong(), entry.getKey())));

				userVotes.remove(BotData.getJDA().getSelfUser().getIdLong()); // won't reset the indexes

				userVotes.entrySet().forEach(entry -> {

					Choice c = choicesMap.get(entry.getValue());
					if (c == null) // Ignores false votes
						return;

					c.setPeopleCount(c.getPeopleCount() + 1);

				});

				newChoices = new ArrayList<>(choicesMap.values());
			}

			new PollAction(getAuthor(), newChoices, getTitle(), getTextChannel(), this.disclosePublicly).submit();
			// Submits the results to the author

			if (message.getAuthor().getIdLong() == message.getJDA().getSelfUser().getIdLong()) {
				List<MessageEmbed> embeds = message.getEmbeds();
				if (embeds.size() == 1 && isPoll(embeds.get(0))) {
					message
					    .editMessage(generateEmbed(this.title, this.description, convertChoices(newChoices),
					        getAuthor(), this.endsOn, true))
					    .queue();
				}

			}
			// Changes the poll message to show that it has ended

			this.submitted = true;
		}

		/**
		 * Checks if the poll is still valid (if it can still be submitted)
		 *
		 * @return true if this poll is still valid, false if not
		 */
		public boolean check() {
			try {
				if (this.submitted)
					return false;

				getAuthor();
				getMessage();

			} catch (Exception e) {
				return false;
			}

			return true;
		}

		@SuppressWarnings("null")
		@Nonnull
		public static String convertChoices(@Nonnull List<Choice> choices) {
			return choices.stream()
			    .map(choice -> MapUtils.getKeyFromMap(EMOJI, choice.getNumber()).get(0) + ": " + choice.getTitle())
			    .collect(Collectors.joining(",\n"));
		}

		public static boolean isPoll(@Nonnull MessageEmbed embed) {
			AuthorInfo author = embed.getAuthor();
			Footer footer = embed.getFooter();
			if (footer == null || author == null)
				return false;
			String name = author.getName();
			String description = embed.getDescription();
			if (name == null || description == null)
				return false;

			return name.startsWith("A poll by ") && description.contains("**Choices:**")
			    && ("This poll ends".equals(footer.getText()) || "This poll has ended".equals(footer.getText()));
		}

		@Nonnull
		private static MessageEmbed generateEmbed(@Nonnull String title, @Nonnull String description,
		                                          @Nonnull String choices, @Nonnull User author, long endsOn) {
			return generateEmbed(title, description, choices, author, endsOn, false);
		}

		@Nonnull
		private static MessageEmbed generateEmbed(@Nonnull String title, @Nonnull String description,
		                                          @Nonnull String choices, @Nonnull User author, long endsOn,
		                                          boolean forceEnd) {
			long now = System.currentTimeMillis();
			boolean ended = now >= endsOn || forceEnd;
			return new EmbedBuilder().setTitle(title)
			    .setDescription(description + "\n**Choices:**\n" + choices)
			    .setColor(ended ? Constants.DISABLED : Constants.LITHIUM)
			    .setAuthor("A poll by " + author.getName(), author.getAvatarUrl())
			    .setFooter(ended ? "This poll has ended" : "This poll ends", null)
			    .setTimestamp(ended && now < endsOn ? Instant.ofEpochMilli(now) : Instant.ofEpochMilli(endsOn))
			    .build();
		}

	}

	@SuppressWarnings("null")
	@Nonnull
	public static String formatChoices(@Nonnull List<Choice> choices) {
		return choices.stream()
		    .map(ch -> MapUtils.getKeyFromMap(EMOJI, ch.getNumber()).get(0) + ": _" + ch.getTitle() + "_")
		    .collect(Collectors.joining(",\n"));
	}

	private static boolean shouldMentionEveryone(@Nonnull Guild guild, @Nonnull Member author,
	                                             @Nonnull EventWaiter ew) {
		return guild.getSelfMember().hasPermission(Permission.MESSAGE_MENTION_EVERYONE)
		    && author.hasPermission(Permission.MESSAGE_MENTION_EVERYONE)
		    && ew.getBoolean(BotUtils.buildEmbed(null,
		        "Finally, do you want " + BotData.getName() + " to mention everyone when the poll starts?",
		        Constants.LITHIUM));
	}

	private static boolean shouldDiscloseResultsPublicly(@Nonnull EventWaiter ew, @Nonnull TextChannel publishChannel) {
		return ew.getBoolean(BotUtils.buildEmbed(null,
		    "Do you want send the poll result to "
		        + publishChannel.getAsMention()
		        + " (& to you)"
		        + "? If you choose ❎, the poll results will only be sent to you.",
		    Constants.LITHIUM));
	}

	private static boolean shouldAllowMoreVotes(@Nonnull EventWaiter ew) {
		return ew.getBoolean(BotUtils.buildEmbed(null,
		    "Do you want users to be able to cast a vote on multiple choices? Recommended: "
		        + Constants.DENY_EMOJI
		        + ".",
		    Constants.LITHIUM));
	}

	private static int getDurationInDays(@Nonnull MessageChannel channel, @Nonnull EventWaiter ew) {
		channel.sendMessage(BotUtils.buildEmbed(null,
		    "Now that you're done with adding choices, let's choose the right duration for your poll. "
		        + "Please enter the duration for your poll **in days** (the duration cap is "
		        + DURATION_DAYS_CAP
		        + " days):_",
		    Constants.LITHIUM)).queue();

		while (true) {
			String response = ew.getString();
			if ("exit".equalsIgnoreCase(response))
				throw new CanceledException();

			int resultDays;
			try {
				resultDays = Parser.parseInt(response);
			} catch (NumberFormatException e) {
				channel.sendMessage(BotUtils.buildEmbed(null, "Please enter a **number**!", Constants.FAILURE)).queue();
				continue;

			} catch (NumberOverflowException e) {
				channel.sendMessage(BotUtils.buildEmbed(null, "I do not accept numbers _that_ big.", Constants.FAILURE))
				    .queue();
				continue;
			}

			if (resultDays == 0) {
				channel.sendMessage(BotUtils.buildEmbed(null,
				    "Poll that would expire the moment it would be created would be pretty pointless, don't you think?",
				    Constants.FAILURE)).queue();
				continue;
			}

			if (resultDays < 0) {
				channel.sendMessage(BotUtils.buildEmbed(null,
				    "I'm sorry, but "
				        + BotData.getName()
				        + "'s time travel module is currently unavailable. Please try again in `-4` days!",
				    Constants.FAILURE)).queue();
				continue;
			}

			if (resultDays > DURATION_DAYS_CAP) {
				channel.sendMessage(BotUtils.buildEmbed(null,
				    "Please enter something below the limit. Current limit is " + DURATION_DAYS_CAP + " days!",
				    Constants.FAILURE)).queue();
				continue;
			}

			return resultDays;
		}
	}

	@Nonnull
	private static TextChannel getTextChannel(@Nonnull Guild guild, @Nonnull MessageChannel channel,
	                                          @Nonnull EventWaiter ew) {
		channel
		    .sendMessage(
		        BotUtils
		            .buildEmbed(null,
		                "Good, let's proceed. Please mention the channel you want to post the poll in.\n\n"
		                    + "_Protip: you can mention a channel with #(channel-name)_.",
		                Constants.LITHIUM))
		    .queue();

		while (true) {
			Message resp = ew.getMessage();

			if (resp.getContentStripped().equalsIgnoreCase("exit"))
				throw new CanceledException();

			List<TextChannel> mentioned = resp.getMentionedChannels();

			if (mentioned.isEmpty()) {
				List<TextChannel> guildChannels = guild.getTextChannels();
				channel.sendMessage(BotUtils.buildEmbed(null,
				    "I couldn't find any mentioned channels there!\n\n_Protip: Mention a channel like this: "
				        + (guildChannels.isEmpty() ? "#(your-channel)" : "<#" + channel.getId() + ">")
				        + "!_",
				    Constants.FAILURE)).queue();
				continue;
			}

			TextChannel resultChannel = mentioned.get(0);
			if (resultChannel.getGuild().getIdLong() != guild.getIdLong()) {
				channel.sendMessage(BotUtils.buildEmbed(null, "There's no way I'm posting there.", Constants.FAILURE))
				    .queue();
				continue;
			}

			if (!resultChannel.canTalk()) {
				channel.sendMessage(BotUtils.buildEmbed(null,
				    "Please grant me the 'Send messages' & 'Read messages' permissions for this channel first!",
				    Constants.FAILURE)).queue();
				continue;
			}

			return resultChannel;
		}
	}

	@Nonnull
	private static List<Choice> getChoices(@Nonnull MessageChannel channel, @Nonnull EventWaiter ew) {
		List<Choice> choices = new ArrayList<>();
		channel.sendMessage(BotUtils.buildEmbed(null,
		    "Great! Let's get started with options for this poll (you can add up to 10). "
		        + "Start typing in option names (eg. \"Broccoli\", \"Pizza\", \"Burger\"), one at a time:",
		    Constants.LITHIUM)).queue();

		boolean done = false;
		while (!done) {
			if (choices.size() == 10) {
				done = true;
				continue;
			}

			String response = ew.getString();
			if ("exit".equalsIgnoreCase(response)) {
				throw new CanceledException();

			} else if ("done".equalsIgnoreCase(response)) {
				if (choices.size() < 2) {
					channel.sendMessage(BotUtils.buildEmbed(null, "Add at least 2 choices!", Constants.FAILURE))
					    .queue();
					continue;
				}

				done = true;
				continue;
			}

			if (response.length() > CHOICE_DESCRIPTION_LENGTH_CAP) {
				channel.sendMessage("Sorry, but the length of the choice description may not exceed "
				    + CHOICE_DESCRIPTION_LENGTH_CAP
				    + " characters!").queue();
				continue;
			}
			choices.add(new Choice(choices.isEmpty() ? 0 : choices.get(choices.size() - 1).getNumber() + 1, response));

			channel.sendMessage(BotUtils.buildEmbed("Current choices:",
			    formatChoices(choices) + "\n\n_Protip: type in `DONE` when you're done adding choices to this poll._",
			    Constants.LITHIUM)).queue();

		}

		return choices;
	}

	@Nonnull
	private static String getDescription(@Nonnull MessageChannel channel, @Nonnull EventWaiter ew) {
		channel
		    .sendMessage(BotUtils.buildEmbed("Okay. Please proceed with the description (the main question if you will)"
		        + "of your poll (eg. \"What's your favorite food?\"):", Constants.LITHIUM))
		    .queue();

		while (true) {
			String response = ew.getString();
			if (response.equalsIgnoreCase("exit"))
				throw new CanceledException();

			if (response.length() > DESCRIPTION_CAP) {
				channel
				    .sendMessage(
				        "Sorry, but the length of the description may not exceed " + DESCRIPTION_CAP + " characters!")
				    .queue();
				continue;
			}

			return response;
		}
	}

	@Nonnull
	private static String getTitle(@Nonnull MessageChannel channel, @Nonnull EventWaiter ew) {
		channel
		    .sendMessage(
		        BotUtils
		            .buildEmbed("Welcome to LiBot's poll-creator 2000!",
		                "Please type in the title for your poll (eg. \"The food poll!\"):"
		                    + " \n\n_Protip: You can always type in `EXIT` to exit!_",
		                Constants.LITHIUM))
		    .queue();

		while (true) {
			String response = ew.getString();
			if (response.equalsIgnoreCase("exit"))
				throw new CanceledException();

			if (response.length() > MessageEmbed.TITLE_MAX_LENGTH) {
				channel.sendMessage("Sorry, but the length of the title may not exceed "
				    + MessageEmbed.TITLE_MAX_LENGTH
				    + " characters!").queue();
				continue;
			}

			return response;
		}
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		Member member = event.getMember();
		if (member == null)
			throw new UnpredictedStateException();

		if (params.check()) {
			String parameter = params.get(0);

			Poll poll = ProviderManager.POLL.getData().get(event.getGuild().getIdLong());
			switch (parameter) {
				case "delete":
					if (!ProviderManager.POLL.getData().containsKey(event.getGuild().getIdLong())) {
						throw new CommandException("No poll is active on this guild", false);
					}

					RestAction<Void> delete = null;
					try {
						delete = poll.getMessage().delete();
					} catch (Exception e) {}

					ProviderManager.POLL.remove(event.getGuild());

					if (delete != null)
						delete.queue();

					event.getChannel()
					    .sendMessage(
					        BotUtils.buildEmbed("Success", "Poll has been successfully canceled!", Constants.SUCCESS))
					    .queue();
					return;

				case "end":
					if (!ProviderManager.POLL.getData().containsKey(event.getGuild().getIdLong())) {
						throw new CommandException("No poll is active on this guild", false);
					}

					if (!poll.check()) {
						throw new CommandException(
						    "For some reason, current poll could not be ended. Please delete it with `"
						        + BotUtils.getCommandPrefix(event.getGuild())
						        + "poll delete`.",
						    false);
					}

					poll.forceSubmit();

					ProviderManager.POLL.remove(event.getGuild());

					event.getChannel()
					    .sendMessage(
					        BotUtils.buildEmbed("Success", "Poll has been successfully ended!", Constants.SUCCESS))
					    .queue();

					return;

				default:
					break;
			}

		}

		if (ProviderManager.POLL.getData().containsKey(event.getGuild().getIdLong())) {
			throw new CommandException("You can only have 1 poll active at a time. Cancel the current poll with `"
			    + BotUtils.getCommandPrefix(event.getGuild())
			    + "poll cancel` or end it now with `"
			    + BotUtils.getCommandPrefix(event.getGuild())
			    + "poll end`", false);
		}

		EventWaiter ew = new EventWaiter(event.getAuthor(), event.getChannel());

		String title = getTitle(event.getChannel(), ew);
		String description = getDescription(event.getChannel(), ew);
		List<Choice> choices = getChoices(event.getChannel(), ew);
		int durationInDays = getDurationInDays(event.getChannel(), ew);
		TextChannel publishChannel = getTextChannel(event.getGuild(), event.getChannel(), ew);
		boolean allowMoreVotes = shouldAllowMoreVotes(ew);
		boolean discloseResultsPublicly = shouldDiscloseResultsPublicly(ew, publishChannel);
		boolean mentionEveryone = shouldMentionEveryone(event.getGuild(), member, ew);

		EmbedBuilder builder = new EmbedBuilder(BotUtils.buildEmbed("Poll review", "", Constants.SUCCESS));
		builder.addField("Title", title, true);
		builder.addField("Description", description, true);
		builder.addField("Choices", formatChoices(choices), true);
		builder.addField("Duration", durationInDays + " days", true);
		builder.addField("Allows more votes per user?", allowMoreVotes ? "Yes (not recommended)" : "No", true);
		builder.addField("Will mention everyone on start?", mentionEveryone ? "Yes" : "No", true);
		builder.addField("Will the results be disclosed publicly?", discloseResultsPublicly ? "Yes" : "No", true);

		Message confirm = event.getChannel().sendMessage(builder.build()).complete();

		if (!ew.getBoolean(BotUtils.buildEmbed(null,
		    "Are you sure you want to post this poll? You can always cancel it with `"
		        + BotUtils.getCommandPrefix(event.getGuild())
		        + "poll delete` or end it early with `"
		        + BotUtils.getCommandPrefix(event.getGuild())
		        + "poll end`.",
		    Constants.SUCCESS))) {
			confirm.delete().queue();
			return;
		}

		confirm.delete().queue();
		ProviderManager.POLL.putPoll(event.getGuild(),
		    new Poll(publishChannel, choices, event.getAuthor(), title, description,
		        TimeUnit.DAYS.toMillis(durationInDays), allowMoreVotes, mentionEveryone, discloseResultsPublicly));
	}

	@Override
	public String getInfo() {
		return "Lets you create a public poll with up to 10 choices. "
		    + "When poll is ended, you will receive the results via direct messages."
		    + "\nOptions for the <switch> parameter:\n"
		    + "\"DELETE\" - cancels current poll,\n\"END\" - ends current poll early.";
	}

	@Override
	public String getName() {
		return "Poll";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public boolean pausesThread() {
		return true;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.MANAGE_SERVER);
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("switch (optional, see command's description for more info)");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

}
