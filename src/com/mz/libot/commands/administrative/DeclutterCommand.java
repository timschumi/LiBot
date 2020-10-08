package com.mz.libot.commands.administrative;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Triple;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.listeners.EventWaiterListener;
import com.mz.libot.core.processes.CommandProcess;
import com.mz.libot.core.processes.ProcessManager;
import com.mz.libot.utils.MessageLock;
import com.mz.utils.Counter;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class DeclutterCommand extends Command {

	private static class EventWaitersGetter extends EventWaiterListener {

		static Map<CommandProcess, Triple<Predicate<GenericEvent>, MessageLock<GenericEvent>, Predicate<Void>>> getEventWaiters() {
			return EventWaiterListener.EVENT_WAITERS;
		}

	}

	@SuppressWarnings("null")
	public static void performCleanup(MessageChannel channel) {
		Counter processes = new Counter();
		for (CommandProcess proc : ProcessManager.getProcesses()) {
			if (proc.getAuthor() == null || proc.getChannel() == null && proc.kill()) {
				processes.count();
			}
		}
		// Kills all obsolete processes

		int waiters = 0;
		List<CommandProcess> ewDeletionQueue = new ArrayList<>();
		for (Entry<CommandProcess, Triple<Predicate<GenericEvent>, MessageLock<GenericEvent>, Predicate<Void>>> entry : EventWaitersGetter
		    .getEventWaiters()
		    .entrySet()) {
			CommandProcess proc = entry.getKey();

			if (!proc.getThread().isAlive()) {
				waiters++;
				ewDeletionQueue.add(proc);
				continue;
			}
			// Checks if the process still exists

			try {
				if (proc.getChannel() == null || !proc.getGuild().isMember(proc.getAuthor())
				    || entry.getValue().getRight().test(null)) {
					throw new Exception();
				}

			} catch (Exception e) {
				ewDeletionQueue.add(proc);
				waiters++;
				continue;
			}
			// Checks if the message assigned to this yes/no waiter is still accessible
		}

		ewDeletionQueue.forEach(p -> {

			if (p.kill())
				processes.count();
			EventWaitersGetter.getEventWaiters().remove(p);

		});
		// Clears obsolete event waiters

		int providers = ProviderManager.cleanAll(BotData.getJDA());

		if (channel == null) {
			return;

		} else if (waiters == 0 && processes.getCount() == 0 && providers == 0) {
			channel.sendMessage(BotUtils.buildEmbed("Declutterification finished", "No obsolete objects were found!",
			    Constants.SUCCESS)).queue();

		} else {
			StringBuilder sb = new StringBuilder();

			if (waiters != 0)
				sb.append("\nRemoved " + waiters + " obsolete event waiter" + (waiters == 1 ? "" : "s") + ".");

			if (processes.getCount() != 0)
				sb.append("\nKilled "
				    + processes.getCount()
				    + " obsolete process"
				    + (processes.getCount() == 1 ? "" : "es")
				    + ".");

			if (providers != 0)
				sb.append("\nRemoved " + providers + " unused data element" + (providers == 1 ? "" : "s") + ".");

			channel.sendMessage(BotUtils.buildEmbed("Cleanup finished", sb.toString().trim(), Constants.LITHIUM))
			    .queue();
		}
	}

	public static void cleanup(MessageChannel channel) {
		performCleanup(channel);
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();

		cleanup(channel);
	}

	@Override
	public String getInfo() {
		return "Performs a manual declutterification. Declutterification will remove obsolete data and processes in order to free more memory.";
	}

	@Override
	public String getName() {
		return "Declutter";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.ADMINISTRATIVE;
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.isOwner(event);
	}

}
