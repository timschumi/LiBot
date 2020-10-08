package com.mz.libot.core.listeners;

import com.mz.libot.commands.utilities.PollCommand.Poll;
import com.mz.libot.core.BotData;
import com.mz.libot.core.Constants;
import com.mz.libot.core.data.providers.ProviderManager;

import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PollListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		if (!BootListener.isReady())
			return;

		if (ProviderManager.POLL.getData().containsKey(event.getGuild().getIdLong())
		    && event.getUser().getIdLong() != BotData.getJDA().getSelfUser().getIdLong()) {
			// If the guild that send this event has a poll

			event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {

				if (message.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()
				    && message.getEmbeds().size() == 1 && Poll.isPoll(message.getEmbeds().get(0))
				    && !ProviderManager.POLL.getData().get(event.getGuild().getIdLong()).allowsMoreVotes()) {
					// If the message is a poll that disallows multiple votes for users

					message.getReactions().forEach(r -> {

						if (!r.getReactionEmote().getName().equals(event.getReactionEmote().getName()))
							r.removeReaction(event.getUser()).queue();
						// Removes every other vote (reaction, actually) cast by that user

					});

				}

			}, Constants.EMPTY_FAIL_CONSUMER);
		}
		/*
		 * Checks if this is a poll and user has voted more than one time while they weren't
		 * allowed to
		 */
	}

}
