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

public class BanCommand extends Command {

	public static void ban(Member member, MessageChannel channel, Guild guild, Parameters params) {
		Utils.getModTarget(ModAction.BAN, channel, guild, member, params);
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();
		Member member = guild.getMember(event.getAuthor());

		ban(member, channel, guild, params);
	}

	@Override
	public String getInfo() {
		return "Bans a member from the guild and allows you to add a reason.";
	}

	@Override
	public String getName() {
		return "Ban";
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
		return Commands.toArray(Permission.BAN_MEMBERS);
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("reason (optional)", "@member");
	}

	@Override
	public int getMinParameters() {
		return 1;
	}

}
