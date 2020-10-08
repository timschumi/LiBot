package com.mz.libot.commands.music;

import java.util.ArrayList;
import java.util.List;

import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class QueueCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		VoiceChannel vc = event.getGuild().getAudioManager().getConnectedChannel();
		if (vc == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if music is playing

		GuildMusicManager gmm = MusicManager.getGuildMusicManager(vc);
		AudioTrack current = gmm.player.getPlayingTrack();
		if (current == null)
			throw new CommandException("Queue is empty!", Constants.DISABLED, false);

		List<StringBuilder> builders = buildQueuePages(gmm, current);

		int page;
		if (params.size() == 1)
			page = params.getAsInteger(0) - 1;
		else
			page = 0;

		if (page < 0) {
			throw new CommandException("Invalid page index", Constants.FAILURE, false);
		} else if (page >= builders.size()) {
			throw new CommandException("Page index out of range", Constants.FAILURE, false);
		}

		EmbedBuilder builder = new EmbedBuilder().setTitle("Queue for " + event.getGuild().getName())
		    .setColor(Constants.LITHIUM);
		StringBuilder queue = builders.get(page);
		if (builders.size() > 1)
			builder.setFooter("Displaying page " + (page + 1) + " out " + builders.size(), null);
		builder.setDescription(queue.toString());

		event.getChannel().sendMessage(builder.build()).queue();
	}

	private static List<StringBuilder> buildQueuePages(GuildMusicManager gmm, AudioTrack current) {
		List<StringBuilder> builders = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		sb.append("\u25B6 **["
		    + current.getInfo().author
		    + ": "
		    + current.getInfo().title
		    + "]("
		    + current.getInfo().uri
		    + ")**\n");

		int i = 1;
		for (AudioTrack track : gmm.scheduler) {
			String overview = "**#"
			    + i
			    + "** ["
			    + track.getInfo().author
			    + ": "
			    + track.getInfo().title
			    + "]("
			    + track.getInfo().uri
			    + ")\n";

			if (sb.length() + overview.length() >= MessageEmbed.TEXT_MAX_LENGTH) {
				builders.add(sb);
				sb = new StringBuilder();
			}

			sb.append(overview);

			i++;
		}
		builders.add(sb);
		return builders;
	}

	@Override
	public String getInfo() {
		return "Lists up to 9 tracks in the current queue."
		    + " If you try to play more music while it is already playing,"
		    + " it will go into this FIFO (First In First Out)"
		    + " queue and be played when the playing track stops.";
	}

	@Override
	public String getName() {
		return "Queue";
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("page");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

}
