package com.mz.libot.commands.games;

import java.util.List;
import java.util.stream.Collectors;

import com.github.markozajc.juno.cards.UnoCard;
import com.github.markozajc.juno.cards.UnoCardColor;
import com.github.markozajc.juno.cards.UnoStandardDeck;
import com.github.markozajc.juno.cards.impl.UnoDrawCard;
import com.github.markozajc.juno.game.UnoControlledGame;
import com.github.markozajc.juno.game.UnoGame;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.players.impl.UnoStrategicPlayer;
import com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules;
import com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules.UnoHouseRule;
import com.github.markozajc.juno.rules.pack.impl.house.UnoProgressiveRulePack;
import com.github.markozajc.juno.utils.UnoRuleUtils;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.BettableGame;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;

public class UnoCommand extends BettableGame {

	private static SnowflakeCacheView<Emote> cache1;
	private static SnowflakeCacheView<Emote> cache2;

	private static final String PILE_EMOTE = "<:Pile:473456020342046730>";

	static void exit(EventWaiter ew) {
		if (ew.getBoolean("Are you sure you want to exit **(if you have betted, you'll loose your full bet!)**?"))
			throw new CanceledException();

	}

	private static class DiscordUnoGame extends UnoControlledGame {

		private final StringBuilder feed;

		@SuppressWarnings("null")
		public DiscordUnoGame(User user, TextChannel channel, StringBuilder feed) {
			super(new DiscordPlayer(channel, user, feed), new UnoStrategicPlayer(BotData.getName()),
			    new UnoStandardDeck(), 7, UnoOfficialRules.getPack(UnoHouseRule.PROGRESSIVE));
			this.feed = feed;
		}

		@Override
		public void onEvent(String format, Object... arguments) {
			this.feed.append(String.format(format, arguments) + "\n");
		}

	}

	private static class DiscordPlayer extends UnoPlayer {

		private static final String UNO_LOGO = "http://www.mattelgames.com/sites/mattel_games/files/styles/game_list_278x278/public/2017-11/featured_uno_2_0.jpg";

		private final EmbedBuilder eb = new EmbedBuilder();
		private final TextChannel channel;
		private final EventWaiter ew;
		private final StringBuilder feed;

		@SuppressWarnings("null")
		public DiscordPlayer(TextChannel channel, User user, StringBuilder feed) {
			super(user.getName());

			this.channel = channel;
			this.ew = new EventWaiter(user, channel);
			this.feed = feed;

			this.eb.setColor(Constants.LITHIUM)
			    .setThumbnail(UNO_LOGO)
			    .setTitle("UNO - You versus " + BotData.getName())
			    .setFooter("Type in EXIT to exit", null);
		}

		@SuppressWarnings("null")
		@Override
		public UnoCard playCard(UnoGame game, UnoPlayer next) {
			this.eb.clearFields();
			this.eb.setDescription("```" + this.feed.toString() + "```");
			this.feed.setLength(0);

			List<UnoCard> possible = UnoRuleUtils.combinedPlacementAnalysis(game.getTopCard(),
			    this.getHand().getCards(), game.getRules(), this.getHand());

			StringBuilder info = new StringBuilder();
			{
				info.append("__Top card: " + getEmoteWithName(game.getTopCard()) + "__");
				info.append("\n"
				    + next.getName()
				    + "'s hand size: "
				    + next.getHand().getSize()
				    + " "
				    + PILE_EMOTE
				    + (next.getHand().getSize() == 1 ? " __**UNO!**__" : ""));
			}

			StringBuilder actions = new StringBuilder();
			{
				List<UnoDrawCard> drawCards = UnoProgressiveRulePack.getConsecutive(game.getDiscard());
				if (!drawCards.isEmpty()) {
					actions.append("0 - Draw **"
					    + drawCards.size() * drawCards.get(0).getAmount()
					    + "** cards from a "
					    + getEmoteWithName(game.getTopCard()));

				} else {
					actions.append("0 - Draw");
				}

				int i = 1;
				for (UnoCard card : this.getCards()) {
					if (possible.contains(card)) {
						actions.append("\n**" + i + "** - " + getEmoteWithName(card) + "");
					} else {
						actions.append("\n" + i + " - " + getEmoteWithName(card));
					}

					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {}

					i++;
				}
			}

			this.eb.addField("Game info", info.toString(), false);
			this.eb.addField("Choose your action", actions.toString(), false);

			this.channel.sendMessage(this.eb.build()).queue();

			while (true) {
				String response = this.ew.getString();
				if (response.toLowerCase().equals("exit"))
					exit(this.ew);

				int choice;

				try {
					choice = Integer.parseInt(response);
				} catch (NumberFormatException e) {
					continue;
				}

				if (choice == 0)
					return null;

				if (choice > getCards().size()) {
					this.channel.sendMessage("Invalid choice!").queue();
					continue;
				}

				if (choice < 0) {
					this.channel.sendMessage("Invalid choice!").queue();
					continue;
				}

				UnoCard card = this.getCards().get(choice - 1);

				if (!possible.contains(card)) {
					this.channel.sendMessage("Invalid choice!").queue();
					continue;
				}

				return card;
			}
		}

