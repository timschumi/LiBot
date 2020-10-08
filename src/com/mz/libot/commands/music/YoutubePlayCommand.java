package com.mz.libot.commands.music;

import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class YoutubePlayCommand extends Command {

	public static void ytplay(TextChannel channel, Parameters params, Member member, User user) {
		String query = params.get(0);
		// Adds parameters and checks if they're correct

		AudioTrack selected = Utils.selectTrack(Utils.findTracks(query, MusicManager.AUDIO_PLAYER_MANAGER), channel,
		    user);

		Utils.easyPlay("https://www.youtube.com/watch?v=" + selected.getIdentifier(), member, channel);
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		TextChannel channel = event.getChannel();
		User author = event.getAuthor();
		Member member = event.getGuild().getMember(event.getAuthor());

		ytplay(channel, params, member, author);
	}

	@Override
	public String getInfo() {
		return "Queries Youtube and lets you play a track (you can select it using a cool interface).";
	}

	@Override
	public String getName() {
		return "YoutubePlay";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("query");
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.canUseMusic(event.getMember());
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("ytplay", "youtube", "yt");
	}

}
