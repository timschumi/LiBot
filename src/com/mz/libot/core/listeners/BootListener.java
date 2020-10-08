package com.mz.libot.core.listeners;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.libot.LiBotCore;
import com.mz.libot.core.Bot;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandLauncher;
import com.mz.libot.core.commands.CommandListBuilder;
import com.mz.libot.core.data.properties.impl.FTPPropertyManager;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.handlers.Handlers;
import com.mz.libot.core.listeners.MusicRelatedListener.MusicQueueProvider;
import com.mz.libot.core.managers.StateManager;
import com.mz.libot.core.managers.StateManager.BotState;
import com.mz.libot.core.processes.tasks.Task;
import com.mz.libot.core.processes.tasks.TaskChain;
import com.mz.utils.FormatAs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BootListener extends ListenerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger("Bootstrap");

	private static boolean booted = false;

	public static boolean isReady() {
		return booted;
	}

	@Override
	@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	public void onReady(ReadyEvent event) {
		Bot.getBot().setJDA(event.getJDA());

		LOG.info("Booting up");

		StateManager.setState(BotState.LOADING, event.getJDA());

		getTaskChain(event).executeAll(true);

		LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		LOG.info("\t" + BotData.getName() + " v" + BotData.getVersion());
		LOG.info("\tLaunch successful!");
		LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		booted = true;

		if (ProviderManager.CONFIGURATION.isMaintenance()) {
			StateManager.setState(BotState.MAINTENANCE, event.getJDA());
		} else {
			StateManager.setState(BotState.RUNNING, event.getJDA());
		}

		new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}

			LOG.info("Chunking members..");
			event.getJDA()
			    .getGuilds()
			    .stream()
			    .forEach(g -> g.loadMembers().onSuccess(l -> LOG.debug("Loaded members for guild {}.", g.getId())));
		}).start();
	}

	public static TaskChain getTaskChain(ReadyEvent event) {
		return new TaskChain(new Task[] {

		    new Task(() -> {
			    if (BotData.getProperties() instanceof FTPPropertyManager) {
				    FTPPropertyManager libotProperties = (FTPPropertyManager) BotData.getProperties();

				    try {
					    MusicRelatedListener
					        .loadMusicQueues(MusicQueueProvider.createPropertyManager(libotProperties.getManager()));
				    } catch (IOException e) {
					    e.printStackTrace();
				    }
			    }
		    }, "load_musicqueues"),

		    new Task(() -> {

			    CommandListBuilder cb = new CommandListBuilder();
			    cb.registerAll();

			    List<Command> invalid = cb.getInvalid();

			    if (invalid.size() > 0) {
				    cb.unregisterCommands(invalid);
				    LOG.warn("Unlisted " + invalid.size() + " commands with invalid configuration!");
				    BotData.getOwner().openPrivateChannel().queue(dm -> {
					    StringBuilder sb = new StringBuilder();
					    for (Command command : invalid) {
						    sb.append("\n -" + FormatAs.getFirstUpper(command.getName()));
					    }

					    dm.sendMessage(BotUtils.buildEmbed("Warning",
					        "LiBot has detected invalid command configuration for the following commands:"
					            + sb.toString()
					            + ".\nPlease fix this as those commands will be unlisted and remain inaccessible "
					            + "for as long as you don't fix them.",
					        Constants.WARN)).queue();
				    });

			    }
			    LiBotCore.commands = cb.build();

			    Handlers.COMMAND_HANDLER.registerListener(new CommandLauncher());

		    }, "load_test_commands",
		        new Task[] { new Task(() -> ProviderManager.loadAll(BotData.getProperties()), "load_data") }),

		}, "libot_boot");
	}

}
