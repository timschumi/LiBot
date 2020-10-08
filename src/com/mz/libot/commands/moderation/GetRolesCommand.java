package com.mz.libot.commands.moderation;

import java.util.ArrayList;
import java.util.List;

import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GetRolesCommand extends Command {

	public static void getRoles(MessageChannel channel, Guild guild) {
		List<Role> allRoles = guild.getRoles();

		ArrayList<String> allRolesString = new ArrayList<>();
		for (Role roleRole : allRoles) {
			allRolesString.add(roleRole.getName());
		}

		String roles = allRolesString.toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("@", "");

		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Constants.LITHIUM);
		builder.setTitle("List of " + guild.getName() + "'s roles:");
		builder.setDescription(roles);
		channel.sendMessage(builder.build()).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		getRoles(channel, guild);
	}

	@Override
	public String getInfo() {
		return "Lists all available roles for current guild.";
	}

	@Override
	public String getName() {
		return "GetRoles";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("roles");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

}
