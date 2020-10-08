package com.mz.libot.core.handlers.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.libot.commands.administrative.DeclutterCommand;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.exceptions.runtime.NumberOverflowException;
import com.mz.libot.core.commands.exceptions.runtime.TimeoutException;
import com.mz.libot.core.commands.exceptions.startup.MemberInsufficientPermissionsException;
import com.mz.libot.core.commands.exceptions.startup.MissingParametersException;
import com.mz.libot.core.commands.exceptions.startup.NotDjException;
import com.mz.libot.core.commands.exceptions.startup.NotOwnerException;
import com.mz.libot.core.commands.exceptions.startup.UnpredictedStateException;
import com.mz.libot.core.commands.exceptions.startup.UsageException;
import com.mz.libot.core.commands.ratelimits.RatelimitsManager;
import com.mz.libot.core.handlers.Handler;
import com.mz.libot.core.processes.CommandProcess;
import com.mz.utils.FormatAs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;

public class ExceptionHandler implements Handler<ExceptionHandlerParameter> {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);
	private static final Set<Throwable> CACHE = Collections.newSetFromMap(new WeakHashMap<Throwable, Boolean>(50));

	public static final Throwable unpackThrowable(Throwable t) {
		Throwable tt = ExceptionUtils.getRootCause(t);
		if (tt == null)
			return t;

		return tt;
	}

	@Override
	public void handle(ExceptionHandlerParameter param) {
		Throwable t = unpackThrowable(param.getThrowable());

		CommandProcess proc = CommandProcess.valueOf(Thread.currentThread());
		if (!HandleThrowable.handleThrowable(param.getCommand(), t, param.getEvent())) {
			String errorCode = Integer.toHexString(t.hashCode());

			// Sends error log to the bot's owner in case a command fails
			if (CACHE.stream().noneMatch(ce -> Arrays.deepEquals(ce.getStackTrace(), t.getStackTrace()))) {
				// If the exception has already been reported

				BotData.getOwner()
				    .openPrivateChannel()
				    .queue(dm -> dm.sendMessage(BotUtils.buildEmbed("Failed to execute " + param.getCommand().getName(),
				        "Error code: `" + errorCode + "`\n```" + ExceptionUtils.getStackTrace(t) + "```",
				        "Reported by " + param.getEvent().getAuthor().getId(), Constants.FAILURE)).queue());

				CACHE.add(t);
			}

			LOG.error("Unhandled exception in {}, pid={}, err={}", param.getCommand().getName(), proc.getPid(),
			    errorCode);
			LOG.error("", param.getThrowable());

			if (param.getEvent().getChannel().canTalk()) {
				param.getEvent()
				    .getChannel()
				    .sendMessage(BotUtils.buildEmbed("// FAILURE //", "LiBot ran into an unknown error (Error code: `0x"
				        + errorCode
				        + "`). If this repeats, please help us out [here](https://bitbucket.org/markozajc/libot/issues/new).",
				        Constants.FAILURE))
				    .queue();
			}

		} else {
			LOG.debug("Automatically handled an exception in {}, pid={}", param.getCommand().getName(), proc.getPid());
		}

		if (param.getCommand().getRatelimit() != 0 && shouldRatelimit(t))
			RatelimitsManager.getRatelimits(param.getCommand()).register(param.getEvent().getAuthor().getId());
	}

	public static class HandleThrowable {

		public static boolean handleThrowable(Command command, Throwable t, GuildMessageReceivedEvent event) {
			if (t instanceof Error) {
				return HandleError.handleError((Error) t, event);
			}

			if (t instanceof Exception) {
				return HandlePlain.handleException(command, (Exception) t, event);
			}

			return true;
		}

		private static class HandleError {

			public static boolean handleError(Error e, GuildMessageReceivedEvent event) {
				// Finds the root exception type
				if (e instanceof VirtualMachineError) {
					return HandleVirtualMachineError.handleVirtualMachineError((VirtualMachineError) e, event);
				}

				return false;
			}

			private static class HandleVirtualMachineError {

				public static boolean handleVirtualMachineError(VirtualMachineError vme,
				                                                GuildMessageReceivedEvent event) {
					// Finds the root exception type
					if (vme instanceof OutOfMemoryError) {
						handleOutOfMemoryError(event);
						return true;
					}

					return false;
				}

				private static void handleOutOfMemoryError(GuildMessageReceivedEvent event) {
					if (event.getChannel().canTalk()) {
						event.getChannel()
						    .sendMessage(BotUtils.buildEmbed("// RAM LOW //",
						        "LiBot was unable to launch this command "
						            + "because there is a possibility that it would halt its vital processes. "
						            + "Please wait some time or try launching someting else!",
						        Constants.LITHIUM))
						    .queue();
					}

					DeclutterCommand.performCleanup(null);
				}

			}

		}

		@SuppressFBWarnings("NM_CLASS_NOT_EXCEPTION")
		private static class HandlePlain {

			public static boolean handleException(Command command, Exception e, GuildMessageReceivedEvent event) {
				// Finds the root exception type
				if (e instanceof InterruptedException) {
					handleInterruptedException(command, event);
					return true;
				}

				if (e instanceof RuntimeException) {
					return HandleRuntime.handleRuntimeException(command, (RuntimeException) e, event);
				}

				return false;

			}

			private static void handleInterruptedException(Command command, GuildMessageReceivedEvent event) {
				// Reports if command failed because the thread was interrupted

				if (event.getChannel().canTalk()) {
					event.getChannel()
					    .sendMessage(BotUtils.buildEmbed(null,
					        FormatAs.getFirstUpper(command.getName()) + " has been killed.", Constants.DISABLED))
					    .queue();
				}
			}

			private static class HandleRuntime {

				public static boolean handleRuntimeException(Command command, RuntimeException re,
				                                             GuildMessageReceivedEvent event) {
					// Finds the root exception type
					if (re instanceof CommandException) {
						return HandleCommand.handleCommandException(command, (CommandException) re, event);
					}

					if (re instanceof ErrorResponseException) {
						handleErrorResponseException((ErrorResponseException) re, event);
						return true;
					}

					if (re instanceof IllegalArgumentException) {
						return HandleIllegalArgument.handleIllegalArgumentException((IllegalArgumentException) re,
						    event);
					}

					if (re instanceof PermissionException) {
						return HandlePermission.handlePermissionException((PermissionException) re, event);
					}

					return false;

				}

				private static class HandleCommand {

					public static boolean handleCommandException(Command command, CommandException ce,
					                                             GuildMessageReceivedEvent event) {
						// Finds the root exception type
						if (ce instanceof CanceledException) {
							return true;
						}

						if (ce instanceof MemberInsufficientPermissionsException) {
							handleMemberInsufficientPermissionsException((MemberInsufficientPermissionsException) ce,
							    event);
							return true;
						}

						if (ce instanceof NotDjException) {
							handleNotDjException((NotDjException) ce, event);
							return true;
						}

						if (ce instanceof NotOwnerException) {
							handleNotOwnerException(event);
							return true;
						}

						if (ce instanceof NumberOverflowException) {
							handleNumberOverflowException(event);
							return true;
						}

						if (ce instanceof TimeoutException) {
							handleTimeoutException(event);
							return true;
						}

						if (ce instanceof UnpredictedStateException) {
							handleUnpredictedStateException((UnpredictedStateException) ce, event);
							return true;
						}

						if (ce instanceof UsageException) {
							return HandleUsage.handleUsageException(command, (UsageException) ce, event);
						}

						// Reports if command execution failed due to a known reason

						ce.sendMessage(event.getChannel());
						return true;
					}

					public static void handleMemberInsufficientPermissionsException(MemberInsufficientPermissionsException ipe,
					                                                                GuildMessageReceivedEvent event) {
						// Reports if the member executing the command does not have enough permissions to
						// execute that command / part of the command

						List<String> missing = ipe.getMissingPermissions()
						    .stream()
						    .map(Permission::getName)
						    .collect(Collectors.toList());

						if (event.getChannel().canTalk()) {
							event.getChannel()
							    .sendMessage(BotUtils.buildEmbed("// ACCESS DENIED //",
							        "You need "
							            + (missing.size() == 1 ? "this permission" : "these permissions")
							            + " in order to be able to execute this command:"
							            + missing.stream().collect(Collectors.joining("\n,", "\n", ".")),
							        Constants.WARN))
							    .queue();
						}
					}

					private static void handleNotDjException(NotDjException nde, GuildMessageReceivedEvent event) {
						// Happens in case a non-DJ member tries to execute a music action

						event.getChannel()
						    .sendMessage(BotUtils.buildEmbed("// DJ-ONLY //",
						        "Looks like this guild has set up a DJ role to be "
						            + nde.getDjRole().getAsMention()
						            + " and it appears you do not have that role."
						            + "This means that you do not have permission to use music commands.",
						        Constants.WARN))
						    .queue();
					}

					private static void handleNotOwnerException(GuildMessageReceivedEvent event) {
						// Reports if command execution failed because its executor is not bot's owner

						if (event.getChannel().canTalk()) {
							event.getChannel()
							    .sendMessage(BotUtils.buildEmbed("// AUTHENTICATION REQUIRED //",
							        "This is a developer-only command and is probably not what you were looking for. "
							            + "To see the full list of commands, run `"
							            + BotUtils.getCommandPrefix(event.getGuild())
							            + "help`",
							        Constants.WARN))
							    .queue();
						}
					}

					private static void handleNumberOverflowException(GuildMessageReceivedEvent event) {
						// Reports a number overflow

						if (event.getChannel().canTalk()) {
							event.getChannel()
							    .sendMessage(BotUtils.buildEmbed("// 2 BIG 4 ME //",
							        "Looks like "
							            + BotData.getName()
							            + " couldn't understand some number because it was too big!",
							        Constants.WARN))
							    .queue();
						}
					}

					private static void handleTimeoutException(GuildMessageReceivedEvent event) {
						// Reports an EventWaiter timeout

						if (event.getChannel().canTalk()) {
							event.getChannel()
							    .sendMessage(BotUtils.buildEmbed("Response time has run out.", Constants.DISABLED))
							    .queue();
						}
					}

					private static void handleUnpredictedStateException(UnpredictedStateException use,
					                                                    GuildMessageReceivedEvent event) {
						// Reports an unpredicted state

						if (event.getChannel().canTalk())
							event.getChannel()
							    .sendMessage(BotUtils.buildEmbed("// ANOMALY DETECTED //",
							        "LiBot has run into a non-fatal unpredicted state. Command execution can not proceed.",
							        Constants.WARN))
							    .queue();
						LOG.error("Anomaly detected", use);
					}

					private static class HandleUsage {

						public static boolean handleUsageException(Command command, UsageException ue,
						                                           GuildMessageReceivedEvent event) {
							// Finds the root exception type
							if (ue instanceof MissingParametersException) {
								handleMissingParametersException(command, event);
								return true;
							}

							// Reports if command execution failed due to incorrect usage

							if (event.getChannel().canTalk()) {
								event.getChannel()
								    .sendMessage(BotUtils.buildEmbed("// USAGE INCORRECT //",
								        "Correct usage: `" + command.getUnescapedUsage(event.getGuild()) + "`.",
								        Constants.WARN))
								    .queue();
							}

							return true;
						}

						private static void handleMissingParametersException(Command command,
						                                                     GuildMessageReceivedEvent event) {
							// Reports if command execution failed due to missing parameters

							if (event.getChannel().canTalk()) {
								event.getChannel()
								    .sendMessage(BotUtils.buildEmbed("// USAGE INCORRECT //",
								        "You've provided too little parameters!\nCorrect usage: `"
								            + command.getUnescapedUsage(event.getGuild())
								            + "`.",
								        Constants.WARN))
								    .queue();
							}
						}

					}
				}

				private static void handleErrorResponseException(ErrorResponseException ere,
				                                                 GuildMessageReceivedEvent event) {
					// Reports an API error response

					if (event.getChannel().canTalk()) {
						event.getChannel()
						    .sendMessage(BotUtils.buildEmbed("// DISCORD FAILED US //",
						        "Looks like Discord didn't like that for some reason.\nError: "
						            + ere.getMeaning()
						            + ".",
						        Constants.FAILURE))
						    .queue();
					}
				}

				private static class HandleIllegalArgument {

					public static boolean handleIllegalArgumentException(IllegalArgumentException iae,
					                                                     GuildMessageReceivedEvent event) {
						// Finds the root exception type
						if (iae instanceof NumberFormatException) {
							handleNumberFormatException(event);
							return true;
						}

						return false;
					}

					private static void handleNumberFormatException(GuildMessageReceivedEvent event) {
						// Reports if command execution failed due to hierarchy problems

						if (event.getChannel().canTalk()) {
							event.getChannel()
							    .sendMessage(BotUtils.buildEmbed("// NaN (Not a Number) //",
							        "Looks like you have provided text in a place where a number would fit the best.",
							        Constants.WARN))
							    .queue();
						}
					}
				}

				private static class HandlePermission {

					public static boolean handlePermissionException(PermissionException pe,
					                                                GuildMessageReceivedEvent event) {
						// Finds the root exception type
						if (pe instanceof HierarchyException) {
							handleHierarchyException(event);
							return true;
						}

						if (pe instanceof InsufficientPermissionException) {
							handleInsufficientPermissionException((InsufficientPermissionException) pe, event);
							return true;
						}

						return false;
					}

					private static void handleInsufficientPermissionException(InsufficientPermissionException ipe,
					                                                          GuildMessageReceivedEvent event) {
						// Reports if command execution failed due to missing permissions

						if (ipe.getPermission().equals(Permission.MESSAGE_EMBED_LINKS)) {
							event.getChannel()
							    .sendMessage("**// EMBED REQUIRED //**\nYou must first grant "
							        + BotData.getName()
							        + " permission _"
							        + ipe.getPermission().getName()
							        + "_ in order to be able to execute this command!")
							    .queue();

						} else if (event.getChannel().canTalk()) {
							event.getChannel()
							    .sendMessage(BotUtils.buildEmbed("// ACCESS DENIED //",
							        "You must first grant "
							            + BotData.getName()
							            + " permission _"
							            + ipe.getPermission().getName()
							            + "_ in order to be able to execute this command!",
							        Constants.WARN))
							    .queue();
						}
					}

					private static void handleHierarchyException(GuildMessageReceivedEvent event) {
						// Reports if command execution failed due to hierarchy problems

						if (event.getChannel().canTalk()) {
							event.getChannel()
							    .sendMessage(BotUtils.buildEmbed("// HIERARCHY ERROR //",
							        "Looks like you tried to perform an audit action on a user that is in a role higher than LiBot, "
							            + "which you can't. Please move LiBot's role up or demote that user!",
							        Constants.WARN))
							    .queue();
						}
					}

				}

			}
		}

		private HandleThrowable() {}

	}

	private static boolean shouldRatelimit(Throwable t) {
		if (t instanceof CommandException) {
			CommandException ce = (CommandException) t;
			return ce.doesRegisterRatelimit();
		}

		return true;
	}

}
