package com.mz.libot.commands.music;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LoopCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		VoiceChannel vc = event.getGuild().getAudioManager().getConnectedChannel();
		if (vc == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if LiBot is in a voice channel

		GuildMusicManager gmm = MusicManager.getGuildMusicManager(vc);

		if (gmm.scheduler.isLoop()) {
			gmm.scheduler.setLoop(false);
			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Loop OFF", "\u25B6 Now playing tracks from the queue normally.",
			        Constants.SUCCESS))
			    .queue();
			return;
		}

		AudioTrack track = gmm.player.getPlayingTrack();
		if (track == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if anything is playing

		if (!track.isSeekable())
			throw new CommandException("Sorry, but LiBot can't loop over currently playing track.", Constants.DISABLED,
			    false);

		gmm.scheduler.setLoop(true);
		event.getChannel()
		    .sendMessage(BotUtils.buildEmbed("Loop ON",
		        "\uD83D\uDD01 Now looping over **" + track.getInfo().title + "**.", Constants.SUCCESS))
		    .queue();

	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getInfo() {
		return "Toggles loop state over the current track. You will still be able to skip tracks when looping is enabled, though.";
	}

	@Override
	public String getName() {
		return "Loop";
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.canUseMusic(event.getMember());
	}

}
