package com.mz.libot.core.listeners;

import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.data.providers.impl.WGMOProvider;
import com.mz.libot.core.data.providers.impl.WGMOProvider.WelcomeGoodbyeMessageOptions;
import com.mz.libot.utils.TriConsumer;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WGMListener extends ListenerAdapter {

	private enum Type {

		WELCOME((tc, wgmo, e) -> {

			if (wgmo.getWelcomeMessage() != null && wgmo.getWelcomeMessage().length() != 0)
				tc.sendMessage(WGMOProvider.parse(wgmo.getWelcomeMessage(), e.getUser(), e.getGuild())).queue();
		}),

		GOODBYE((tc, wgmo, e) -> {
			if (wgmo.getGoodbyeMessage() != null && wgmo.getGoodbyeMessage().length() != 0)
				tc.sendMessage(WGMOProvider.parse(wgmo.getGoodbyeMessage(), e.getUser(), e.getGuild())).queue();
		});

		private TriConsumer<TextChannel, WelcomeGoodbyeMessageOptions, GenericGuildMemberEvent> action;

		private Type(TriConsumer<TextChannel, WelcomeGoodbyeMessageOptions, GenericGuildMemberEvent> action) {
			this.action = action;
		}

		public void execute(GenericGuildMemberEvent event) {
			WelcomeGoodbyeMessageOptions wgmo = ProviderManager.WGMO.get(event.getGuild());

			TextChannel tc = event.getGuild().getTextChannelById(wgmo.getChannelId());
			if (tc == null || !tc.canTalk())
				return;

			this.action.accept(tc, wgmo, event);
		}

	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		if (!BootListener.isReady())
			return;

		Type.WELCOME.execute(event);
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		if (!BootListener.isReady())
			return;

		Type.GOODBYE.execute(event);
	}

}
