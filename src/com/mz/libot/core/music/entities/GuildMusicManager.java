package com.mz.libot.core.music.entities;

import com.mz.libot.core.music.AudioPlayerSendHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {

	/**
	 * Audio player for the guild.
	 */
	public final AudioPlayer player;

	/**
	 * Track scheduler for the player.
	 */
	public final TrackScheduler scheduler;

	/**
	 * Id of the voice channel this {@link GuildMusicManager} is going to be used in.
	 */
	public final long voiceChannelId;

	/**
	 * Creates a player and a track scheduler.
	 * 
	 * @param manager
	 *            Audio player manager to use for creating the player.
	 * @param voiceChannel
	 *            the voice channel this {@link GuildMusicManager} is going to be used
	 *            in.
	 */
	public GuildMusicManager(AudioPlayerManager manager, VoiceChannel voiceChannel) {
		this.player = manager.createPlayer();
		this.scheduler = new TrackScheduler(this.player);
		this.player.addListener(this.scheduler);

		this.voiceChannelId = voiceChannel.getIdLong();
	}

	/**
	 * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	public AudioPlayerSendHandler getSendHandler() {
		return new AudioPlayerSendHandler(this.player);
	}
}
