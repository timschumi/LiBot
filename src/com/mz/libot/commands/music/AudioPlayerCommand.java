package com.mz.libot.commands.music;

import java.util.function.Consumer;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.GuildMusicManager;
import com.mz.libot.utils.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AudioPlayerCommand extends Command {

	private static final int POPUP_TIME = 4000;
	private static final Consumer<Message> DELETE_POPUP = m -> {
		try {
			Thread.sleep(POPUP_TIME);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}

		m.delete().complete();
	};

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		VoiceChannel vc = event.getGuild().getAudioManager().getConnectedChannel();
		if (vc == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;
		// Checks if LiBot is in a voice channel

		GuildMusicManager gmm = MusicManager.getGuildMusicManager(vc);

		if (gmm.player.getPlayingTrack() == null)
			throw PlayingCommand.NOT_PLAYING_EXCEPTION;

		Message playerMessage = event.getChannel()
		    .sendMessage(BotUtils.buildEmbed("LiBot AudioPlayer",
		        "\u23EF - Play/pause\n\u23ED - Skip\n\u21A9 - Rewind\n"
		            + "\uD83D\uDD01 - Loop\n\u2139 - Track info\n\u274C - Close",
		        Constants.LITHIUM))
		    .complete();
		playerMessage.addReaction("\u23EF").complete();
		playerMessage.addReaction("\u23ED").complete();
		playerMessage.addReaction("\u21A9").complete();
		playerMessage.addReaction("\uD83D\uDD01").complete();
		playerMessage.addReaction("\u2139").complete();
		playerMessage.addReaction("\u274C").complete();

		EventWaiter ew = new EventWaiter(event.getAuthor(), event.getChannel(), 7200);
		while (true) {
			String response = ew.getReaction(playerMessage).getReactionEmote().getName();

			if (response.equals("\u23EF")) {
				// Play/pause

				if (gmm.player.isPaused()) {
					gmm.player.setPaused(false);
					event.getChannel().sendMessage(PauseCommand.RESUMED_EMBED).queue(DELETE_POPUP);

				} else {
					gmm.player.setPaused(true);
					event.getChannel().sendMessage(PauseCommand.PAUSED_EMBED).queue(DELETE_POPUP);
				}

			} else if (response.equals("\u23ED")) {
				// Skip

				AudioTrack track = gmm.scheduler.nextTrack();
				if (track == null) {
					event.getChannel()
					    .sendMessage(
					        BotUtils.buildEmbed("Skipped", "You skip into a trackless void..", Constants.LITHIUM))
					    .queue(DELETE_POPUP);
					break;
				}

				event.getChannel()
				    .sendMessage(BotUtils.buildEmbed("Skipped",
				        String.format(Utils.NEW_TRACK_MESSAGE, BotUtils.escapeMarkdown(track.getInfo().title),
				            BotUtils.escapeMarkdown(track.getInfo().author)),
				        Constants.SUCCESS))
				    .queue(DELETE_POPUP);

			} else if (response.equals("\u21A9")) {
				// Rewind
				AudioTrack track = gmm.player.getPlayingTrack();
				if (track == null)
					continue;

				if (!track.isSeekable())
					event.getChannel()
					    .sendMessage(
					        BotUtils.buildEmbed("Sorry, but LiBot can't rewind that track!", Constants.DISABLED))
					    .queue(DELETE_POPUP);

				track.setPosition(0);

			} else if (response.equals("\uD83D\uDD01")) {
				// Loop
				if (gmm.scheduler.isLoop()) {
					gmm.scheduler.setLoop(false);
					event.getChannel()
					    .sendMessage(BotUtils.buildEmbed("Loop OFF",
					        "\u25B6 Now playing tracks from the queue normally.", Constants.SUCCESS))
					    .queue(DELETE_POPUP);
					continue;
				}

				AudioTrack track = gmm.player.getPlayingTrack();
				if (track == null)
					continue;
				// Checks if anything is playing

				if (!track.isSeekable())
					event.getChannel()
					    .sendMessage(BotUtils.buildEmbed("Sorry, but LiBot can't loop over currently playing track.",
					        Constants.DISABLED))
					    .queue(DELETE_POPUP);

				gmm.scheduler.setLoop(true);
				event.getChannel()
				    .sendMessage(BotUtils.buildEmbed("Loop ON",
				        "\uD83D\uDD01 Now looping over **" + track.getInfo().title + "**.", Constants.SUCCESS))
				    .queue(DELETE_POPUP);

			} else if (response.equals("\u2139")) {
				// Track info
				try {
					LiBotCore.commands.getById(1171089422).execute(event, params);
				} catch (CommandException ce) {}

			} else if (response.equals("\u274C")) {
				// Exit
				break;

			} else {
				playerMessage.getReactions()
				    .stream()
				    .filter(MessageReaction::isSelf)
				    .forEach(mr -> mr.removeReaction().queue());
			}
		}

		playerMessage.delete().queue();
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getInfo() {
		return "Gives you a more visual representation of LiBot's audio commands.";
	}

	@Override
	public String getName() {
		return "AudioPlayer";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("ap");
	}

}
