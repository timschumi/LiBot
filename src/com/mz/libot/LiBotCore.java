package com.mz.libot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.mz.libot.commands.administrative.ThreadDumpCommand;
import com.mz.libot.core.Bot;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.CommandList;
import com.mz.libot.core.data.properties.impl.FTPPropertyManager;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.core.entities.Stats;
import com.mz.libot.core.entities.ftp.FTPCredentials;
import com.mz.libot.core.ftp.FTPManager;
import com.mz.libot.core.listeners.AutoRoleListener;
import com.mz.libot.core.listeners.BootListener;
import com.mz.libot.core.listeners.EventWaiterListener;
import com.mz.libot.core.listeners.MessageListener;
import com.mz.libot.core.listeners.MusicRelatedListener;
import com.mz.libot.core.listeners.PollListener;
import com.mz.libot.core.listeners.WGMListener;
import com.mz.libot.core.processes.CommandProcess;
import com.mz.libot.core.processes.ProcessManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class LiBotCore extends ListenerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(LiBotCore.class);

	public static final Stats STATS = new Stats();

	@SuppressFBWarnings("MS_CANNOT_BE_FINAL")
	public static CommandList commands;

	public static void run(File propertiesFile) throws IOException, LoginException {
		Properties properties = new Properties();
		try (BufferedReader newReader = Files.newReader(propertiesFile, StandardCharsets.UTF_8)) {
			properties.load(newReader);
		}
		Map<String, String> propertiesMap = new HashMap<>();

		for (Object key : properties.keySet())
			propertiesMap.put(key.toString(), properties.getProperty(key.toString()));

		run(propertiesMap);
	}

	/**
	 * Runs the main JDA service
	 *
	 * @param properties
	 *            the {@link Properties} to use
	 *
	 * @throws LoginException
	 * @throws IOException
	 */
	@SuppressWarnings("null")
	public static void run(Map<String, String> properties) throws IOException, LoginException {
		Constants.DEFAULT_COMMAND_PREFIX = properties.get("BOT_DEFAULTPREFIX");

		FTPCredentials credentials = new FTPCredentials(properties.get("FTP_PASSWORD"), properties.get("FTP_USERNAME"),
		    properties.get("FTP_HOST"), properties.get("FTP_PATH"));

		Bot.getBot()
		    .setProperties(new FTPPropertyManager(new FTPManager(credentials), new File(properties.get("FS_PATH"))))
		    .getInfo()
		    .setName(properties.get("BOT_NAME"))
		    .setOwnerId(Long.parseLong(properties.get("BOT_OWNERID")));

		JDABuilder
		    .create(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
		        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
		    .enableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOTE, CacheFlag.MEMBER_OVERRIDES)
		    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
		    .setToken(properties.get("JDA_TOKEN"))
		    .setMemberCachePolicy(MemberCachePolicy.ALL)
		    .setChunkingFilter(ChunkingFilter.NONE)
		    .setStatus(OnlineStatus.IDLE)
		    .addEventListeners(new AutoRoleListener(), new BootListener(), new EventWaiterListener(),
		        new MessageListener(), new MusicRelatedListener(), new PollListener(), new WGMListener(),
		        new LiBotCore())
		    .build();
	}

	/**
	 * Interrupts the JDA service making it possible to restart it. This will also reset
	 * all non-global variables
	 */
	public static void interrupt() {
		LOG.info("Killing all command threads");
		for (CommandProcess proc : ProcessManager.getProcesses())
			if (!proc.kill())
				LOG.warn("Could not kill process {} with PID of {}.", proc.getCommand().getName(), proc.getPid());

		LOG.info("Shutting down all bot's connections");
		BotData.getJDA().shutdown();

		LOG.info("Interrupting PollManager");
		ProviderManager.POLL.interruptPollService();

		LOG.info("Shutdown successful!");
	}

	@Override
	public void onException(ExceptionEvent event) {
		BotData.getOwner().openPrivateChannel().queue(dm -> {
			if (event.getCause() instanceof OutOfMemoryError) {
				File file = new File("dump-" + System.currentTimeMillis() + ".txt");

				try {
					Files.write(ThreadDumpCommand.createDump().getBytes(StandardCharsets.UTF_8), file);
				} catch (IOException e) {
					LOG.error("Couldn't create the thread dump", e);
					return;
				}

				dm.sendMessage(BotUtils.buildEmbed("Out of memory",
				    "Looks like "
				        + BotData.getName()
				        + " ran out of memory!"
				        + " A detailed list of threads was saved to `"
				        + file.getAbsolutePath()
				        + "`.!",
				    Constants.FAILURE)).queue();

			} else {
				dm.sendMessage(BotUtils.buildEmbed("Something has failed within LiBot",
				    "```" + ExceptionUtils.getStackTrace(event.getCause()) + "```", Constants.FAILURE)).queue();
			}
		});
	}

}
