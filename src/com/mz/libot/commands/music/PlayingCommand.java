package com.mz.libot.commands.music;

import com.mz.libot.core.BotData;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.GuildMusicManager;
import com.mz.libot.utils.Timestamp;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PlayingCommand extends Command {

	public static final String YT_THUMBNAIL = "http://img.youtube.com/vi/%s/mqdefault.jpg";
	public static final String DEFAULT_THUMBNAIL = "https://yt3.ggpht.com/pHwZj3tkgC3SJFbuqebBoT7WtVcIwAijEmcbe"
	    + "9VDCauv9ZlG6uS2zjvZQUSO7SfFqa3xjYqGp_L4QbM7=s900-nd-c-c0xffffffff-rj-k-no";
	public static final CommandException NOT_PLAYING_EXCEPTION;
	static {
		String message = "Load a track with `@{name} ytplay <track's name>` or `@{name} play <url>`!";
		if (BotData.getJDA() != null) {
			NOT_PLAYING_EXCEPTION = new CommandException("Nothing is playing",
			    message.replace("{name}", BotData.getJDA().getSelfUser().getName()), Constants.DISABLED, false);

		} else {
			NOT_PLAYING_EXCEPTION = new CommandException("Nothing is playing", Constants.DISABLED, false);
		}
	}

	private static String getProgress(AudioTrack track) {
		StringBuilder progress = new StringBuilder("——————————");

		if (track.getDuration() == Long.MAX_VALUE)
			return progress.append("—").toString();

		progress.insert(Math.round((float) track.getPosition() / (float) track.getDuration() * 10f), "|");
		return progress.toString();
	}

	private static String formatTimestamp(long timestamp) {
		if (timestamp == Long.MAX_VALUE)
			return "LIVE 🔴";

		return Timestamp.formatTimestamp(timestamp, "HH:mm:ss");
	}

	public static void playing(MessageChannel channel, Guild guild) {
		VoiceChannel vc = guild.getAudioManager().getConnectedChannel();
		if (vc == null)
			throw NOT_PLAYING_EXCEPTION;
		// Checks if LiBot is in a voice channel

		GuildMusicManager gmm = MusicManager.getGuildMusicManager(vc);

		AudioTrack track = gmm.player.getPlayingTrack();
		if (track == null)
			throw NOT_PLAYING_EXCEPTION;
		// Checks if music is playing

		StringBuilder sb = new StringBuilder();
		sb.append("**Title: **[" + track.getInfo().title + "](" + track.getInfo().uri + ")\n"); // Track's title
		sb.append("**Author: **" + track.getInfo().author + "\n"); // Track's author
		sb.append("**Length: **" + formatTimestamp(track.getInfo().length) + "\n\n"); // Track's length

		sb.append("```"); // Start code block
		sb.append(gmm.player.isPaused() ? "\u275A\u275A" : gmm.scheduler.isLoop() ? "\uD83D\uDD01" : "\u25B6"); // Player
		                                                                                                        // status
		sb.append(getProgress(track) + " "); // Track's "progress bar"
		sb.append(formatTimestamp(track.getPosition())); // Player's position
		sb.append("```"); // End code block

		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Information about current song:");
		builder.setColor(Constants.LITHIUM);
		builder.setDescription(sb.toString());

		if (track.getInfo().uri.contains("https://www.youtube.com/watch?v="))
			builder.setThumbnail(String.format(YT_THUMBNAIL, track.getInfo().identifier));
		else
			builder.setThumbnail(DEFAULT_THUMBNAIL);
		// Sets embed's image to thumbnail from Youtube if possible

		channel.sendMessage(builder.build()).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		playing(channel, guild);
	}

	@Override
	public String getInfo() {
		return "Displays detailed information about currently playing song. If the song is from YouTube, "
		    + "it will also display its thumbnail.";
	}

	@Override
	public String getName() {
		return "Playing";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

}
