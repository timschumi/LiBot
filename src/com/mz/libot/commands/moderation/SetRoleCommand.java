package com.mz.libot.commands.moderation;

import com.mz.libot.commands.moderation.Utils.RolePack;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetRoleCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		RolePack pack = Utils.getRolePack(params, event);
		event.getGuild().modifyMemberRoles(pack.getTarget(), pack.getRole()).queue();
		event.getMessage().addReaction("✅").queue();
	}

	@Override
	public String getInfo() {
		return "Sets a role for the mentioned user (removes all other roles and adds the chosen one).";
	}

	@Override
	public String getName() {
		return "SetRole";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.MANAGE_ROLES);
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("member", "role");
	}

}
