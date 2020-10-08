package com.mz.libot.commands.money;

import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.ratelimits.Ratelimits;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MoneyCommand extends Command {

	private static final Ratelimits HOURLY_REWARD_RATELIMIT = new Ratelimits(3600);
	private static final int MIN_WAGE = 5;

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Constants.LITHIUM);

		builder.setDescription(
		    "Your current balance is: **" + ProviderManager.MONEY.getBalance(event.getAuthor()) + "Ł**");

		long remainingTime = HOURLY_REWARD_RATELIMIT.check(event.getAuthor().getIdLong());
		long earned = -1;
		if (remainingTime == -1) {
			earned = calculateEarning(event);

			ProviderManager.MONEY.addMoney(event.getAuthor(), earned);
			HOURLY_REWARD_RATELIMIT.register(event.getAuthor().getIdLong());
			builder.appendDescription(
			    " + " + earned + "Ł (hourly reward).\n\nYou can claim your next hourly reward in **60** minutes.");

		} else {
			long remainingSeconds = remainingTime / 1000;
			String time = formatRemainingTime(remainingSeconds);

			builder.appendDescription(".\n\nYou can claim your next hourly reward in " + time + ".");
		}

		event.getChannel().sendMessage(builder.build()).queue();
	}

	private static String formatRemainingTime(long remainingSeconds) {
		String time;
		if (remainingSeconds > 60) {
			time = "**" + remainingSeconds / 60 + "** minute(s)";

		} else {
			time = "**" + remainingSeconds + "** second(s)";
		}
		return time;
	}

	private static long calculateEarning(GuildMessageReceivedEvent event) {
		long earned;
		long current = ProviderManager.MONEY.getBalance(event.getAuthor());
		long earnedUnmodified = Math.round(Math.sqrt(current));

		if (earnedUnmodified > MIN_WAGE) {
			earned = earnedUnmodified;

		} else {
			earned = MIN_WAGE;
		}
		return earned;
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MONEY;
	}

	@Override
	public String getInfo() {
		return "Lets you view your current balance and lets you claim your hourly reward.";
	}

	@Override
	public String getName() {
		return "Money";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("balance", "work", "reward");
	}

}
