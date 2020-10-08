package com.mz.libot.commands.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.math.NumberUtils;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.exceptions.runtime.TimeoutException;
import com.mz.libot.core.commands.exceptions.startup.NotDjException;
import com.mz.libot.core.commands.exceptions.startup.UnpredictedStateException;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.GuildMusicManager;
import com.mz.libot.core.music.entities.TrackScheduler;
import com.mz.libot.core.music.entities.exceptions.QueueFullException;
import com.mz.libot.utils.EventWaiter;
import com.mz.libot.utils.MessageLock;
import com.mz.libot.utils.Parser;
import com.mz.libot.utils.entities.Customization;
import com.mz.utils.Counter;
import com.mz.utils.FormatAs;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

class Utils {

	public static final String NEW_TRACK_MESSAGE = "Now playing: **%s** by _%s_.";
	public static final String QUEUED_TRACK_MESSAGE = "Put **%s** by _%s_ into queue.";

	private Utils() {}

	/**
	 * Plays a track from any website
	 *
	 * @param url
	 *            URL of the track
	 * @param channel
	 * @param scheduler
	 *            TrackScheduler to use
	 * @param playerManager
	 *            AudioPlayerManager to use
	 */
	static void playTrack(String url, TextChannel channel, TrackScheduler scheduler, User user) {
		MessageLock<List<AudioTrack>> lock = new MessageLock<>();

		MusicManager.AUDIO_PLAYER_MANAGER.loadItem(url, new AudioLoadResultHandlerImpl(lock, scheduler, channel, url));
		// Loads the track or fills in search results

		List<AudioTrack> tracks = lock.receive();
		if (tracks != null && !tracks.isEmpty()) {
			playTrack(selectTrack(tracks, channel, user).getInfo().uri, channel, scheduler, user);
			// If the previous command found a search result
		}
		// Waits for track loader thread to finish
	}

