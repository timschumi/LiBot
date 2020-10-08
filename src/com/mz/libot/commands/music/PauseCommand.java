package com.mz.libot.commands.music;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PauseCommand extends Command {

	public static final MessageEmbed RESUMED_EMBED = BotUtils.buildEmbed("\u25B6 Playback resumed", Constants.LITHIUM);
	public static final MessageEmbed PAUSED_EMBED = BotUtils.buildEmbed("\u23F8 Playback paused", Constants.LITHIUM);

	public static void pause(MessageChannel channel, Guild guild) {
		VoiceChannel vc = guild.getAudioManager().getConnectedChannel();
		if (vc == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if music is playing

		AudioPlayer player = MusicManager.getGuildMusicManager(vc).player;
		if (player.getPlayingTrack() == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if anything is playing

		if (player.isPaused()) {
			player.setPaused(false);
			channel.sendMessage(RESUMED_EMBED).queue();

		} else {
			player.setPaused(true);
			channel.sendMessage(PAUSED_EMBED).queue();
		}
		// Toggles pause of the playback
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		pause(channel, guild);
	}

	@Override
	public String getInfo() {
		return "Pauses/resumes playback.";
	}

	@Override
	public String getName() {
		return "Pause";
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
