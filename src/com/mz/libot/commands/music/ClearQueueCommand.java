package com.mz.libot.commands.music;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.TrackScheduler;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ClearQueueCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		VoiceChannel vc = event.getGuild().getAudioManager().getConnectedChannel();
		if (vc == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if music is playing

		TrackScheduler scheduler = MusicManager.getGuildMusicManager(vc).scheduler;

		if (scheduler.size() == 0)
			throw new CommandException("The queue is already empty.", Constants.WARN, false);

		scheduler.clear();
		event.getChannel()
		    .sendMessage(BotUtils.buildEmbed("Cleared", "Queue cleared successfully", Constants.SUCCESS))
		    .queue();
	}

	@Override
	public String getInfo() {
		return "Clears the queue. The currently playing track will remain playing.";
	}

	@Override
	public String getName() {
		return "ClearQueue";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("clear");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.canUseMusic(event.getMember());
	}

}
