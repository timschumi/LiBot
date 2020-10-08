package com.mz.libot.core.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.libot.commands.administrative.DeclutterCommand;
import com.mz.libot.commands.administrative.EvaluateCommand;
import com.mz.libot.commands.administrative.GlobalDisableCommand;
import com.mz.libot.commands.administrative.GlobalEnableCommand;
import com.mz.libot.commands.administrative.KillProcessCommand;
import com.mz.libot.commands.administrative.MaintenanceCommand;
import com.mz.libot.commands.administrative.ShutdownCommand;
import com.mz.libot.commands.administrative.ThreadDumpCommand;
import com.mz.libot.commands.automod.AutoRoleCommand;
import com.mz.libot.commands.automod.AutoRoleRemoveCommand;
import com.mz.libot.commands.customization.DisableCommand;
import com.mz.libot.commands.customization.DjRoleCommand;
import com.mz.libot.commands.customization.EnableCommand;
import com.mz.libot.commands.customization.SetPrefixCommand;
import com.mz.libot.commands.customization.WelcomeGoodbyeMessageOptionsCommand;
import com.mz.libot.commands.games.AkinatorCommand;
import com.mz.libot.commands.games.BlackjackCommand;
import com.mz.libot.commands.games.GuessANumberCommand;
import com.mz.libot.commands.games.MathQuizCommand;
import com.mz.libot.commands.games.RockPaperScissorsCommand;
import com.mz.libot.commands.games.UnoCommand;
import com.mz.libot.commands.informative.AvatarCommand;
import com.mz.libot.commands.informative.GuildInfoCommand;
import com.mz.libot.commands.informative.PermsissionsCommand;
import com.mz.libot.commands.informative.UserInfoCommand;
import com.mz.libot.commands.libot.AboutCommand;
import com.mz.libot.commands.libot.GetInviteCommand;
import com.mz.libot.commands.libot.HelpCommand;
import com.mz.libot.commands.messaging.BlockUserCommand;
import com.mz.libot.commands.messaging.FeedbackCommand;
import com.mz.libot.commands.messaging.GetBlockedUsersCommand;
import com.mz.libot.commands.messaging.MailCommand;
import com.mz.libot.commands.messaging.MessageCommand;
import com.mz.libot.commands.messaging.UnblockUserCommand;
import com.mz.libot.commands.moderation.AddRoleCommand;
import com.mz.libot.commands.moderation.BackupGuildCommand;
import com.mz.libot.commands.moderation.BanCommand;
import com.mz.libot.commands.moderation.GetRolesCommand;
import com.mz.libot.commands.moderation.KickCommand;
import com.mz.libot.commands.moderation.RemoveRoleCommand;
import com.mz.libot.commands.moderation.RestoreGuildCommand;
import com.mz.libot.commands.moderation.SetRoleCommand;
import com.mz.libot.commands.money.LeaderboardCommand;
import com.mz.libot.commands.money.MoneyCommand;
import com.mz.libot.commands.money.TransferCommand;
import com.mz.libot.commands.music.AudioPlayerCommand;
import com.mz.libot.commands.music.ClearQueueCommand;
import com.mz.libot.commands.music.LoopCommand;
import com.mz.libot.commands.music.PauseCommand;
import com.mz.libot.commands.music.PlayCommand;
import com.mz.libot.commands.music.PlayingCommand;
import com.mz.libot.commands.music.QueueCommand;
import com.mz.libot.commands.music.RewindCommand;
import com.mz.libot.commands.music.SeekCommand;
import com.mz.libot.commands.music.ShuffleCommand;
import com.mz.libot.commands.music.SkipCommand;
import com.mz.libot.commands.music.StopCommand;
import com.mz.libot.commands.music.YoutubePlayCommand;
import com.mz.libot.commands.searching.GoogleCommand;
import com.mz.libot.commands.searching.UrbanDictionaryCommand;
import com.mz.libot.commands.utilities.CalculatorCommand;
import com.mz.libot.commands.utilities.ChatbotCommand;
import com.mz.libot.commands.utilities.DiceCommand;
import com.mz.libot.commands.utilities.DumpMessagesCommand;
import com.mz.libot.commands.utilities.GetIdCommand;
import com.mz.libot.commands.utilities.LennyCommand;
import com.mz.libot.commands.utilities.PingCommand;
import com.mz.libot.commands.utilities.PollCommand;
import com.mz.libot.commands.utilities.PurgeCommand;
import com.mz.libot.commands.utilities.TextToImageCommand;
import com.mz.libot.commands.utilities.TimerCommand;

