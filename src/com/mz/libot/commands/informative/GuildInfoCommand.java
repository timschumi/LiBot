package com.mz.libot.commands.informative;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.Timestamp;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Guild.MetaData;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GuildInfoCommand extends Command {

	@Nonnull
	private static String parseVerificationLevel(@Nonnull VerificationLevel vl) {
		switch (vl) {
			case NONE:
				return "None";

			case LOW:
				return "Low";

			case MEDIUM:
				return "Medium";

			case HIGH:
				return "\u256F\u00B0\u25A1\u00B0\uFF09\u256F\uFE35 \u253B\u2501\u253B (high)";

			case VERY_HIGH:
				return "\u253B\u2501\u253B \uFF90\u30FD(\u0CA0\u76CA\u0CA0)\u30CE\u5F61\u253B\u2501\u253B (very high)";

			default:
				return "Unknown";
		}

	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		Guild guild = event.getGuild();

		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Information about " + guild.getName());
		builder.setColor(Constants.LITHIUM);
		builder.setThumbnail(guild.getIconUrl());

		builder.addField("ID:", guild.getId(), false);

		if (guild.getIconUrl() != null)
			builder.addField("Icon URL:", guild.getIconUrl(), false);

		CompletableFuture
		    .allOf(appendMembers(guild, builder),
		        guild.retrieveOwner(false)
		            .submit()
		            .thenAccept(owner -> builder.addField("Owner", owner.getUser().getAsTag(), false)))
		    .thenRun(() -> {
			    builder.addField("Verification level:", parseVerificationLevel(guild.getVerificationLevel()), false);

			    builder.addField("Creation date:",
			        Timestamp.formatTimestamp(guild.getTimeCreated(), "yyyy-MM-dd HH:mm"), false);

			    builder.addField("Region:", guild.getRegion().getName(), false);

			    event.getChannel().sendMessage(builder.build()).queue();
		    });
	}

	private static CompletableFuture<Void> appendMembers(Guild guild, EmbedBuilder builder) {
		long bots = guild.getMembers().stream().map(Member::getUser).filter(User::isBot).count();
		long humans = guild.getMembers().size() - bots;

		return guild.retrieveMetaData()
		    .submit()
		    .thenApply(MetaData::getApproximatePresences)
		    .thenAccept(online -> builder.addField("Members:",
		        humans + (bots > 0 ? " humans + " + bots + " bots" : "") + " (" + online + " online)", false));
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.INFORMATIVE;
	}

	@Override
	public String getInfo() {
		return "Displays some information and statistics for the current guild.";
	}

	@Override
	public String getName() {
		return "GuildInfo";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("server", "serverinfo", "guild");
	}

}
