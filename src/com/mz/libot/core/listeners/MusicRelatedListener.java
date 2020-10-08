package com.mz.libot.core.listeners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.data.properties.PropertyManager;
import com.mz.libot.core.data.properties.impl.FTPPropertyManager;
import com.mz.libot.core.data.providers.SnowflakeProvider;
import com.mz.libot.core.ftp.FTPManager;
import com.mz.libot.core.listeners.MusicRelatedListener.MusicQueueProvider.MusicQueueDescriptor;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.GuildMusicManager;
import com.mz.libot.core.music.entities.exceptions.QueueFullException;
import com.mz.libot.utils.MessageLock;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class MusicRelatedListener extends ListenerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(MusicRelatedListener.class);
	private static final MusicQueueProvider MUSIC_QUEUE = new MusicQueueProvider();

	static final Runnable SHUTDOWN_HOOK = () -> {

		MusicManager.getGuildMusicManagers().forEach((guildId, gmm) -> {

			if (gmm.player.getPlayingTrack() == null)
				return;
			// In case LiBot is not actually connected to the voice channel (workaround)

			List<String> tracks = new ArrayList<>();
			tracks.add(gmm.player.getPlayingTrack().getInfo().uri);
			tracks.addAll(gmm.scheduler.getTracks()
			    .stream()
			    .map(track -> track.getInfo().uri)
			    .limit(30)
			    .collect(Collectors.toList()));
			// Converts all tracks into a list of URIs

			if (tracks.isEmpty())
				return;
			// Skips if there are no tracks

			MUSIC_QUEUE.put(guildId, new MusicQueueDescriptor(tracks, gmm.player.isPaused(),
			    gmm.player.getPlayingTrack().getPosition(), gmm.voiceChannelId, gmm.scheduler.isLoop()));
		});
		// Puts all queues into provider's data

		if (BotData.getProperties() instanceof FTPPropertyManager) {
			FTPPropertyManager libotProperties = (FTPPropertyManager) BotData.getProperties();

			try {
				MUSIC_QUEUE.store(MusicQueueProvider.createPropertyManager(libotProperties.getManager()), false);
			} catch (IOException e) {
				LOG.error("Failed to store the music queues", e);
			}
		}
		// Stores data on a FTP server (if available) [VERY HACKY]

	};

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(SHUTDOWN_HOOK, "MusicQueueUploader"));
	}

	public static final int DISCONNECT_TIMEOUT = 5000;
	private static final Predicate<Member> NO_BOT = m -> !m.getUser().isBot();

	private static final ScheduledExecutorService DISCONNECT_TIMEOUT_EXECUTOR = Executors
	    .newSingleThreadScheduledExecutor(
	        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("music-disconnect-timeouter-thread-%d").build());

	public static void loadMusicQueues(PropertyManager pm) {
		MUSIC_QUEUE.load(pm);
	}

	@SuppressFBWarnings("DE_MIGHT_IGNORE")
	protected static class MusicQueueProvider extends SnowflakeProvider<MusicQueueDescriptor> {

		private static final TypeToken<Map<Long, MusicQueueDescriptor>> TYPE_TOKEN = new TypeToken<>() {};

		protected static class MusicQueueDescriptor {

			private final List<String> tracks;
			private final boolean isPaused;
			private final long position;
			private final long voiceChannelId;
			private boolean loop;

			public MusicQueueDescriptor(List<String> tracks, boolean isPaused, long position, long voiceChannelId,
			                            boolean loop) {
				this.tracks = tracks;
				this.isPaused = isPaused;
				this.position = position;
				this.voiceChannelId = voiceChannelId;
				this.loop = loop;
			}

			public List<String> getTracks() {
				return Collections.unmodifiableList(this.tracks);
			}

			public boolean isPaused() {
				return this.isPaused;
			}

			public long getPosition() {
				return this.position;
			}

			public long getVoiceChannelId() {
				return this.voiceChannelId;
			}

			public boolean isLoop() {
				return this.loop;
			}

		}

		public void put(long guildId, MusicQueueDescriptor descriptor) {
			this.data.put(guildId, descriptor);
		}

		@Override
		public TypeToken<Map<Long, MusicQueueDescriptor>> getTypeToken() {
			return TYPE_TOKEN;
		}

		@Override
		public String getDataKey() {
			return "musicqueues";
		}

		@Override
		public Map<Long, MusicQueueDescriptor> getData() {
			throw new UnsupportedOperationException("Direct access to this provider's data is not permitted.");
		}

		@Override
		protected void onDataLoaded(PropertyManager pm) {
			this.data.forEach((guildId, data) -> {
				try {
					Guild guild = BotData.getJDA().getGuildById(guildId);
					if (guild == null)
						return;
					// In case the whole guild has been deleted

					VoiceChannel vc = guild.getVoiceChannelById(data.getVoiceChannelId());
					if (vc == null)
						return;
					// In case the voice channel has been deleted

					AudioManager am = guild.getAudioManager();
					GuildMusicManager gmm = MusicManager.getGuildMusicManager(vc);
					MusicManager.stopPlayback(vc);

					if (am.getSendingHandler() == null)
						am.setSendingHandler(gmm.getSendHandler());
					if (!am.isConnected())
						am.openAudioConnection(vc);
					// Initializes the audio mananar and connects to the voice channel

					List<AudioTrack> tracks = data.getTracks()
					    .stream()
					    .map(MusicQueueProvider::resolveAudioTrack)
					    .filter(Objects::nonNull)
					    .collect(Collectors.toList());
					// Resolves all tracks

					AudioTrack first = tracks.get(0);
					if (first.isSeekable() && first.getInfo().uri.equals(data.getTracks().get(0)))
						first.setPosition(data.getPosition());
					// Seeks the first track to the last position (if possible)

					gmm.player.setPaused(data.isPaused());
					// Loads the player's state

					gmm.scheduler.setLoop(data.isLoop());
					// Loads the looping state

					tracks.forEach(t -> {
						try {
							gmm.scheduler.queue(t, false);
						} catch (QueueFullException e) {
							LOG.error("Failed to restore the tracks because the queue was full", e);
						}
					});
					// Queues all tracks
				} catch (Exception e) {
					BotData.getOwner()
					    .openPrivateChannel()
					    .queue(dm -> dm
					        .sendMessage("Failed to restore music on guild "
					            + guildId
					            + ". MusicQueueDescriptor and the stack trace have been appended as a file.")
					        .addFile(GSON.toJson(data).getBytes(), "mqd.json")
					        .addFile(ExceptionUtils.getStackTrace(e).getBytes(), "stacktrace.txt")
					        .queue());
				}
			});

			this.data.clear();
			// Clears all music queues to save memory

			if (BotData.getProperties() instanceof FTPPropertyManager) {
				FTPPropertyManager libotProperties = (FTPPropertyManager) BotData.getProperties();

				try {
					this.store(MusicQueueProvider.createPropertyManager(libotProperties.getManager()));
				} catch (IOException e) {
					LOG.error("Failed to clear the music queues", e);
				}
			}
			// Clears the configuration on the FTP server to prevent "duplicate queues"
		}

		public static FTPPropertyManager createPropertyManager(FTPManager manager) throws IOException {
			return new FTPPropertyManager(manager, new File("temporary/musicqueues.ini"), "musicqueues.ini");
		}

		private static AudioTrack resolveAudioTrack(String url) {
			MessageLock<AudioTrack> lock = new MessageLock<>();

			MusicManager.AUDIO_PLAYER_MANAGER.loadItem(url, new AudioLoadResultHandler() {

				@Override
				public void trackLoaded(AudioTrack track) {
					lock.send(track);
				}

				@Override
				public void playlistLoaded(AudioPlaylist playlist) {
					lock.send(null);
				}

				@Override
				public void noMatches() {
					lock.send(null);
				}

				@Override
				public void loadFailed(FriendlyException exception) {
					lock.send(null);
				}
			});

			return lock.receive();
		}

		@Override
		protected Predicate<Long> getObsoleteFilter() {
			return null;
		}

	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		if (!BootListener.isReady())
			return;

		VoiceChannel vc = event.getGuild().getAudioManager().getConnectedChannel();
		if (vc != null)
			MusicManager.stopPlayback(vc);
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		if (!BootListener.isReady())
			return;

		AudioManager am = event.getGuild().getAudioManager();
		// If LiBot is currently connected to that channel

		if (am.isConnected()) {
			if (event.getMember().getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
				// If LiBot has left the channel
				am.closeAudioConnection();

			} else {
				if (event.getChannelLeft().getMembers().stream().filter(NO_BOT).count() == 0) {
					// If there aren't any human users in that voice channel, waits for <timeout> seconds
					// and rechecks

					DISCONNECT_TIMEOUT_EXECUTOR.schedule(() -> {

						if (event.getChannelLeft().getMembers().stream().filter(NO_BOT).count() == 0) {
							MusicManager.stopPlayback(event.getChannelLeft());
							am.closeAudioConnection();
						}

					}, DISCONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
				}
			}
		}
	}

}