		@Override
		public UnoCardColor chooseColor(UnoGame game) {
			UnoCard top = game.getDiscard().getTop();

			this.eb.clearFields();
			this.eb.setDescription(this.feed.toString());
			this.feed.setLength(0);

			StringBuilder info = new StringBuilder();
			info.append("__Top card: " + getEmoteWithName(top) + "__");

			this.eb.addField("Your cards",
			    this.getHand().getCards().stream().map(UnoCommand::getEmoteWithName).collect(Collectors.joining(" ")),
			    true);
			this.eb.addField("Game info", info.toString(), true);
			this.eb.addField("Choose a color", "0 - Yellow\n1 - Red\n2 - Green\n3 - Blue", false);

			this.channel.sendMessage(this.eb.build()).queue();

			while (true) {
				String response = this.ew.getString();
				if (response.toLowerCase().equals("exit"))
					exit(this.ew);

				int choice;

				try {
					choice = Integer.parseInt(response);
				} catch (NumberFormatException e) {
					continue;
				}

				switch (choice) {
					case 0:
						return UnoCardColor.YELLOW;
					case 1:
						return UnoCardColor.RED;
					case 2:
						return UnoCardColor.GREEN;
					case 3:
						return UnoCardColor.BLUE;
					default:
						break;
				}

				this.channel.sendMessage("Invalid choice!").queue();
			}
		}

		@Override
		public boolean shouldPlayDrawnCard(UnoGame game, UnoCard drawnCard, UnoPlayer next) {
			return this.ew.getBoolean(BotUtils.buildEmbed(
			    "You draw a " + getEmoteWithName(drawnCard) + ". Do you want to place it?", Constants.LITHIUM));

		}

	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.GAMES;
	}

	@Override
	public String getName() {
		return "Uno";
	}

	@Override
	public GameResult play(GuildMessageReceivedEvent event) {
		StringBuilder feed = new StringBuilder();
		UnoGame game = new DiscordUnoGame(event.getAuthor(), event.getChannel(), feed);

		UnoPlayer winner;
		try {
			winner = game.playGame();
			if (winner != null)
				feed.append(winner.getName() + " won!");

			if (winner instanceof DiscordPlayer) {
				event.getChannel()
				    .sendMessage(BotUtils.buildEmbed("You won!", "```" + feed.toString() + "```", Constants.SUCCESS))
				    .queue();
				return GameResult.WON;

			} else if (winner instanceof UnoStrategicPlayer) {
				event.getChannel()
				    .sendMessage(BotUtils.buildEmbed("You lost.", "```" + feed.toString() + "```", Constants.FAILURE))
				    .queue();
				return GameResult.LOST;

			} else {
				event.getChannel()
				    .sendMessage(BotUtils.buildEmbed(
				        "Both discard and draw piles were emptied, the player with least cards wins.\nBoth players has the same amount of cards.",
				        Constants.DISABLED))
				    .queue();
				return GameResult.CANCELED;
			}
		} catch (CanceledException ce) {
			return GameResult.LOST;
		}
	}

	@Override
	public String getGameInfo() {
		return "Lets you play a game of UNO with the official rules + Progressive Uno rule"
		    + " using the original deck of 108 cards. Read more about UNO here: https://en.wikipedia.org/wiki/Uno_(card_game).\n"
		    + "Implemented using [JUNO](https://github.com/markozajc/JUNO).";
	}

	@SuppressWarnings("null")
	static String getEmoteWithName(UnoCard card) {
		if (cache1 == null) {
			Guild cacheGuild = BotData.getJDA().getGuildById("473450258517721100");
			if (cacheGuild != null) {
				cache1 = cacheGuild.getEmoteCache();
			}
		}
		if (cache1 != null) {
			List<Emote> result = cache1.getElementsByName(card.toString().replace(" ", ""), false);
			if (!result.isEmpty())
				return result.get(0).getAsMention() + " " + card;
		}

		if (cache2 == null) {
			Guild cacheGuild = BotData.getJDA().getGuildById("473453015223762973");
			if (cacheGuild != null) {
				cache2 = cacheGuild.getEmoteCache();
			}
		}
		if (cache2 != null) {
			List<Emote> result = cache2.getElementsByName(card.toString().replace(" ", ""), false);
			if (!result.isEmpty())
				return result.get(0).getAsMention() + " " + card;
		}

		return null;
	}

}
