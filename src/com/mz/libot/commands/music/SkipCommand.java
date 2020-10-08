package com.mz.libot.commands.music;

import com.mz.libot.core.BotUtils;
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

public class SkipCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		VoiceChannel vc = event.getGuild().getAudioManager().getConnectedChannel();
		if (vc == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if music is playing

		int amount;
		if (params.size() == 1)
			amount = params.getAsInteger(0);
		else
			amount = 0;

		if (amount < 1)
			throw new CommandException("Invalid amount of tracks", Constants.FAILURE, false);

		AudioTrack track = MusicManager.getGuildMusicManager(vc).scheduler.skipTrack(amount);

		event.getChannel()
		    .sendMessage(BotUtils.buildEmbed("Skipped " + amount + " track" + (amount != 1 ? "s" : ""),
		        track == null ? "You skip into a trackless void.."
		            : String.format(Utils.NEW_TRACK_MESSAGE, BotUtils.escapeMarkdown(track.getInfo().title),
		                BotUtils.escapeMarkdown(track.getInfo().author)),
		        track == null ? Constants.LITHIUM : Constants.SUCCESS))
		    .queue();
	}

	@Override
	public String getInfo() {
		return "Skips to the next track in queue, stops playing if there are no more queued tracks.";
	}

	@Override
	public String getName() {
		return "Skip";
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("amount of tracks");
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
