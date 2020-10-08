package com.mz.libot.commands.moderation;

import com.mz.libot.commands.moderation.Utils.ModAction;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class KickCommand extends Command {

	public static void kick(Member member, MessageChannel channel, Guild guild, Parameters params) {
		Utils.getModTarget(ModAction.KICK, channel, guild, member, params);
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();
		Member member = guild.getMember(event.getAuthor());

		kick(member, channel, guild, params);
	}

	@Override
	public String getInfo() {
		return "Kicks a member from the guild and allows you to add a reason.";
	}

	@Override
	public String getName() {
		return "Kick";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public boolean pausesThread() {
		return true;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.KICK_MEMBERS);
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("user's name (no spaces allowed)", "reason (optional)");
	}

	@Override
	public int getMinParameters() {
		return 1;
	}

}
