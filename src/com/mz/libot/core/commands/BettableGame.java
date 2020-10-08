package com.mz.libot.core.commands;

import com.mz.libot.commands.administrative.DeclutterCommand;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.processes.CommandProcess;
import com.mz.libot.core.processes.ProcessManager;
import com.mz.libot.utils.Parser;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class BettableGame extends Command {

	protected enum GameResult {
		/**
		 * Full bet will be returned
		 */
		CANCELED,

		/**
		 * Half the bet will be returned
		 */
		EXITTED,

		/**
		 * No money will be returned
		 */
		LOST,

		/**
		 * Double the bet will be returned
		 */
		WON
	}

	public abstract GameResult play(GuildMessageReceivedEvent event);

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		long bet = 0;
		if (params.check(1)) {
			String betString = params.get(0);
			bet = Parser.parseLong(betString);

			if (bet < 0)
				throw new CommandException("Your bet must be positive!", Constants.FAILURE, false);

			if (bet > ProviderManager.MONEY.getBalance(event.getAuthor()))
				throw new CommandException("You don't have that much money! Check your balance with `"
				    + BotUtils.getCommandPrefix(event.getGuild())
				    + "money`.", Constants.FAILURE, false);

			CommandProcess another = ProcessManager.getProcesses()
			    .stream()
			    .filter(cp -> cp.getAdditionalData() != null && !"bettable={money}".equals(cp.getAdditionalData())
			        && cp.getAdditionalData().contains("bettable=")
			        && cp.getAuthor().getIdLong() == event.getAuthor().getIdLong())
			    .findAny()
			    .orElse(null);

			if (another != null && another.getChannel() == null) {
				DeclutterCommand.performCleanup(null);

			} else if (another != null) {
				throw new CommandException("\uD83D\uDED1 You may not have more than 1 bet going at a time."
				    + "Your current game you have betted on is running in "
				    + another.getChannel().getAsMention(), Constants.FAILURE, false);
			}

			ProviderManager.MONEY.retractMoney(event.getAuthor(), bet);

			Thread ct = Thread.currentThread();
			ct.setName(ct.getName().replace("bettable={money}", "bettable=" + bet));
		}

		GameResult result = play(event);

		if (bet != 0) {
			long earned = 0;
			switch (result) {
				case EXITTED:
					earned = bet / 2;
					event.getChannel()
					    .sendMessage(BotUtils.buildEmbed("You've lost half of the bet (" + (bet - earned) + " Ł).",
					        Constants.DISABLED))
					    .queue();
					break;

				case CANCELED:
					earned = bet;
					event.getChannel()
					    .sendMessage(
					        BotUtils.buildEmbed("Your bet has been returned (" + bet + " Ł).", Constants.DISABLED))
					    .queue();
					break;

				case WON:
					earned = bet * 2;
					event.getChannel()
					    .sendMessage(BotUtils.buildEmbed("You've earned **" + bet + " Ł**!", Constants.SUCCESS))
					    .queue();

					break;

				case LOST:
					event.getChannel()
					    .sendMessage(BotUtils.buildEmbed("You've lost **" + bet + " Ł**.", Constants.FAILURE))
					    .queue();

					break;
			}

			ProviderManager.MONEY.addMoney(event.getAuthor(), earned);

		}
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.GAMES;
	}

	public abstract String getGameInfo();

	@Override
	public String getInfo() {
		return getGameInfo()
		    + "\n**You can bet your LiBot cash on this game.** If you win, "
		    + "you get double the betted amount of money, and if you lose, you don't get back anything."
		    + "If don't want to bet anything, run the command without parameters."
		    + "\nNote that LiBot cash (Ł) can in no way be transfered into real money,"
		    + " or be used to purchase real goods.";
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("bet (leave empty to play without betting any Ł)");
	}

	@Override
	public String getAdditionalData() {
		return "bettable={money}";
	}

}
