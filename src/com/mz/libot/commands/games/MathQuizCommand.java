package com.mz.libot.commands.games;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.mz.expGen.Expression;
import com.mz.expGen.ExpressionGenerator;
import com.mz.expGen.Operator;
import com.mz.expGen.operator.OperatorMul;
import com.mz.expGen.operator.OperatorSub;
import com.mz.expGen.operator.OperatorSum;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;
import com.mz.libot.utils.entities.ExpressionMistake;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MathQuizCommand extends Command {

	enum Difficulty {
		EASY,
		MEDIUM,
		HARD
	}

	private static Field createMistakesField(List<ExpressionMistake> mistakes) {
		StringBuilder sbMistakes = new StringBuilder();

		if (mistakes.isEmpty()) {
			sbMistakes.append("No mistakes at all! Hooray!");

		} else {
			for (ExpressionMistake mistake : mistakes) {
				sbMistakes.append("\n```"
				    + mistake.getExpression().getExpression()
				    + "```Equals ~~"
				    + mistake.getAnswer().toPlainString()
				    + "~~ **"
				    + mistake.getExpression().getSolution().stripTrailingZeros().toPlainString()
				    + "**");
			}
			// Creates detailed list of mistakes

			if (sbMistakes.toString().toCharArray().length > MessageEmbed.VALUE_MAX_LENGTH) {
				sbMistakes = new StringBuilder();

				for (ExpressionMistake mistake : mistakes) {
					sbMistakes.append("\n```"
					    + mistake.getExpression().getExpression()
					    + "```Equals "
					    + mistake.getExpression().getSolution().stripTrailingZeros().toPlainString());
				}
			}
			// Creates slightly less detailed list of mistakes if the first list is too long

			if (sbMistakes.toString().toCharArray().length > MessageEmbed.VALUE_MAX_LENGTH) {
				sbMistakes = new StringBuilder();

				for (ExpressionMistake mistake : mistakes) {
					sbMistakes.append("\n**#"
					    + (mistakes.indexOf(mistake) + 1)
					    + "** = ~~"
					    + mistake.getAnswer().toPlainString()
					    + "~~ **"
					    + mistake.getExpression().getSolution().stripTrailingZeros().toPlainString()
					    + "**");
				}
			}
			// Creates way less detailed list of mistakes if the second list is too long

			if (sbMistakes.toString().toCharArray().length > MessageEmbed.VALUE_MAX_LENGTH) {
				sbMistakes = new StringBuilder();

				for (ExpressionMistake mistake : mistakes) {
					sbMistakes.append("\n#"
					    + (mistakes.indexOf(mistake) + 1)
					    + " = "
					    + mistake.getExpression().getSolution().stripTrailingZeros().toPlainString());
				}
			}
			// Creates way less detailed list of mistakes without markdown if the third list
			// is too long

			if (sbMistakes.toString().toCharArray().length > MessageEmbed.VALUE_MAX_LENGTH) {
				sbMistakes = new StringBuilder("_Too many mistakes for Discord to process, sorry :(_");
			}
			// Does not create a list of mistakes if all previous attempts are too long
		}

		return new Field("Mistakes:", sbMistakes.toString(), false);
		// Creates mistakes field
	}

	private static List<Expression> generateQuestion(int questions, Difficulty difficulty) {
		List<Expression> expressions = new ArrayList<>();

		ExpressionGenerator eg = null;
		Operator[] easyOps = {
		    new OperatorSum(), new OperatorSub()
		};
		Operator[] mediumOps = {
		    new OperatorSum(), new OperatorSub(), new OperatorMul()
		};

		switch (difficulty) {
			case EASY:
				eg = new ExpressionGenerator().setBounds(1, 100)
				    .useDecimalNumbers(false)
				    .setOperators(easyOps)
				    .setMaxDecimalsInResult(0);
				break;

			case MEDIUM:
				eg = new ExpressionGenerator().setBounds(-100, 100)
				    .useDecimalNumbers(true)
				    .setDecimalPlacesBounds(0, 1)
				    .setOperators(mediumOps)
				    .setMaxDecimalsInResult(1);
				break;

			case HARD:
				eg = new ExpressionGenerator().setBounds(-200, 1000)
				    .useDecimalNumbers(true)
				    .setDecimalPlacesBounds(0, 3)
				    .setMaxDecimalsInResult(2);
				break;

		}
		// Configures generator

		if (eg == null)
			throw new CommandException("Unrecognized difficulty", Constants.FAILURE, false);

		for (int i = 0; i < questions; i++) {
			switch (difficulty) {
				case EASY:
					eg.setRandomLength(1, 3);
					break;

				case MEDIUM:
					eg.setRandomLength(3, 5);
					break;

				case HARD:
					eg.setRandomLength(4, 8);
					break;
			}
			// Sets expression's length

			expressions.add(eg.generate());
		}
		// Generates expressions

		return expressions;
	}

	public static void mathQuiz(Parameters params, MessageChannel channel, User author) {
		String difficultyString = params.get(0).toLowerCase();
		if (!"easy|medium|hard".contains(difficultyString))
			throw new CommandException("Difficulty parameter (the first one) must be \"easy\", \"medium\" or \"hard\"!",
			    false);
		// Checks if difficulty is correct

		if (!NumberUtils.isParsable(params.get(1)))
			throw new CommandException("Number of questions parameter (the second one) must be a number!", false);
		int quantity = params.getAsInteger(1);

		if (quantity > 35)
			throw new CommandException("Maximal number of questions is 35!", false);
		// Checks if number of questions is correct

		if (quantity < 1)
			throw new CommandException("Minimal number of questions is 1!", false);
		// Checks if number of questions is correct

		Difficulty difficulty;
		if (difficultyString.equals("easy")) {
			difficulty = Difficulty.EASY;

		} else if (difficultyString.equals("medium")) {
			difficulty = Difficulty.MEDIUM;

		} else if (difficultyString.equals("hard")) {
			difficulty = Difficulty.HARD;

		} else {
			throw new IllegalArgumentException("Couldn't recognize the difficulty.");
		}

		List<Expression> questions = generateQuestion(quantity, difficulty);

		List<ExpressionMistake> mistakesList = new ArrayList<>();
		int i = 1;
		int correct = 0;

		long start = System.currentTimeMillis();

		EventWaiter ew = new EventWaiter(author, channel);
		for (Expression exp : questions) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setColor(Constants.LITHIUM);
			builder.setTitle("Math quiz");
			builder.setDescription(
			    "Solve ```" + exp.getExpression() + "```and type in the result, or type in ``EXIT`` to exit the quiz!");
			builder.setFooter("Question " + i + " out of " + quantity, null);
			channel.sendMessage(builder.build()).queue();

			String answerString = null;
			while (!NumberUtils.isParsable(answerString)) {
				answerString = ew.getString().toLowerCase();

				if (answerString.equals("exit") && ew.getBoolean("Are you sure you want to leave math quiz?")) {
					channel.sendMessage(BotUtils.buildEmbed("Exit", "You left the quiz..", Constants.DISABLED)).queue();
					throw new CanceledException();
				}
			}

			BigDecimal answer = new BigDecimal(answerString).stripTrailingZeros();

			if (exp.getSolution().stripTrailingZeros().equals(answer)) {
				correct++;
			} else {
				mistakesList.add(new ExpressionMistake(exp, answer));
			}

			i++;
		}
		// Lets user solve the expression

		long end = System.currentTimeMillis();

		Field score = new Field("Score:", correct + " out of " + quantity + " on " + difficultyString + " difficulty",
		    false);
		// Creates score field

		Field time = new Field("Time:", (end - start) / 1000 + " seconds", false);
		// Calculates time & creates time field

		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Math quiz");
		builder.addField(score);
		builder.addField(time);
		builder.addField(createMistakesField(mistakesList));
		// Configures EmbedBuilder

		float correctFloat = correct;
		float quantityFloat = quantity;
		float result = correctFloat / quantityFloat;
		builder.setColor(new Color(1f - result, result, 0f));
		// Calculates and sets color from score

		channel.sendMessage(builder.build()).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		mathQuiz(params, channel, author);
	}

	@Override
	public String getInfo() {
		return "Generates a number of mathematical questions for you and tells you how well you did once you solve them.";
	}

	@Override
	public String getName() {
		return "MathQuiz";
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
		return Commands.toArray("difficulty (easy/medium/hard)", "number of questions");
	}
}
