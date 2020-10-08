package com.mz.libot.commands.games;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.exceptions.runtime.NumberOverflowException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;
import com.mz.libot.utils.Parser;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GuessANumberCommand extends Command {

	private static final int MAX_HISTORY = 24;

	private static void postStatus(MessageChannel channel, ArrayList<Integer> history, int newGuess, int number) {
		if (history.size() > MAX_HISTORY)
			history.remove(0);

		String result = "Hmm.. My number is "
		    + (newGuess > number ? "lower" : "higher")
		    + " than your guess!\n\nYour guesses:\n"
		    + history.stream()
		        .map(guess -> guess + " - " + (guess > number ? "lower" : "higher"))
		        .collect(Collectors.joining("\n"))
		    + "\n**"
		    + newGuess
		    + " - "
		    + (newGuess > number ? "lower**" : "higher**")
		    + "\nType in your guess or ``EXIT`` to exit!";

		channel.sendMessage(BotUtils.buildEmbed("Guess a number", result, Constants.LITHIUM)).complete();

	}

	public static void guessNum(User author, MessageChannel channel, Parameters params) {
		int maxNum = 100;
		if (params.check()) {
			if (!NumberUtils.isParsable(params.get(0))) {
				throw new CommandException("The max number parameter must be a number.", false);
			}

			maxNum = params.getAsInteger(0);
		}

		if (maxNum < 10) {
			throw new CommandException("The max number parameter must be more than or equal to 10.", false);
		}

		int num = BotUtils.getRandom().nextInt(maxNum) + 1;

		channel
		    .sendMessage(
		        BotUtils.buildEmbed("Guess a number", "Type in your guess or ``EXIT`` to exit!", Constants.LITHIUM))
		    .queue();

		ArrayList<Integer> history = new ArrayList<>();
		EventWaiter ew = new EventWaiter(author, channel);
		while (true) {
			Message guessMsg = ew.getMessage();

			if (guessMsg.getContentDisplay().equalsIgnoreCase("exit")
			    && ew.getBoolean("Are you sure you want to exit this game?")) {
				guessMsg.addReaction(Constants.ACCEPT_EMOJI).queue();
				throw new CanceledException();
			}

			int guess = 0;
			try {
				guess = Parser.parseInt(guessMsg.getContentDisplay());
			} catch (NumberOverflowException | NumberFormatException e) {
				continue;
			}

			if (guess == num) {
				channel.sendMessage(BotUtils.buildEmbed("You won",
				    "My number was " + num + " and it took you " + (history.size() + 1) + " guesses to find it!",
				    Constants.SUCCESS)).queue();
				break;
			}

			String limitClue = null;
			if (guess > maxNum) {
				limitClue = "below or equal to " + maxNum;

			} else if (guess < 0) {
				limitClue = "above 0";
			}

			if (limitClue != null) {
				channel
				    .sendMessage(
				        "No, you're not going to find it there. Rather try guessing numbers **" + limitClue + "**!")
				    .queue();
				continue;
			}

			postStatus(channel, history, guess, num);

			history.add(guess);
		}

	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		guessNum(author, channel, params);
	}

	@Override
	public String getInfo() {
		return "Lets you play a game of \"Guess that number\". In this text-based game, you'll have to blindly guess a number and LiBot will be giving you hints. "
		    + "If max number is not provided, 100 will be picked.";
	}

	@Override
	public String getName() {
		return "GuessANumber";
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
	public String[] getParameters() {
		return Commands.toArray("max number (optional)");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("guessnum");
	}

}
