package com.mz.libot.core.music;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.mz.libot.core.music.entities.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class MusicManager {

	private static final Map<Long, GuildMusicManager> ACTIVE_MUSIC_MANAGERS = new ConcurrentHashMap<>();
	public static final AudioPlayerManager AUDIO_PLAYER_MANAGER;
	static {
		AUDIO_PLAYER_MANAGER = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(AUDIO_PLAYER_MANAGER);
	}

	/**
	 * Returns the GuildMusicManager for this guild. If a GuildMusicManager does not
	 * exist for this guild, this will create a new one.
	 *
	 * @param voiceChannel
	 *
	 * @return the guild music manager for this guild
	 */
	public static GuildMusicManager getGuildMusicManager(VoiceChannel voiceChannel) {
		GuildMusicManager gmm = getGuildMusicManager(voiceChannel.getGuild());
		if (gmm == null) {
			gmm = new GuildMusicManager(AUDIO_PLAYER_MANAGER, voiceChannel);
			ACTIVE_MUSIC_MANAGERS.put(voiceChannel.getGuild().getIdLong(), gmm);
		}

		return gmm;
	}

	/**
	 * @param guild
	 *
	 * @return the guild music manager for this guild or null if none exist
	 */
	@Nullable
	public static GuildMusicManager getGuildMusicManager(Guild guild) {
		return ACTIVE_MUSIC_MANAGERS.get(guild.getIdLong());
	}

	/**
	 * Completely stops the playback, clears the queue, destroys the player and removes
	 * the GuildMusicManager to save RAM.
	 *
	 * @param voiceChannel
	 */
	public static void stopPlayback(VoiceChannel voiceChannel) {
		GuildMusicManager gmm;
		if ((gmm = getGuildMusicManager(voiceChannel.getGuild())) != null) {
			gmm.player.stopTrack();
			gmm.scheduler.clear();
		}
	}

	/**
	 * @return a map of GuildMusicManagers (key is guild's id, values is the
	 *         GuildMusicManager)
	 */
	public static Map<Long, GuildMusicManager> getGuildMusicManagers() {
		return Collections.unmodifiableMap(ACTIVE_MUSIC_MANAGERS);
	}

	private MusicManager() {}

}
