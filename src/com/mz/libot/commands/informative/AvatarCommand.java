package com.mz.libot.commands.informative;

import java.util.List;

import com.mz.libot.core.FinderUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AvatarCommand extends Command {

	public static void avatar(TextChannel channel, Parameters params, User author) {

		EmbedBuilder builder = new EmbedBuilder();

		User target = null;

		if (params.check()) {
			List<Member> members = FinderUtils.findMembers(params.get(0), channel.getGuild());

			if (!members.isEmpty())
				target = members.get(0).getUser();
		}

		if (target == null)
			target = author;

		builder.setImage(target.getEffectiveAvatarUrl());
		builder.appendDescription("Link: " + target.getEffectiveAvatarUrl());

		channel.sendMessage(builder.build()).queue();

	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		TextChannel channel = event.getChannel();
		User author = event.getAuthor();

		avatar(channel, params, author);
	}

	@Override
	public String getInfo() {
		return "Displays link to mentioned user's avatar image. If no user is mentioned, your avatar will be displayed.";
	}

	@Override
	public String getName() {
		return "Avatar";
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

}
