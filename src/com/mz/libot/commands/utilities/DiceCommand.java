package com.mz.libot.commands.utilities;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class DiceCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		int maxNumber = 6;
		if (params.check())
			maxNumber = params.getAsInteger(0);

		if (maxNumber < 1)
			throw new CommandException("The number of sides must be more than 1", false);

		event.getChannel()
		    .sendMessage(
		        "\uD83C\uDFB2 The dice has rolled on number **" + (BotUtils.getRandom().nextInt(maxNumber) + 1) + "**.")
		    .queue();
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("number of sides");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public String getInfo() {
		return "Rolls a dice. You can configure the number of sides with the first parameter, the default is 6.";
	}

	@Override
	public String getName() {
		return "Dice";
	}

}
