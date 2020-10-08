package com.mz.libot.commands.money;

import java.util.List;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.FinderUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.data.providers.impl.MoneyProviders.MoneyProvider;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class TransferCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		List<User> targets = FinderUtils.findUsers(params.get(1));
		if (targets.isEmpty()) {
			throw new CommandException("Nonexistent user",
			    "User '" + BotUtils.escapeMarkdown(params.get(1)) + "' was not found.", Constants.FAILURE, false);
		}
		targets = FinderUtils.preferFromGuild(targets, event.getGuild());

		int amount = params.getAsInteger(0);
		if (amount < 1)
			throw new CommandException("The amount must be larger than 0!", Constants.FAILURE, false);

		if (amount > ProviderManager.MONEY.getBalance(event.getAuthor())) {
			throw new CommandException("You don't have that much money! Check your balance with `"
			    + BotUtils.getCommandPrefix(event.getGuild())
			    + "money`.", Constants.FAILURE, false);
		}

		long maxTransfer = ProviderManager.MONEY.getBalance(event.getAuthor()) - MoneyProvider.DEFAULT_AMOUNT;
		if (amount > maxTransfer) {
			throw new CommandException("You must not have less than **"
			    + MoneyProvider.DEFAULT_AMOUNT
			    + " Ł** after the transfer; you "
			    + (maxTransfer <= 0 ? "can't transfer any money" : "can transfer at most **" + maxTransfer + " Ł**")
			    + ".", Constants.FAILURE, false);
		}

		User target = targets.get(0);
		if (event.getAuthor().equals(target) || target.isBot())
			throw new CommandException("Invalid recipient!", Constants.FAILURE, false);

		if (!new EventWaiter(event.getAuthor(), event.getChannel())
		    .getBoolean("Are you sure you want to transfer **" + amount + " Ł** to **" + target.getAsTag() + "**?"))
			throw new CanceledException();

		ProviderManager.MONEY.retractMoney(event.getAuthor(), amount);
		ProviderManager.MONEY.addMoney(target, amount);

		event.getChannel()
		    .sendMessage("Transfer successful! " + target.getAsTag() + " will now receive " + amount + " Ł")
		    .queue();
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MONEY;
	}

	@Override
	public String getInfo() {
		return "Transfers an amount of Ł to another user. Transferring Ł can not be undone (unless the other user transfers it back to you).";
	}

	@Override
	public String getName() {
		return "Transfer";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("sendmoney", "donate");
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("amount", "user");
	}

}
