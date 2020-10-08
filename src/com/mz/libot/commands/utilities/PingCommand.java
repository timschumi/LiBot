package com.mz.libot.commands.utilities;

import com.mz.libot.core.BotData;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PingCommand extends Command {

	public static void ping(MessageChannel channel) {
		long wsPing = BotData.getJDA().getGatewayPing();
		BotData.getJDA()
		    .getRestPing()
		    .queue(restPing -> channel.sendMessage("Pong!\nHTTP ping: **"
		        + restPing
		        + "** ms,\nWebSocket ping: **"
		        + (wsPing == -1 ? " Unavailable, try again later!" : wsPing)
		        + "** ms.").queue());
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();

		ping(channel);
	}

	@Override
	public String getInfo() {
		return "Responds with _Pong!_ and bot's current ping in milliseconds.";
	}

	@Override
	public String getName() {
		return "Ping";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

}