public class CommandListBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(CommandListBuilder.class);

	public static final Collection<Command> OFFICIAL_COMMANDS;
	static {
		List<Command> commands = new ArrayList<>();

		// Administrative
		commands.add(new EvaluateCommand());
		commands.add(new KillProcessCommand());
		commands.add(new MaintenanceCommand());
		commands.add(new ShutdownCommand());
		commands.add(new DeclutterCommand());
		commands.add(new ThreadDumpCommand());
		commands.add(new GlobalDisableCommand());
		commands.add(new GlobalEnableCommand());

		// AutoMod
		commands.add(new AutoRoleCommand());
		commands.add(new AutoRoleRemoveCommand());

		// Customization
		commands.add(new EnableCommand());
		commands.add(new DisableCommand());
		commands.add(new SetPrefixCommand());
		commands.add(new DjRoleCommand());
		commands.add(new WelcomeGoodbyeMessageOptionsCommand());

		// Games
		commands.add(new BlackjackCommand());
		commands.add(new RockPaperScissorsCommand());
		commands.add(new GuessANumberCommand());
		commands.add(new MathQuizCommand());
		commands.add(new AkinatorCommand());
		commands.add(new UnoCommand());

		// Informative
		commands.add(new UserInfoCommand());
		commands.add(new AvatarCommand());
		commands.add(new GuildInfoCommand());
		commands.add(new PermsissionsCommand());

		// LiBot
		commands.add(new AboutCommand());
		commands.add(new HelpCommand());
		commands.add(new GetInviteCommand());

		// Messaging
		commands.add(new MessageCommand());
		commands.add(new FeedbackCommand());
		commands.add(new MailCommand());
		commands.add(new BlockUserCommand());
		commands.add(new UnblockUserCommand());
		commands.add(new GetBlockedUsersCommand());

		// Moderator
		commands.add(new AddRoleCommand());
		commands.add(new BanCommand());
		commands.add(new GetRolesCommand());
		commands.add(new KickCommand());
		commands.add(new RemoveRoleCommand());
		commands.add(new SetRoleCommand());
		commands.add(new BackupGuildCommand());
		commands.add(new RestoreGuildCommand());

		// Money
		commands.add(new TransferCommand());
		commands.add(new MoneyCommand());
		commands.add(new LeaderboardCommand());

		// Music
		commands.add(new PlayingCommand());
		commands.add(new PauseCommand());
		commands.add(new PlayCommand());
		commands.add(new SkipCommand());
		commands.add(new StopCommand());
		commands.add(new YoutubePlayCommand());
		commands.add(new QueueCommand());
		commands.add(new RewindCommand());
		commands.add(new LoopCommand());
		commands.add(new AudioPlayerCommand());
		commands.add(new SeekCommand());
		commands.add(new ShuffleCommand());
		commands.add(new ClearQueueCommand());

		// Searching
		commands.add(new UrbanDictionaryCommand());
		commands.add(new GoogleCommand());

		// Utilities
		commands.add(new DumpMessagesCommand());
		commands.add(new PurgeCommand());
		commands.add(new GetIdCommand());
		commands.add(new PingCommand());
		commands.add(new PollCommand());
		commands.add(new ChatbotCommand());
		commands.add(new TextToImageCommand());
		commands.add(new DiceCommand());
		commands.add(new LennyCommand());
		commands.add(new TimerCommand());
		commands.add(new CalculatorCommand());

		OFFICIAL_COMMANDS = Collections.unmodifiableCollection(commands);
	}

	private final Collection<Command> registered;

	/**
	 * Creates a list of executable commands
	 */
	public CommandListBuilder() {
		this.registered = new HashSet<>();
	}

	/**
	 * Registers a command.
	 *
	 * @param command
	 *
	 * @return self, used for chaining
	 */
	public CommandListBuilder registerCommand(Command command) {
		this.registered.add(command);
		return this;
	}

	/**
	 * Registers a list of commands.
	 *
	 * @param commands
	 *
	 * @return self, used for chaining
	 */
	public CommandListBuilder registerCommands(Collection<Command> commands) {
		this.registered.addAll(commands);
		return this;
	}

	/**
	 * Unregisters a command.
	 *
	 * @param command
	 *
	 * @return self, used for chaining
	 *
	 * @see CommandListBuilder#unregisterCommands(Collection)
	 */
	public CommandListBuilder unregisterCommand(Command command) {
		this.registered.remove(command);
		return this;
	}

	/**
	 * Unregisters a collection of commands.
	 *
	 * @param commands
	 *
	 * @return self, used for chaining
	 *
	 * @see CommandListBuilder#unregisterCommand(Command)
	 */
	public CommandListBuilder unregisterCommands(Collection<Command> commands) {
		this.registered.removeAll(commands);
		return this;
	}

	/**
	 * Registers all official LiBot commands.
	 *
	 * @return self, used for chaining
	 */
	public CommandListBuilder registerAll() {
		this.registered.addAll(OFFICIAL_COMMANDS);
		return this;
	}

	/**
	 * @return an unmodifiable collection of all currently registered commands.
	 */
	public Collection<Command> getRegistered() {
		return Collections.unmodifiableCollection(this.registered);
	}

	/**
	 * Creates a new CommandList for ease of access.
	 *
	 * @return Commands
	 */
	public CommandList build() {
		return new CommandList(this);
	}

	/**
	 * Returns a list of invalidly configured commands and logs configuration mistakes as
	 * warnings into the standard error stream.
	 *
	 * @return an unmodifiable list of invalid commands
	 */
	public List<Command> getInvalid() {
		List<Command> invalid = new ArrayList<>();

		for (Command command : this.registered) {
			{
				if (command.getName().equals("")) {
					LOG.warn("Missing name for command in class {}", command.getClass().getCanonicalName());

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
					continue;
				}

				if (!Character.isUpperCase(command.getName().codePointAt(0))) {
					LOG.warn("Bad name for command {}; command's name must start with an upper case letter.",
					    command.getClass().getCanonicalName());

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
				}

				if (!StringUtils.isAlpha(command.getName())) {
					LOG.warn("Bad name for command "
					    + command.getClass().getCanonicalName()
					    + "; command's name must not contain non-alphabetic characters.");

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
				}

				if (command.getName().equals(command.getClass().getSimpleName() + "Command")) {
					LOG.warn("Bad name for command "
					    + command.getClass().getCanonicalName()
					    + "; command's name must be same as its class name + 'Command'.");

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
					continue;
				}
			}
			// Tests command's name

			{
				String[] aliases = command.getAliases();
				for (String alias : aliases) {
					if (!alias.equals(alias.toLowerCase())) { // NOSONAR this is checking if the alias is all-lowercase
						LOG.warn("Bad alias for command {}; alias '{}' must be all in lower-case.", command.getName(),
						    alias);

						if (!invalid.contains(command))
							invalid.add(command);

						break;
					}

					if (!StringUtils.isAlpha(command.getName())) {

						LOG.warn("Bad alias for command {}; alias '{}' must not contain non-alphabetic characters.",
						    command.getName(), alias);

						if (!invalid.contains(command))
							invalid.add(command);

						break;
					}
				}
			}
			// Tests command's aliases

			{
				if (command.getInfo().equals("")) {
					LOG.warn("Missing description for command {}.", command.getName());

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
				}

				if (!command.getInfo().endsWith(".") && !command.getInfo().endsWith("!")
				    && !command.getInfo().endsWith("?")) {
					LOG.warn("Bad description for command "
					    + command.getName()
					    + "; description must end with a '.', an '!' or a '?'.");

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
				}
			}
			// Tests command's description

			{
				if (command.getUsage(null).equals("")) {
					LOG.warn("Missing usage for command {}.", command.getName());

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
					continue;
				}
			}
			// Tests command's usage message

			{
				if (!command.getClass()
				    .getPackage()
				    .getName()
				    .toLowerCase()
				    .endsWith(command.getCategory().toString().toLowerCase())) {
					LOG.warn("Bad category for command {}; category must be the same as the lowest package's name.",
					    command.getName());

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
				}
			}
			// Tests command's category

			{
				if (command.getRatelimit() < 0) {
					LOG.warn("Bad ratelimit time for command {}; ratelimit time must not not be less than 0.",
					    command.getName());

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
				}
			}
			// Tests command's ratelimit specification

			{
				if (command.getMinParameters() < 0) {
					LOG.warn("Bad minimal parameters for command {}; minimal parameters must not not be less than 0.",
					    command.getName());

					if (!invalid.contains(command)) {
						invalid.add(command);
					}
				}
			}
			// Tests command's minimal parameters specification

			if (!invalid.contains(command)) {
				LOG.debug("Validated command '{}'.", command.getName());
			}
		}

		return Collections.unmodifiableList(invalid);
	}
}
