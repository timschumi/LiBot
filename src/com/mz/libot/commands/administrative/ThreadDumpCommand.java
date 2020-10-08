package com.mz.libot.commands.administrative;

import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import com.mz.libot.core.BotData;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ThreadDumpCommand extends Command {

	public static String createDump() {
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		StringBuilder dump = new StringBuilder(
		    "Full thread dump for " + BotData.getName() + ":\n\nThread count:" + threads.size());

		int i = 1;
		for (Thread thread : threads) {
			dump.append("\n\n\""
			    + thread.getName()
			    + "\" #"
			    + i
			    + " prio="
			    + thread.getPriority()
			    + " tid=0x"
			    + Hex.encodeHexString(String.valueOf(thread.getId()).getBytes()));
			dump.append("\njava.lang.ThreadState:" + thread.getState().toString());

			for (StackTraceElement element : thread.getStackTrace()) {
				dump.append("\n\tat "
				    + element.getClassName()
				    + "."
				    + element.getMethodName()
				    + "("
				    + (element.isNativeMethod() ? "Native method"
				        : element.getFileName() == null ? "No sources"
				            : element.getFileName() + ":" + element.getLineNumber())
				    + ")");
			}

			i++;
		}

		return dump.toString();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		event.getAuthor()
		    .openPrivateChannel()
		    .queue(dm -> dm.sendFile(createDump().getBytes(), "threaddump.txt").queue());

		event.getMessage().addReaction("✅").queue();
	}

	@Override
	public String getInfo() {
		return "Creates a thread dump for LiBot. Useful for debugging.";
	}

	@Override
	public String getName() {
		return "ThreadDump";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.ADMINISTRATIVE;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("tdump");
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		super.startupCheck(event, params);

		Utils.isOwner(event);
	}

}
