package com.mz.libot.commands.utilities;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.data.providers.impl.TimerProvider.Timer;
import com.mz.libot.utils.Parser;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class TimerCommand extends Command {

	private static String formatTime(long seconds) {
		StringBuilder time = new StringBuilder();

		long secs = seconds;

		if (secs >= 3600) {
			long hrs = secs / 3600;
			time.append(hrs + " hour(s)");
			secs -= hrs * 3600;
		}
		// Appends hours

		if (secs >= 60) {
			long mins = secs / 60;
			time.append((time.length() > 0 ? ", " : "") + mins + " minute(s)");
			secs -= mins * 60;
		}
		// Appends seconds

		if (secs >= 1) {
			time.append((time.length() > 0 ? ", " : "") + secs + " second(s)");
		}
		// Appends seconds

		return time.toString();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		String text = "Your timer has been activated.";
		long seconds = Parser.parseLong(params.get(0));
		if (params.check(2))
			text = params.get(1);

		if (seconds == 0) {
			ProviderManager.TIMERS.remove(event.getAuthor());
			return;
		}

		if (seconds < 0)
			throw new CommandException(
			    "Sorry, time travel module is currently unavailable. Please try again before `5` hours.",
			    Constants.FAILURE, false);

		ProviderManager.TIMERS.putTimer(event.getAuthor(),
		    new Timer(text, System.currentTimeMillis() + (seconds * 1000)));

		event.getChannel()
		    .sendMessage(
		        BotUtils.buildEmbed("Timer successfully set for **" + formatTime(seconds) + "**.", Constants.SUCCESS))
		    .queue();
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public String getInfo() {
		return "Lets you set a timer that will remind you upon running out. Run `@"
		    + BotData.getName()
		    + " "
		    + getName()
		    + " 0` to cancel the current timer.";
	}

	@Override
	public String getName() {
		return "Timer";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("reminder", "remind");
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("time in seconds", "timer text (optional)");
	}

	@Override
	public int getMinParameters() {
		return 1;
	}

}
