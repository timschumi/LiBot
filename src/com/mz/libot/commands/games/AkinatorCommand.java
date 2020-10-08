package com.mz.libot.commands.games;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.markozajc.akiwrapper.Akiwrapper;
import com.markozajc.akiwrapper.Akiwrapper.Answer;
import com.markozajc.akiwrapper.AkiwrapperBuilder;
import com.markozajc.akiwrapper.core.entities.Guess;
import com.markozajc.akiwrapper.core.entities.Question;
import com.markozajc.akiwrapper.core.entities.Server.GuessType;
import com.markozajc.akiwrapper.core.entities.Server.Language;
import com.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import com.markozajc.akiwrapper.core.exceptions.ServerUnavailableException;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;
import com.mz.utils.FormatAs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AkinatorCommand extends Command {

	public static final Predicate<String> IS_ANSWER_YES = s -> s.equals("yes") || s.equals("y");
	public static final Predicate<String> IS_ANSWER_NO = s -> s.equals("no") || s.equals("n");
	public static final Predicate<String> IS_ANSWER_IDK =
		s -> s.equals("don't know") || s.equals("dont know") || s.equals("dk");
	public static final Predicate<String> IS_ANSWER_PROB = s -> s.equals("probably") || s.equals("p");
	public static final Predicate<String> IS_ANSWER_PROB_NO = s -> s.equals("probably not") || s.equals("pn");
	public static final Predicate<String> IS_BACK = s -> s.equals("back") || s.equals("b");

	private static Answer getAnswer(String answerString) {
		if (IS_ANSWER_YES.test(answerString))
			return Answer.YES;
		else if (IS_ANSWER_NO.test(answerString))
			return Answer.NO;
		else if (IS_ANSWER_IDK.test(answerString))
			return Answer.DONT_KNOW;
		else if (IS_ANSWER_PROB.test(answerString))
			return Answer.PROBABLY;
		else if (IS_ANSWER_PROB_NO.test(answerString))
			return Answer.PROBABLY_NOT;

		return null;
	}

	private static boolean reviewGuess(EventWaiter ew, Guess guess, MessageChannel channel) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Constants.LITHIUM);
		builder.addField("Name", guess.getName(), true);

		if (guess.getDescription() != null)
			builder.addField("Description", guess.getDescription(), true);

		URL image = guess.getImage();
		if (image != null)
			builder.setImage(image.toString());

		channel.sendMessage(builder.build()).queue();

		return ew.getBoolean("Is that your character?");
	}

	private static void finish(boolean win, MessageChannel channel) {
		if (win) {
			channel
				.sendMessage(BotUtils.buildEmbed("Great!", "Guessed right one more time. I love playing with you!",
												 Constants.SUCCESS))
				.queue();
		} else {
			channel.sendMessage(BotUtils.buildEmbed("Bravo!", "You defeated me.", Constants.DISABLED)).queue();
		}
	}

	public static void akinator(User author, TextChannel channel, Parameters params) {
		Language language = null;

		if (params.check()) {
			List<Language> languages = new ArrayList<>(Arrays.asList(Language.values()));
			// Fetches all available languages.

			Language matching =
				languages.stream().filter(l -> l.toString().equalsIgnoreCase(params.get(0))).findAny().orElse(null);

			if (matching == null)
				throw new CommandException("Unsupported language",
										   "Sorry, that language isn't supported. Try" + languages.stream()
											   .map(l -> FormatAs.getFirstUpper(l.toString().toLowerCase()))
											   .collect(Collectors.joining("\n\u2022 ", "\n\u2022 ", "")),
										   Constants.DISABLED, false);

			language = matching;

		} else {
			language = Language.ENGLISH;
		}

		channel.sendTyping().queue();

		Akiwrapper aw = null;
		try {
			aw = new AkiwrapperBuilder().setFilterProfanity(!channel.isNSFW())
				.setLanguage(language)
				.setGuessType(GuessType.CHARACTER)
				.build();
		} catch (ServerUnavailableException | ServerNotFoundException e) {
			throw new CommandException("Currently, all Akinator's servers for language " +
				FormatAs.getFirstUpper(language.toString()) +
				" are unavailable.\n**Suggestions:**\n\u2022 Wait 1-3 minutes and then try again,\n\u2022 Use another language,\n\u2022 ~~When in the game, answer with 'debug'~~",
									   Constants.DISABLED, false);
		}

		List<String> rejected = new ArrayList<>();

		EventWaiter ew = new EventWaiter(author, channel, 120);
		try {
			for (Question question = aw.getCurrentQuestion(); question != null; question = aw.getCurrentQuestion()) {
				channel.sendMessage(BotUtils
					.buildEmbed("Question #" + (question.getStep() + 1),
								question.getQuestion() + (question.getStep() == 0
									? "\n\nAnswer with , **N** (no), **Y** (yes), **DK** (don't know), " +
										"**P** (probably), **PN** (probably not) or **B** (back)." +
										"\nType in `exit` to exit the game."
									: ""),
								Constants.LITHIUM))
					.queue();

				boolean answered = false;
				while (!answered) {
					String answerString = ew.getString().toLowerCase();

					if (answerString.equals("exit") && ew.getBoolean("Are you sure you want to exit?"))
						throw new CanceledException();

					if (answerString.equals("debug")) {
						channel
							.sendMessage(BotUtils.buildEmbed("Debug information",
															 "**Current API server:** " + aw.getServer().getUrl() +
																 "\n**Current guess count:** " +
																 aw.getGuesses().size(),
															 Constants.SUCCESS))
							.queue();

						continue;
					}

					if (IS_BACK.test(answerString)) {
						if (question.getStep() == 0)
							channel.sendMessage("Sorry, going to the 0th question is currently not supported!").queue();

						aw.undoAnswer();
						answered = true;
						continue;
					}

					Answer answer = getAnswer(answerString);
					if (answer == null) {
						channel.sendMessage(BotUtils
							.buildEmbed("Answer with , **N** (no), **Y** (yes), **DK** (don't know), **P** (probably), **PN** (probably not) or **B** (back).\n" +
								"Type in `exit` to exit the game.", Constants.WARN))
							.queue();
						continue;
					}

					aw.answerCurrentQuestion(answer);

					answered = true;
					// Answers the question
				}

				boolean validQuestion = false;
				List<Guess> guesses = aw.getGuessesAboveProbability(0.85d);
				if (!guesses.isEmpty()) {
					for (Guess guess : guesses) {
						if (!rejected.contains(guess.getId())) {
							if (reviewGuess(ew, guess, channel)) {
								// Checks if this guess complies with the conditions
								finish(true, channel);
								throw new CanceledException();
							}

							validQuestion = true;

							rejected.add(guess.getId());
							// Registers this guess as declined
						}

					}

					if (validQuestion && !ew.getBoolean("Continue?")) {
						finish(false, channel);
						throw new CanceledException();
					}
				}
				// Checks if there are any available guesses
			}
		} catch (ServerUnavailableException e) {
			throw new CommandException("Looks like the Akinator server you were playing on has just died..",
									   Constants.DISABLED, false);
		}

		for (Guess guess : aw.getGuesses()) {
			if (reviewGuess(ew, guess, channel)) {
				// Reviews all final guesses
				finish(true, channel);
				break;
			}
		}

		finish(false, channel);
		// Loses if no guess is accepted
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		TextChannel channel = event.getChannel();
		User author = event.getAuthor();

		akinator(author, channel, params);
	}

	@Override
	public String getInfo() {
		return "Lets you challenge Akinator.\nImplemented using [Akiwrapper](https://github.com/markozajc/Akiwrapper).";
	}

	@Override
	public String getName() {
		return "Akinator";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.GAMES;
	}

	@Override
	public boolean pausesThread() {
		return true;
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("language");
	}

}
