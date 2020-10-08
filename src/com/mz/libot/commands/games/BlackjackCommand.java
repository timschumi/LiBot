package com.mz.libot.commands.games;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.BettableGame;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class BlackjackCommand extends BettableGame {

	/**
	 * Initiates draw
	 * 
	 * @param channel
	 *            channel to send draw message to
	 */
	public static void gameDraw(MessageChannel channel) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Draw!");
		builder.setColor(Constants.DISABLED);
		builder.setDescription("You and the dealer had the same amount of cards!");

		channel.sendMessage(builder.build()).queue();
	}

	/**
	 * Initiates victory with reason
	 * 
	 * @param channel
	 *            channel to send victory message to
	 * @param reason
	 *            reason
	 */
	public static void gameWin(MessageChannel channel, String reason) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("You won!");
		builder.setColor(Constants.SUCCESS);
		builder.setDescription(reason);

		channel.sendMessage(builder.build()).queue();
	}

	/**
	 * Initiates game over with reason
	 * 
	 * @param channel
	 *            channel to send game over message to
	 * @param reason
	 *            reason
	 */
	public static void gameOver(MessageChannel channel, String reason) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("You lost!");
		builder.setColor(Constants.FAILURE);
		builder.setDescription(reason);

		channel.sendMessage(builder.build()).queue();
	}

	/**
	 * Posts current game status
	 * 
	 * @param channel
	 *            channel to post game status to
	 * @param history
	 *            current card history
	 * @param draw
	 *            how many cards you took
	 * @return total cards
	 */
	public static int postStatus(MessageChannel channel, Collection<Integer> history, int draw) {
		int total = draw;
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Game status");
		builder.setColor(Constants.LITHIUM);

		StringBuilder sb = new StringBuilder();
		for (Integer card : history) {
			sb.append("+" + card + "\n");
			total += card;
		}

		builder.appendDescription("Card history:\n"
		    + sb.toString()
		    + "**+"
		    + draw
		    + "\nTOTAL: "
		    + total
		    + "/21**\n\nTo take another card type in ``HIT``, ``STAND`` to let the dealer play or ``EXIT`` to "
		    + (history.size() == 1 ? "surrender" : "exit"));
		history.add(draw);

		channel.sendMessage(builder.build()).queue();
		return total;
	}

	/**
	 * Plays dealer's AI
	 * 
	 * @param channel
	 *            channel to post final results to
	 * @return dealer's total cards
	 */
	public static int playDealer(MessageChannel channel) {
		StringBuilder history = new StringBuilder();

		int total = 0;
		while (total < 17) {
			int next = BotUtils.getRandom().nextInt(11) + 1;
			if (next > 10)
				next = 10;

			total += next;
			history.append("+" + next + "\n");
		}

		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Constants.LITHIUM);
		builder.setTitle("Dealer");
		builder.setDescription("Dealer's card history:\n" + history.toString() + "**TOTAL: " + total + "/21**");

		channel.sendMessage(builder.build()).queue();

		return total;

	}

	@Override
	public String getName() {
		return "Blackjack";
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
	public GameResult play(GuildMessageReceivedEvent event) {
		List<Integer> history = new ArrayList<>();
		int total = 0;

		total = postStatus(event.getChannel(), history, BotUtils.getRandom().nextInt(11) + 1);

		EventWaiter ew = new EventWaiter(event.getAuthor(), event.getChannel());
		boolean stand = false;
		while (!stand) {
			String said = ew.getString().toLowerCase();

			if (said.equals("hit")) {
				// HIT command
				int next = BotUtils.getRandom().nextInt(11) + 1;
				if (next > 10)
					next = 10;

				total = postStatus(event.getChannel(), history, next);

			} else if (said.equals("exit")) {
				// EXIT command
				if (ew.getBoolean("Are you sure that you want to exit this Blackjack game **(if you have betted, "
				    + (history.size() == 1 ? "only half of your bet will be returned" : "you'll loose your full bet")
				    + "!)**?")) {

					if (history.size() == 1)
						return GameResult.EXITTED;

					return GameResult.LOST;
				}

			} else if (said.equals("stand")) {
				// STAND command
				stand = true;

			}

			if (total > 21) {
				// If you have more than 21 cards
				gameOver(event.getChannel(), "BUSTED! You have more than 21 cards _(" + total + " to be exact)_!");
				return GameResult.LOST;
			}
		}

		int dealer = playDealer(event.getChannel());

		if (dealer > 21) {
			gameWin(event.getChannel(),
			    "Dealer was busted because they had more than 21 cards _(" + dealer + " to be exact)_!");
			return GameResult.WON;

		} else if (total > dealer) {
			gameWin(event.getChannel(), "You are closer to 21 cards than the dealer!");
			return GameResult.WON;

		} else if (dealer > total) {
			gameOver(event.getChannel(), "Dealer is closer to 21 cards than you are!");
			return GameResult.LOST;
		}

		gameDraw(event.getChannel());
		return GameResult.CANCELED;
	}

	@Override
	public String getGameInfo() {
		return "Lets you play a single-player game of Blackjack with LiBot. If you don't know the rules, "
		    + "you can read more about this game [here](https://en.wikipedia.org/wiki/Blackjack).";
	}

}