	/**
	 * Finds a track on YouTube and prints the results in chat
	 *
	 * @param query
	 *            Search query
	 * @param playerManager
	 *            AudioPlayerManager to use
	 * @param channel
	 *
	 * @throws InterruptedException
	 */
	static List<AudioTrack> findTracks(String query, AudioPlayerManager playerManager) {
		MessageLock<List<AudioTrack>> lock = new MessageLock<>();

		playerManager.loadItem("ytsearch:" + query, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				lock.send(Collections.emptyList());
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				lock.send(playlist.getTracks());
			}

			@Override
			public void noMatches() {
				lock.send(Collections.emptyList());
			}

			@Override
			public void loadFailed(FriendlyException e) {
				lock.throwException(e);
			}

		});

		return lock.receive();
	}

	/**
	 * Easily plays a track from an URL
	 *
	 * @param url
	 *            URL to play track from
	 * @param manager
	 *            GuildMusicManager containing all proper variables
	 * @param member
	 * @param channel
	 * @param guild
	 *
	 * @throws CommandException
	 * @throws InterruptedException
	 */
	static void easyPlay(String url, Member member, TextChannel channel) {

		GuildVoiceState voiceState = member.getVoiceState();
		if (voiceState == null)
			throw new UnpredictedStateException();

		VoiceChannel voiceChannel = voiceState.getChannel();
		if (voiceChannel == null) {
			throw new CommandException("You must be in a voice channel in order to play music!", false);
		}
		// Checks if user is in a voice channel

		AudioManager audioManager = channel.getGuild().getAudioManager();

		if (!audioManager.isConnected())
			audioManager.openAudioConnection(voiceChannel);
		// Connects to the voice channel voiceChannel if not already connected

		GuildMusicManager gmm = MusicManager.getGuildMusicManager(voiceChannel);
		// Gets the GuildMusicManager variable

		if (audioManager.getSendingHandler() == null)
			audioManager.setSendingHandler(gmm.getSendHandler());
		// Initializes the AudioManager variable and sets the sending handler if not
		// already set

		playTrack(url, channel, gmm.scheduler, member.getUser());
		// Loads the song
	}

	/**
	 * Lets the user pick a track from the list
	 *
	 * @param results
	 *            list of tracks
	 * @param channel
	 * @param user
	 *
	 * @return
	 *
	 * @throws CommandException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("null")
	static AudioTrack selectTrack(@Nonnull List<AudioTrack> results, @Nonnull MessageChannel channel,
	                              @Nonnull User user) {
		if (results.isEmpty())
			throw new CommandException("No results", "No results were found!", Constants.DISABLED, false);

		StringBuilder builder = new StringBuilder();

		Counter i = new Counter();
		builder.append(results.stream().limit(10).map(track -> {
			i.count();

			return "**#"
			    + i.getCount()
			    + ":** "
			    + (track.getInfo().author != null ? BotUtils.escapeMarkdown(track.getInfo().author) + " - " : "")
			    + "_"
			    + BotUtils.escapeMarkdown(track.getInfo().title)
			    + "_";

		}).collect(Collectors.joining(",\n")));
		builder.append("\nPlease type in track's index (the bolded number) or EXIT to abort");

		AudioTrack selected = null;
		EventWaiter ew = new EventWaiter(user, channel, 45);

		Message resultsMessage = channel.sendMessage(BotUtils.buildEmbed(
		    "Search results from " + FormatAs.getFirstUpper(results.get(0).getSourceManager().getSourceName()),
		    builder.toString(), Constants.LITHIUM)).complete();

		while (selected == null) {
			String indexString;
			try {
				indexString = ew.getString();
			} catch (TimeoutException e) {
				resultsMessage.delete().queue();
				throw e;
			}

			if ("exit".equalsIgnoreCase(indexString)) {
				resultsMessage.delete().queue();
				throw new CanceledException();
			}

			if (!NumberUtils.isParsable(indexString)) {
				channel.sendMessage("Index is a number!").queue();
				continue;
			}

			int index = Parser.parseInt(indexString);
			if (index < 1 || index > results.size()) {
				channel.sendMessage("Invalid index!").queue();
				continue;
			}

			selected = results.get(index - 1);
		}

		resultsMessage.delete().queue();

		return selected;
	}

	static void canUseMusic(Member member) {
		Customization cust = ProviderManager.CUSTOMIZATIONS.getCustomization(member.getGuild());
		if (!cust.isDj(member)) {
			Long id = cust.getDjRoleId();
			if (id == null)
				return;

			throw new NotDjException(member.getGuild().getRoleById(id));
		}
	}

	private static class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {

		private final MessageLock<List<AudioTrack>> lock;
		private final TrackScheduler scheduler;
		private final MessageChannel channel;
		private final String url;

		public AudioLoadResultHandlerImpl(MessageLock<List<AudioTrack>> lock, TrackScheduler scheduler,
		                                  MessageChannel channel, String url) {
			this.lock = lock;
			this.scheduler = scheduler;
			this.channel = channel;
			this.url = url;
		}

		@SuppressWarnings("null")
		@Override
		public void trackLoaded(AudioTrack track) {
			this.lock.send(null);
			// Resumes the thread right away because waiting isn't necessary

			boolean started;
			try {
				started = this.scheduler.queue(track);
			} catch (QueueFullException e) {
				this.channel.sendMessage(BotUtils.buildEmbed("Queue is full",
				    "This track has not been added to the queue because the queue may not exceed "
				        + TrackScheduler.QUEUE_MAX_SIZE
				        + " elements!",
				    Constants.FAILURE)).queue();
				return;
			}
			AudioTrackInfo info = track.getInfo();

			if (started) {
				this.channel
				    .sendMessage(BotUtils.buildEmbed("Started playing", String.format(NEW_TRACK_MESSAGE,
				        BotUtils.escapeMarkdown(info.title), BotUtils.escapeMarkdown(info.author)), Constants.SUCCESS))
				    .queue();
			} else {
				this.channel
				    .sendMessage(BotUtils.buildEmbed("Track queued", String.format(QUEUED_TRACK_MESSAGE,
				        BotUtils.escapeMarkdown(info.title), BotUtils.escapeMarkdown(info.author)), Constants.SUCCESS))
				    .queue();
			}
		}

		@Override
		public void playlistLoaded(AudioPlaylist playlist) {
			if (playlist.isSearchResult()) {
				List<AudioTrack> searchResult = new ArrayList<>();

				for (AudioTrack track : playlist.getTracks())
					searchResult.add(track);

				this.lock.send(searchResult);
				return;
			}

			this.lock.send(null);
			// Resumes the thread right away because waiting isn't necessary

			int exceeded = 0;
			boolean started = false;
			for (AudioTrack track : playlist.getTracks()) {
				if (started) {
					try {
						this.scheduler.queue(track);
					} catch (QueueFullException e) {
						exceeded++;
					}
				} else {
					try {
						started = this.scheduler.queue(track);
					} catch (QueueFullException e) {
						exceeded++;
					}
				}
			}

			this.channel.sendMessage(BotUtils.buildEmbed(started ? "Started playing" : "Queued",
			    "Added "
			        + (playlist.getTracks().size() - exceeded)
			        + " songs from playlist "
			        + playlist.getName()
			        + " to the queue"
			        + (started ? " and started the player!" : "!")
			        + (exceeded > 0
			            ? " "
			                + exceeded
			                + " elements were not added to the queue because the queue is capped at "
			                + TrackScheduler.QUEUE_MAX_SIZE
			                + " elements!"
			            : ""),
			    started ? Constants.SUCCESS : Constants.LITHIUM)).queue();
		}

		@Override
		public void noMatches() {
			this.lock.send(null);
			// Resumes the thread right away because waiting isn't necessary

			this.channel
			    .sendMessage(
			        BotUtils.buildEmbed("No streamable track found on '" + this.url + "'!", Constants.DISABLED))
			    .queue();
		}

		@Override
		public void loadFailed(FriendlyException e) {
			this.lock.send(null);
			// Resumes the thread right away because waiting isn't necessary

			this.channel
			    .sendMessage(
			        BotUtils.buildEmbed("Failure", "Failed to load this track: " + e.getMessage(), Constants.FAILURE))
			    .queue();
		}
	}

}

// Play by id link: https://www.youtube.com/watch?v=ID
// Thumbnail link: http://img.youtube.com/vi/ID/default.jpg
