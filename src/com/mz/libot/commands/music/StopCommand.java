package com.mz.libot.commands.music;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.GuildMusicManager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class StopCommand extends Command {

	public static void stop(MessageChannel channel, Guild guild) {
		AudioManager am = guild.getAudioManager();

		VoiceChannel vc = am.getConnectedChannel();
		if (vc == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if music is playing

		GuildMusicManager gmm = MusicManager.getGuildMusicManager(vc);

		boolean playing = gmm.player.getPlayingTrack() != null;
		boolean queue = gmm.scheduler.iterator().hasNext();

		MusicManager.stopPlayback(vc);

		am.closeAudioConnection();
		channel.sendMessage(BotUtils.buildEmbed("Playback stopped",
		    "Disconnected from the voice channel"
		        + (playing ? " & stopped currently playing track" : "")
		        + (queue ? " & cleared the queue." : "."),
		    Constants.SUCCESS)).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		stop(channel, guild);
	}

	@Override
	public String getInfo() {
		return "Stops the playback, clears the queue and disconnects LiBot from the voice channel. "
		    + "If music is no longer playing, you can use this command to 'kick' "
		    + BotData.getName()
		    + " out of the voice channel.";
	}

	@Override
	public String getName() {
		return "Stop";
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
