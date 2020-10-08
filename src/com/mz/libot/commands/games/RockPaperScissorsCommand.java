package com.mz.libot.commands.games;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.BettableGame;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RockPaperScissorsCommand extends BettableGame {

	private static final int ROCK = 0;
	private static final int PAPER = 1;
	private static final int SCISSORS = 2;

	/**
	 * Indicates that the player won
	 * 
	 * @param ai
	 * @param human
	 * @param channel
	 */
	public static void gameWin(int ai, int human, MessageChannel channel) {
		MessageEmbed message = BotUtils.buildEmbed("You won!",
		    "_Rock..\nPaper..\nScissors.._\nYou choose **"
		        + getName(human)
		        + "**\nLiBot chooses **"
		        + getName(ai)
		        + "**\n\nYou won!",
		    Constants.SUCCESS);

		channel.sendMessage(message).queue();
	}

	/**
	 * Indicates a draw
	 * 
	 * @param ai
	 * @param human
	 * @param channel
	 */
	public static void gameDraw(int ai, int human, MessageChannel channel) {
		MessageEmbed message = BotUtils.buildEmbed("Draw!",
		    "_Rock..\nPaper..\nScissors.._\n\nYou choose **"
		        + getName(human)
		        + "**\nLiBot chooses **"
		        + getName(ai)
		        + "**\n\nIt's a draw!",
		    Constants.DISABLED);

		channel.sendMessage(message).queue();
	}

	/**
	 * Indicates that the player lost
	 * 
	 * @param ai
	 * @param human
	 * @param channel
	 */
	public static void gameLose(int ai, int human, MessageChannel channel) {
		MessageEmbed message = BotUtils.buildEmbed("You lost!",
		    "_Rock..\nPaper..\nScissors.._\n\nYou choose **"
		        + getName(human)
		        + "**\nLiBot chooses **"
		        + getName(ai)
		        + "**\n\nLiBot won!",
		    Constants.FAILURE);

		channel.sendMessage(message).queue();
	}

	/**
	 * @param selection
	 *            selection to get name of
	 * @return the name of a selection
	 */
	private static String getName(int selection) {
		switch (selection) {
			case ROCK:
				return "Rock";

			case PAPER:
				return "Paper";

			case SCISSORS:
				return "Scissors";

			default:
				return "Unknown";
		}
	}

	@Override
	public String getName() {
		return "RockPaperScissors";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.GAMES;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("rps");
	}

	@Override
	public GameResult play(GuildMessageReceivedEvent event) {
		EventWaiter ew = new EventWaiter(event.getAuthor(), event.getChannel());

		int human = 4;
		int ai = BotUtils.getRandom().nextInt(3);

		while (human == 4) {
			event.getChannel()
			    .sendMessage("Please pick between **ROCK**, **PAPER**, **SCISSORS**, or **EXIT**.")
			    .queue();

			String reply = ew.getString().toLowerCase();
			if (reply.equals("rock")) {
				human = ROCK;

			} else if (reply.equals("paper")) {
				human = PAPER;

			} else if (reply.equals("scissors")) {
				human = SCISSORS;

			} else if (reply.equals("exit")) {
				return GameResult.CANCELED;
			}
		}

		if (human == ROCK) {
			if (ai == ROCK) {
				gameDraw(ai, human, event.getChannel());
				return GameResult.CANCELED;

			} else if (ai == PAPER) {
				gameLose(ai, human, event.getChannel());
				return GameResult.LOST;

			} else {
				gameWin(ai, human, event.getChannel());
				return GameResult.WON;

			}

		} else if (human == PAPER) {
			if (ai == ROCK) {
				gameWin(ai, human, event.getChannel());
				return GameResult.WON;

			} else if (ai == PAPER) {
				gameDraw(ai, human, event.getChannel());
				return GameResult.CANCELED;

			} else {
				gameLose(ai, human, event.getChannel());
				return GameResult.LOST;

			}

		} else {
			if (ai == ROCK) {
				gameLose(ai, human, event.getChannel());
				return GameResult.LOST;

			} else if (ai == PAPER) {
				gameWin(ai, human, event.getChannel());
				return GameResult.WON;

			} else {
				gameDraw(ai, human, event.getChannel());
				return GameResult.CANCELED;

			}

		}
	}

	@Override
	public String getGameInfo() {
		return "Plays a game of \"Rock Paper Scissors\" with LiBot.";
	}

}
