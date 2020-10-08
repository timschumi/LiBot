package com.mz.libot.commands.music;

import java.util.concurrent.TimeUnit;

import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SeekCommand extends Command {

	private static final TimeUnit[] UNIT_INDEXES = new TimeUnit[] {
	    TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS
	};

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
			throw new CommandException("It is not possible to seek the current track.", Constants.DISABLED, false);

		String[] timestampSplit;
		if (params.get(0).contains(":")) {
			// hh:mm:ss
			timestampSplit = params.get(0).split(":", 3);
		} else {
			// hh mm ss
			timestampSplit = params.asArray();
		}

		int[] times = new int[3];
		for (int i = 0; i < timestampSplit.length; i++)
			times[3 - timestampSplit.length + i] = Integer.parseInt(timestampSplit[i]);
		// The above code allows the user to omit boxes of the timestamp
		// hh:mm:ss --(omit one box)-> mm:ss and not hh:mm

		int position = 0;
		for (int i = 0; i < 3; i++)
			position += UNIT_INDEXES[i].toMillis(times[i]);


		if(track.getDuration() < position || position < 0)
			throw new CommandException("That position is out of range.", Constants.FAILURE, false);

		track.setPosition(position);

		event.getMessage().addReaction(Constants.ACCEPT_EMOJI).queue();
	}

	@Override
	public String getInfo() {
		return "Seeks the currently playing track to the specified timestamp.";
	}

	@Override
	public String getName() {
		return "Seek";
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("hours (optional)", "minutes (optional)", "seconds");
	}

	@Override
	public int getMinParameters() {
		return 1;
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
