package com.mz.libot.commands.administrative;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.Bot;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.entities.EvalResult;
import com.mz.libot.utils.eval.Binding;
import com.mz.libot.utils.eval.EvalEngine;
import com.mz.libot.utils.eval.Import;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class EvaluateCommand extends Command {

	private static List<Binding> getBindings(GuildMessageReceivedEvent event) {
		List<Binding> bindings = new ArrayList<>();

		bindings.add(new Binding("event", event));
		bindings.add(new Binding("jda", BotData.getJDA()));
		bindings.add(new Binding("bot", Bot.getBot()));
		bindings.add(new Binding("channel", event.getChannel()));
		bindings.add(new Binding("message", event.getMessage()));
		bindings.add(new Binding("guild", event.getGuild()));
		bindings.add(new Binding("author", event.getAuthor()));
		bindings.add(new Binding("commands", LiBotCore.commands));
		bindings.add(new Binding("owner", BotData.getOwner()));
		bindings.add(new Binding("ownerId", BotData.getOwnerId()));

		return bindings;
	}

	private static List<Import> getImports() {
		List<Import> imports = new ArrayList<>();

		imports.addAll(EvalEngine.DEFAULT_JAVA_IMPORTS);
		imports.addAll(EvalEngine.DEFAULT_JDA_IMPORTS);
		imports.addAll(EvalEngine.DEFAULT_LIBOT_IMPORTS);

		return imports;
	}

	public static void eval(Parameters params, MessageChannel channel, GuildMessageReceivedEvent event) {
		EvalEngine engine = EvalEngine.getEngine();

		try {
			EvalResult result = null;
			result = engine.eval(params.get(0), getImports(), getBindings(event));

			if (!result.getErrorOutput().equals(""))
				throw new ScriptException(result.getErrorOutput());

			EmbedBuilder builder = new EmbedBuilder(
			    BotUtils.buildEmbed("Success", "Successfully executed this script.", Constants.SUCCESS));

			if (!result.getOutput().equals("")) {
				builder.addField("Console output:", "```\n" + result.getOutput() + "```", false);
			}

			if (result.getResult() != null) {
				builder.addField("Return value:", "```\n" + result.getResult() + "```", false);
			}

			channel.sendMessage(builder.build()).queue();
		} catch (ScriptException e) {
			// If evaluation fails
			EmbedBuilder builder = new EmbedBuilder(
			    BotUtils.buildEmbed("Failure", "Failed to execute this script.", Constants.FAILURE));

			builder.addField("Error:", "```" + e.getCause().toString() + "```", false);

			channel.sendMessage(builder.build()).queue();
		}

	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();

		eval(params, channel, event);
	}

	@Override
	public String getInfo() {
		return "Runs a Groovy script. This command is forbidden to non-admin users "
		    + "as it is possible to harm the system and do some really bad stuff with it.";
	}

	@Override
	public String getName() {
		return "Evaluate";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.ADMINISTRATIVE;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("script");
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.isOwner(event);
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("eval");
	}

}
