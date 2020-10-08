package com.mz.libot.commands.music;

import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RewindCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		VoiceChannel vc = event.getGuild().getAudioManager().getConnectedChannel();
		if (vc == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if LiBot is in a voice channel

		AudioTrack track = MusicManager.getGuildMusicManager(vc).player.getPlayingTrack();
		if (track == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if anything is playing

		if (!track.isSeekable())
			throw new CommandException("Sorry, but LiBot can't rewind that track!", Constants.DISABLED, false);

		track.setPosition(0);

		event.getMessage().addReaction(Constants.ACCEPT_EMOJI).queue();
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getInfo() {
		return "Rewinds the current track to the beginning.";
	}

	@Override
	public String getName() {
		return "Rewind";
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.canUseMusic(event.getMember());
	}

}
