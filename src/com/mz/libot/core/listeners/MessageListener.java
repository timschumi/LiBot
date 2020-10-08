package com.mz.libot.core.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandLauncher;
import com.mz.libot.core.commands.exceptions.launch.CommandDisabledException;
import com.mz.libot.core.commands.exceptions.launch.CommandLaunchException;
import com.mz.libot.core.commands.exceptions.launch.RatelimitedException;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.handlers.Handlers;
import com.mz.libot.core.handlers.command.CommandHandlerParameter;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(MessageListener.class);

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!BootListener.isReady())
			return;

		Message message = event.getMessage();
		String msg = message.getContentDisplay();
		/*
		 * Prepares some variables for easier access later on
		 */

		if (message.getAuthor().isBot()) {
			return;
		}
		/*
		 * Checks if sender is a bot
		 */

		if (ProviderManager.CONFIGURATION.isMaintenance()) {
			if (!event.getAuthor().equals(BotData.getOwner())) {
				return;
			}

			if (event.getMessage()
			    .getContentStripped()
			    .equalsIgnoreCase(BotUtils.getCommandPrefix(event.getGuild()) + "maintenance")) {
				event.getChannel().sendMessage("Exited maintenance mode!").queue();

				ProviderManager.CONFIGURATION.setMaintenance(false);
			}

			return;
		}
		/*
		 * Checks if maintenance mode is enabled and ignores the command if it is
		 */

		if (message.isMentioned(BotData.getJDA().getSelfUser())
		    && msg.trim().length() == event.getGuild().getSelfMember().getEffectiveName().length() + 1) {
			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Welcome to LiBot",
			        "To start with LiBot, type in `"
			            + BotUtils.getCommandPrefix(event.getGuild())
			            + "help` or `@"
			            + event.getGuild().getSelfMember().getEffectiveName()
			            + " help` to receive a list of commands. "
			            + "If you're still unsure of what which command does, you can type in `"
			            + BotUtils.getCommandPrefix(event.getGuild())
			            + "help <command's name>`"
			            + "to get information about a command!",
			        Constants.LITHIUM))
			    .queue();
			return;
		}
		/*
		 * Sends 'welcome message' if LiBot is mentioned
		 */

		String selfMention = event.getGuild().getSelfMember().getAsMention();
		if (message.getContentRaw().startsWith(BotUtils.getCommandPrefix(event.getGuild()))
		    || message.getContentRaw().startsWith(selfMention)) {
			int substring = 0;
			if (message.getContentRaw().startsWith(selfMention)) {
				substring = selfMention.length();

			} else if (message.getContentRaw().startsWith(BotUtils.getCommandPrefix(event.getGuild()))) {
				substring = BotUtils.getCommandPrefix(event.getGuild()).length();

			}

			String[] parameters = Parameters
			    .parseParameters(message.getContentRaw().toLowerCase().substring(substring).trim(), 0, false, false);
			if (parameters.length < 1)
				return;
			String commandName = parameters[0];

			Command command = LiBotCore.commands.get(commandName);

			if (command == null) {
				LOG.debug("Command {} does not exist.", commandName);
				return;
			}

			try {
				CommandLauncher.checkCommand(event, command);
			} catch (RatelimitedException e) {
				if (event.getChannel().canTalk()) {
					long seconds = e.getRemaining() / 1000;
					event.getChannel()
					    .sendMessage(BotUtils.buildEmbed("Ratelimited",
					        "Not so fast! Please wait "
					            + (seconds < 1 ? "less than a second" : seconds + " seconds")
					            + " before launching "
					            + command.getName()
					            + " again!",
					        Constants.DISABLED))
					    .queue();
				}
				return;

			} catch (CommandDisabledException e) {
				if (event.getChannel().canTalk()) {
					if (e.isGlobal()) {
						event.getChannel()
						    .sendMessage(BotUtils.buildEmbed("Disabled",
						        "Command "
						            + command.getName()
						            + " is currently globally disabled. Please try again later!",
						        Constants.DISABLED))
						    .queue();

					} else {
						event.getChannel()
						    .sendMessage(BotUtils.buildEmbed("Disabled", "Command "
						        + command.getName()
						        + " is currently disabled for this guild. Please contact a moderator for further information.",
						        Constants.DISABLED))
						    .queue();
					}
				}
				return;

			} catch (CommandLaunchException e) {
				LOG.error("Failed to launch a command", e);
				return;
			}

			Handlers.COMMAND_HANDLER.handle(new CommandHandlerParameter(command, event, BotData.getJDA()));
			/*
			 * Launches the command handler
			 */
		}
	}

}
