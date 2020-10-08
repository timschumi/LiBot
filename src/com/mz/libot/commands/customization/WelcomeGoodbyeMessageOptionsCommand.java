package com.mz.libot.commands.customization;

import java.util.function.BiConsumer;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.data.providers.impl.WGMOProvider;
import com.mz.libot.core.data.providers.impl.WGMOProvider.WelcomeGoodbyeMessageOptions;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class WelcomeGoodbyeMessageOptionsCommand extends Command {

	private enum EventType {
		WELCOME,
		GOODBYE
	}

	enum Operation {
		SET((e, et) -> {
			WelcomeGoodbyeMessageOptions wgmo = ProviderManager.WGMO.get(e.getGuild());

			EventWaiter ew = new EventWaiter(e.getAuthor(), e.getChannel(), 240);

			EmbedBuilder builder = new EmbedBuilder();
			builder.setColor(Constants.LITHIUM);
			builder.appendDescription("You're setting the **"
			    + et.toString().toLowerCase()
			    + "** message for **"
			    + e.getGuild().getName()
			    + " - #"
			    + e.getChannel().getName()
			    + "**.\n**Please type in your desired message.**");
			builder.addField("Available variables (variable - description - example)",
			    "`{user}` - Mention of the user - "
			        + e.getAuthor().getAsMention()
			        + "\n`{usernm}` - User's name - "
			        + e.getAuthor().getName()
			        + "\n`{server}` - Guild/server's name - "
			        + e.getGuild().getName(),
			    false);
			builder.setFooter("Type EXIT to cancel this operation", null);

			e.getChannel().sendMessage(builder.build()).queue();

			Message message = ew.getMessage();
			String contentDisplay = message.getContentDisplay();
			String contentRaw = message.getContentRaw();

			if (contentDisplay.equalsIgnoreCase("exit"))
				return;

			if (contentRaw.length() > 100)
				throw new CommandException("Message length may not exceed 100 characters.", Constants.FAILURE, false);

			wgmo.setChannel(e.getChannel());

			switch (et) {
				case WELCOME:
					wgmo.setWelcomeMessage(contentRaw);
					break;

				case GOODBYE:
					wgmo.setGoodbyeMessage(contentRaw);
					break;
			}

			ProviderManager.WGMO.register(e.getGuild(), wgmo);

			e.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Success",
			        "The message `"
			            + contentDisplay.replace("`", "\\`")
			            + "` has been successfully set for "
			            + e.getChannel().getAsMention()
			            + ". You may now test it with `"
			            + BotUtils.getCommandPrefix(e.getGuild())
			            + "wgmo test "
			            + et.toString().toLowerCase()
			            + "`.",
			        Constants.SUCCESS))
			    .queue();

		}),

		REMOVE((e, et) -> {
			WelcomeGoodbyeMessageOptions wgmo = ProviderManager.WGMO.get(e.getGuild());

			EventWaiter ew = new EventWaiter(e.getAuthor(), e.getChannel(), 60);
			if (!ew.getBoolean("Are you sure you want to remove the **"
			    + et.toString().toLowerCase()
			    + "** message for "
			    + e.getGuild().getName()
			    + "?"))
				return;

			switch (et) {
				case WELCOME:
					String wmsg = wgmo.getWelcomeMessage();
					if (wmsg == null)
						throw new CommandException("Welcome message does not exist for this guild.",
						    Constants.DISABLED, false);

					wgmo.setWelcomeMessage(null);
					break;

				case GOODBYE:
					String gmsg = wgmo.getGoodbyeMessage();
					if (gmsg == null)
						throw new CommandException("Goodbye message does not exist for this guild.",
						    Constants.DISABLED, false);

					wgmo.setGoodbyeMessage(null);
					break;
			}

			ProviderManager.WGMO.register(e.getGuild(), wgmo);

			e.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Success", "Message removed successfully.", Constants.SUCCESS))
			    .queue();

		}),

		TEST((e, et) -> {
			WelcomeGoodbyeMessageOptions wgmo = ProviderManager.WGMO.get(e.getGuild());

			TextChannel tc = e.getGuild().getTextChannelById(wgmo.getChannelId());
			if (tc == null) {
				EventWaiter ew = new EventWaiter(e.getAuthor(), e.getChannel(), 120);
				if (ew.getBoolean(BotUtils.buildEmbed("// WARNING //",
				    "The channel LiBot would send the welcome/goodbye message to does no longer exist."
				        + "\nDo you want to set the current channel as the welcome/goodbye channel?",
				    Constants.WARN))) {
					tc = e.getChannel();

					wgmo.setChannel(tc);
					ProviderManager.WGMO.register(e.getGuild(), wgmo);

				} else {
					return;
				}

			}

			switch (et) {
				case WELCOME:
					String wmsg = wgmo.getWelcomeMessage();
					if (wmsg == null)
						throw new CommandException("Welcome message does not exist for this guild.",
						    Constants.DISABLED, false);

					tc.sendMessage(WGMOProvider.parse(wmsg, e.getAuthor(), e.getGuild())).queue();
					break;

				case GOODBYE:
					String gmsg = wgmo.getGoodbyeMessage();
					if (gmsg == null)
						throw new CommandException("Goodbye message does not exist for this guild.",
						    Constants.DISABLED, false);

					tc.sendMessage(WGMOProvider.parse(gmsg, e.getAuthor(), e.getGuild())).queue();
					break;
			}

			e.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Success", "Test message has been sent to " + tc.getAsMention() + ".",
			        Constants.SUCCESS))
			    .queue();

		});

		private BiConsumer<GuildMessageReceivedEvent, EventType> consumer;

		private Operation(BiConsumer<GuildMessageReceivedEvent, EventType> consumer) {
			this.consumer = consumer;
		}

		public void execute(GuildMessageReceivedEvent event, Parameters params) {

			EventType et;
			String ets = params.get(1).toLowerCase().trim();

			if (ets.equals("goodbye")) {
				et = EventType.GOODBYE;

			} else if (ets.equals("welcome")) {
				et = EventType.WELCOME;

			} else {
				throw new CommandException(ets + " is not a valid event type. Please use `WELCOME` or `GOODBYE`.",
				    Constants.FAILURE, false);
			}

			this.consumer.accept(event, et);
		}

	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		Operation op;
		String ops = params.get(0).toLowerCase().trim();

		if (ops.equals("set")) {
			op = Operation.SET;

		} else if (ops.equals("remove")) {
			op = Operation.REMOVE;

		} else if (ops.equals("test")) {
			op = Operation.TEST;

		} else {
			throw new CommandException(ops + " is not a valid operation. Please use `SET`, `REMOVE` or `TEST`.",
			    Constants.FAILURE, false);
		}

		op.execute(event, params);

	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.CUSTOMIZATION;
	}

	@Override
	public String getInfo() {
		return "Allows you to create, test, or remove a goodbye/welcome message for members joining/leaving your guild.\n"
		    + "Operations:\n"
		    + "> Set - sets the message that will be sent for the welcome/goodbye event\n"
		    + "> Remove - removes the message\n"
		    + "> Test (default) - tests the event message by sending it as if you were joining/leaving.";
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("set/remove/test", "welcome/goodbye");
	}

	@Override
	public String getName() {
		return "WelcomeGoodbyeMessageOptions";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("welcome", "goodbye", "farewell", "wgmo");
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.MANAGE_SERVER);
	}

}
