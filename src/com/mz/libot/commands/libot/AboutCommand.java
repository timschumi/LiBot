package com.mz.libot.commands.libot;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.BotData;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.music.MusicManager;
import com.mz.libot.utils.Timestamp;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AboutCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		EmbedBuilder builder = new EmbedBuilder();

		builder.setColor(Constants.LITHIUM);
		builder.setThumbnail(BotData.getJDA().getSelfUser().getAvatarUrl());
		builder.setTitle("About LiBot");
		builder.appendDescription("LiBot is a Discord multi-purpose bot coded and designed with \u2665 by **"
		    + BotData.getOwner().getName()
		    + "#"
		    + BotData.getOwner().getDiscriminator()
		    + "** using [Java](https://java.com/en/) and [JDA](https://github.com/DV8FromTheWorld/JDA/).\n"
		    + HelpCommand.getLinks(event.getJDA()));

		long playing = MusicManager.getGuildMusicManagers()
		    .values()
		    .stream()
		    .filter(gmm -> gmm.player.getPlayingTrack() != null)
		    .count();

		builder.addField("Statistics",
		    "Guild count:** "
		        + BotData.getJDA().getGuilds().size()
		        + "**,"
		        + "\nPrevious command: **"
		        + LiBotCore.STATS.getLastCommandName()
		        + "**,"
		        + "\nTotal of **"
		        + LiBotCore.STATS.getTotalLaunchedCommands()
		        + "** commands launched,"
		        + "\nCurrently streaming music on **"
		        + playing
		        + "** guild"
		        + (playing == 1 ? "" : "s")
		        + ".",
		    true);

		builder.setFooter("v"
		    + Constants.VERSION
		    + " | Last reboot: "
		    + Timestamp.formatTimestamp(LiBotCore.STATS.getStartupTimestamp(), "yyyy-MM-dd HH:mm"), null);

		event.getChannel().sendMessage(builder.build()).queue();
	}

	@Override
	public String getInfo() {
		return "Displays statistics and information about the bot.";
	}

	@Override
	public String getName() {
		return "About";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.LIBOT;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("info", "botinfo");
	}

}
