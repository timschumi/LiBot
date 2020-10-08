package com.mz.libot.commands.utilities;

import java.math.RoundingMode;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.udojava.evalex.Expression;
import com.udojava.evalex.Expression.ExpressionException;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CalculatorCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		String expression = params.get(0)
		    .replace('\u2715', '*')
		    .replace('\u2052', '/')
		    .replace(':', '/')
		    .replace('x', '*');
		// Replaces commonly used operation characters with their correct ASCII
		// representation

		try {
			String result = new Expression(expression).eval()
			    .setScale(10, RoundingMode.HALF_UP)
			    .stripTrailingZeros()
			    .toPlainString();

			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Evaluation complete", "Result:\n```" + result + "```", Constants.SUCCESS))
			    .queue();

		} catch (ExpressionException | ArithmeticException e) {
			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Evaluation failed", "Invalid expression: " + e.getMessage(),
			        Constants.FAILURE))
			    .queue();
		}

	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public String getInfo() {
		return "Calculates an expression. For a list of supported functions, check out https://github.com/uklimaschewski/EvalEx#supported-functions .";
	}

	@Override
	public String getName() {
		return "Calculator";
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("expression");
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("calc", "c");
	}

}
