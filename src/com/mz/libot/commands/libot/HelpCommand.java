package com.mz.libot.commands.libot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.utils.FormatAs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class HelpCommand extends Command {

	@SuppressWarnings("null")
	private static void aboutCommand(@Nonnull TextChannel txtChannel, @Nonnull Parameters params) {
		// Activates the "about command" mode

		Command target = LiBotCore.commands.get(params.get(0));
		if (target == null) {
			throw new CommandException("Nonexistent",
			    "Command "
			        + BotUtils.escapeMarkdown(params.get(0))
			        + " does not (yet) exist! Please try again in approximately `"
			        + (new Random(params.get(0).hashCode()).nextInt(1000) + 300)
			        + "` years!",
			    Constants.DISABLED, false);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Usage: _" + target.getUsage(null) + "_");

		String[] aliases = target.getAliases();
		if (aliases.length != 0) {
			StringBuilder aliasesSb = new StringBuilder();
			for (String alias : aliases) {
				aliasesSb.append(", `" + alias + "`");
			}

			sb.append("\nAlias" + (aliases.length == 1 ? "" : "es") + ": " + aliasesSb.substring(2));
		}

		sb.append("\nCategory: _" + FormatAs.getFirstUpper(target.getCategory().toString().toLowerCase()) + "_");

		Permission[] permissions = target.getPermissions();
		if (permissions.length != 0) {
			StringBuilder permsSb = new StringBuilder();
			for (Permission perm : permissions) {
				permsSb.append(", " + perm.getName());
			}

			sb.append("\nRequired permission" + (permissions.length == 1 ? "" : "s") + ": " + permsSb.substring(2));
		}

		if (target.getRatelimit() != 0) {
			sb.append("\nRatelimit: _1 time per " + target.getRatelimit() + " seconds_");
		}

		sb.append("\n\n" + target.getInfo());

		if (target.getCategory() == CommandCategory.ADMINISTRATIVE) {
			sb.append("\n(this command (or just its administrative parts) can only be launched by "
			    + BotData.getName()
			    + "'s developer - "
			    + BotData.getOwner().getName()
			    + ")");
		}

		txtChannel.sendMessage(BotUtils.buildEmbed("Info about " + target.getName(), sb.toString(), Constants.LITHIUM))
		    .complete();
	}

	public static String getLinks(JDA jda) {
		return " - **[Join Lithium](https://discord.gg/asDUrbR)** - **[Get "
		    + BotData.getName()
		    + "]("
		    + jda.getInviteUrl(GetInviteCommand.getInvitePerms())
		    + ")**";
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		if (params.check()) {
			aboutCommand(event.getChannel(), params);

		} else {
			List<Field> categories = new ArrayList<>();

			for (CommandCategory category : CommandCategory.values()) {
				// Iterates over categories
				StringBuilder sb = new StringBuilder();

				if (LiBotCore.commands.getAll(category).isEmpty()) {
					// Skips this field if there are no registered commands in this category
					continue;
				}

				for (Command command : LiBotCore.commands.getAll(category)) {
					sb.append(command.getName() + " `" + command.getUnescapedUsage(event.getGuild()) + "`\n");
				}
				Field field = new Field(FormatAs.getFirstUpper(category.toString()), sb.toString(), false);

				categories.add(field);
			}

			EmbedBuilder builder = new EmbedBuilder();
			builder.setColor(Constants.LITHIUM);
			builder.setTitle("LiBot manual");
			builder.setThumbnail(BotData.getJDA().getSelfUser().getAvatarUrl());
			builder.setFooter(BotData.getName() + " " + Constants.VERSION,
			    BotData.getJDA().getSelfUser().getAvatarUrl());

			for (Field field : categories) {
				builder.addField(field);
			}

			builder.appendDescription("To get detailed information about a command, use `"
			    + BotUtils.getCommandPrefix(event.getGuild())
			    + "help (command's name)`.\nBy the way, you can also use @"
			    + BotData.getJDA().getSelfUser().getName()
			    + " as a command prefix!\n"
			    + getLinks(event.getJDA()));

			event.getAuthor()
			    .openPrivateChannel()
			    .queue(pc -> pc.sendMessage(builder.build())
			        .queue(t -> {
			            // If private message was not delivered
			            if (event.getGuild()
			                .getSelfMember()
			                .hasPermission(event.getChannel(), Permission.MESSAGE_ADD_REACTION)) {
					        event.getMessage().addReaction(Constants.ACCEPT_EMOJI).queue();
				        }

			        }, e -> {
			            // If private message was not delivered
			            if (event.getChannel().canTalk())
					        event.getChannel().sendMessage(builder.build()).queue();
			        }));
		}
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.LIBOT;
	}

	@Override
	public String getInfo() {
		return "Direct-messages you the list of all commands. To get detailed information about a command, "
		    + "use help along with command's name as a parameter.";
	}

	@Override
	public String getName() {
		return "Help";
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("command (optional)");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}
}
