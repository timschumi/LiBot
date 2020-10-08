package com.mz.libot.commands.music;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.startup.MissingParametersException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.core.music.entities.GuildMusicManager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PlayCommand extends Command {

	public static final MessageEmbed NOT_PLAYING_ERROR = BotUtils
	    .buildEmbed(Constants.FAILURE_EMOJI + " Nothing is playing!", Constants.FAILURE);

	public static void play(TextChannel channel, Parameters params, Guild guild, Member member) {
		GuildMusicManager gmm = MusicManager.getGuildMusicManager(guild);
		if (!params.check()) {
			if (gmm != null && gmm.player.isPaused()) {
				gmm.player.setPaused(false);
				channel.sendMessage(PauseCommand.RESUMED_EMBED).queue();
				return;
			}

			throw new MissingParametersException();
		}

		Utils.easyPlay(params.get(0), member, channel);
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		TextChannel channel = event.getChannel();
		User author = event.getAuthor();
		Guild guild = event.getGuild();
		Member member = guild.getMember(author);

		play(channel, params, guild, member);
	}

	@Override
	public String getInfo() {
		return "Plays a track from an URL _(supports almost any music website)_."
		    + " Using `scsearch:query` as track's URL (sc stands for SoundCloud) or such,"
		    + " you can even search and play music from providers other than Youtube! Also, "
		    + "you can use this command to resume playback.";
	}

	@Override
	public String getName() {
		return "Play";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("URL (optional)");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.canUseMusic(event.getMember());
	}

}
