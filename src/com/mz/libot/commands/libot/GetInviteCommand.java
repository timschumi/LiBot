package com.mz.libot.commands.libot;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GetInviteCommand extends Command {

	private static final Permission[] INVITE_PERMS = { Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
	    Permission.MESSAGE_ADD_REACTION, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK,
	    Permission.MESSAGE_EMBED_LINKS };

	public static final Permission[] getInvitePerms() {
		return INVITE_PERMS.clone();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		event.getChannel()
		    .sendMessage(BotUtils.buildEmbed("Invite " + BotData.getName(),
		        "To invite "
		            + BotData.getName()
		            + " to your guild, click [here]("
		            + event.getJDA().getInviteUrl(INVITE_PERMS)
		            + ") and follow further instructions!",
		        Constants.LITHIUM))
		    .queue();
	}

	@Override
	public String getInfo() {
		return "Displays bot's invitation link. To add the bot to your guild, "
		    + "click the link and follow the instructions.";
	}

	@Override
	public String getName() {
		return "GetInvite";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.LIBOT;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("add", "invite", "getlibot");
	}

}
