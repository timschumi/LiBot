package com.mz.libot.core.listeners;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.libot.core.data.providers.ProviderManager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoRoleListener extends ListenerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(AutoRoleListener.class);

	private static void logThrowable(@Nonnull Throwable t, @Nonnull Guild guild, @Nonnull User user) {
		LOG.error("Failed to invoke AutoRole for user {} in guild {}", user.getId(), guild.getId());
		LOG.error("", t);
	}

	@SuppressWarnings("null")
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		if (!BootListener.isReady())
			return;

		try {
			if (ProviderManager.AUTO_ROLE.getData().containsKey(event.getGuild().getIdLong())) {
				if (event.getUser().isBot()) {
					return;
				}

				Role targetRole = event.getGuild()
				    .getRoleById(ProviderManager.AUTO_ROLE.getData().get(event.getGuild().getIdLong()));
				if (targetRole == null)
					return;

				Member member = event.getGuild().getMember(event.getUser());
				if (member == null)
					return;

				event.getGuild()
				    .addRoleToMember(event.getMember(), targetRole)
				    .reason("Autorole")
				    .queue(null, t -> logThrowable(t, event.getGuild(), event.getUser()));
				// Promotes user if guild has configured AutoRole

			}
		} catch (Exception e) {
			logThrowable(e, event.getGuild(), event.getUser());
		}
	}
}
