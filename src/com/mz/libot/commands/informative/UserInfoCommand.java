package com.mz.libot.commands.informative;

import java.util.List;
import java.util.stream.Collectors;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.FinderUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.Timestamp;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class UserInfoCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		User target = null;

		if (params.check()) {
			List<Member> members = FinderUtils.findMembers(params.get(0), event.getGuild());

			if (!members.isEmpty())
				target = members.get(0).getUser();
		}

		if (target == null)
			target = event.getAuthor();

		Member targetMember = event.getGuild().getMember(target);
		if (targetMember == null)
			throw new CommandException("That user is not a part of the current guild", Constants.FAILURE, false);

		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Constants.LITHIUM);
		builder.setThumbnail(target.getAvatarUrl());

		builder.setTitle("Info about "
		    + target.getName()
		    + "#"
		    + target.getDiscriminator()
		    + (target.isBot() ? " [BOT]" : "")
		    + (targetMember.isOwner() ? " [OWNER]" : ""));
		builder.addField("ID:", "```" + target.getId() + "```", true);
		builder.addField("Avatar URL:", "```" + target.getAvatarUrl() + "```", true);
		builder.addField("Account creation date:",
		    "```" + Timestamp.formatTimestamp(target.getTimeCreated(), "yyyy-MM-dd HH:mm") + "```", true);

		builder.addField("Current name for this guild:", "```" + targetMember.getEffectiveName() + "```", true);
		builder.addField("Guild join date:",
		    "```" + Timestamp.formatTimestamp(targetMember.getTimeJoined(), "yyyy-MM-dd HH:mm") + "```", true);

		String roles = "None";
		if (!targetMember.getRoles().isEmpty())
			targetMember.getRoles().stream().map(Role::getName).collect(Collectors.joining(", "));

		builder.addField("Roles:", "```" + roles + "```", true);
		builder.addField("Permissions:",
		    "```"
		        + (targetMember.isOwner() ? "All of them."
		            : "To get "
		                + target.getName()
		                + "'s permissions for this guild, use "
		                + BotUtils.getCommandPrefix(event.getGuild())
		                + "perms @"
		                + target.getName()
		                + "#"
		                + target.getDiscriminator()
		                + ".")
		        + "```",
		    true);

		event.getChannel().sendMessage(builder.build()).queue();
	}

	@Override
	public String getInfo() {
		return "This command retrieves some of the 'hidden' (less accessible) data about someone. "
		    + "If no one is mentioned, information about you will be displayed.";
	}

	@Override
	public String getName() {
		return "UserInfo";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.INFORMATIVE;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("user's name (optional)");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("user");
	}
}
