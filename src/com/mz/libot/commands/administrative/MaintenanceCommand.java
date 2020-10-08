package com.mz.libot.commands.administrative;

import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MaintenanceCommand extends Command {

	public static void maintenance(MessageChannel channel) {
		ProviderManager.CONFIGURATION.setMaintenance(true);

		channel.sendMessage("Entered maintenance mode!").queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();

		maintenance(channel);
	}

	@Override
	public String getInfo() {
		return "Toggles maintenance mode.";
	}

	@Override
	public String getName() {
		return "Maintenance";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.ADMINISTRATIVE;
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.isOwner(event);
	}

}
