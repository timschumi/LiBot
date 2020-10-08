package com.mz.libot.commands.administrative;

import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.processes.CommandProcess;
import com.mz.libot.core.processes.ProcessManager;
import com.mz.utils.FormatAs;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class KillProcessCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		if (!params.check()) {
			StringBuilder sb = new StringBuilder();

			Set<CommandProcess> procList = ProcessManager.getProcesses();

			if (procList.isEmpty()) {
				sb.append("_No running processes_\n");

			} else {
				for (CommandProcess proc : procList) {
					if (proc.getCommand() == null)
						continue;

					if (proc.getAuthor() == null) {
						sb.append("_?");

					} else {
						sb.append("_" + (proc.getAuthor().getId().equals(event.getAuthor().getId()) ? "\\*" : ""));
					}

					sb.append(proc.getCommand().getName() + "_ - PID: `" + proc.getPid() + "`\n");
				}
			}

			sb.append("\n_\\* - command launched by you\n\\? - obsolete command_");

			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Running processes", sb.toString(), Constants.SUCCESS))
			    .queue();
			return;
		}

		Utils.isOwner(event);

		if (!NumberUtils.isParsable(params.get(0))) {
			event.getChannel().sendMessage("PID is a number!").queue();
			throw new CommandException(false);
		}

		Set<CommandProcess> procList = ProcessManager.getProcesses();
		CommandProcess cp = procList.stream()
		    .filter(proc -> proc.getPid() == params.getAsInteger(0))
		    .findAny()
		    .orElse(null);

		if (cp != null) {
			if (!cp.kill()) {
				throw new CommandException("Failure",
				    "Could not kill process "
				        + FormatAs.getFirstUpper(cp.getCommand().getName())
				        + " with PID of `"
				        + cp.getPid()
				        + "`!",
				    Constants.FAILURE, false);
			}
			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Success!",
			        FormatAs.getFirstUpper(cp.getCommand().getName())
			            + " with PID of `"
			            + cp.getPid()
			            + "` was successfully killed!",
			        Constants.SUCCESS))
			    .queue();

		} else {
			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("Not found",
			        "Process with PID of `" + params.get(0) + "` was not found!", Constants.DISABLED))
			    .queue();
		}
	}

	@Override
	public String getInfo() {
		return "Kills a running process."
		    + " If no PID is provided, a list of running processes with their PIDs will be shown."
		    + " Killing a process is only available to bot's owner while"
		    + " only displaying running processes is available to anyone.";
	}

	@Override
	public String getName() {
		return "KillProcess";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.ADMINISTRATIVE;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("pid");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("kill");
	}

}
