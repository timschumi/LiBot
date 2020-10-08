package com.mz.libot.commands.moderation;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mz.libot.commands.moderation.BackupGuildCommand.CategoryInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.ChannelInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.GuildInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.MemberInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.MemberPermissionOverrideInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.ModerationInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.OverrideInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.RoleInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.RolePermissionOverrideInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.TextChannelInfo;
import com.mz.libot.commands.moderation.BackupGuildCommand.VoiceChannelInfo;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;
import com.mz.utils.CTE;
import com.mz.utils.CTE.Key;
import com.mz.utils.CTE.Mode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;

public class RestoreGuildCommand extends Command {

	private enum FailureReason {

		MISSING_PERMISSIONS("Insufficient permissions / hierarchy height"),
		ILLEGAL_JSON("Could not parse this section of the backup file"),
		UNKNOWN("Unknown failure");

		public final String displayable;

		FailureReason(String displayable) {
			this.displayable = displayable;
		}

	}

	private enum Phase {

		MODERATION_INFO("Restoring moderation info"),
		ROLES("Restoring roles"),
		MEMBER_DATA("Restoring member data"),
		CATEGORIES("Restoring categories"),
		CHANNELS("Restoring voice & text channels"),
		GUILD_DATA("Restoring guild data"),
		DONE(null);

		public final String readable;

		Phase(String readable) {
			this.readable = readable;
		}

	}

	@SuppressWarnings("null")
	@Nonnull
	private static MessageEmbed getStatus(@Nonnull Map<Phase, FailureReason> failures, @Nonnull Phase currentPhase,
	                                      @Nonnull String guildName) {
		StringBuilder sb = new StringBuilder();

		Color color = Constants.LITHIUM;
		if (!failures.isEmpty()) {
			color = Constants.FAILURE;

		} else if (currentPhase == Phase.DONE) {
			color = Constants.SUCCESS;
		}

		for (Phase phase : Phase.values()) {
			if (phase.readable != null) {
				if (failures.containsKey(phase)) {
					sb.append(phase.readable
					    + " - FAILED _("
					    + failures.get(phase).displayable
					    + ")_ "
					    + Constants.DENY_EMOJI
					    + "\n");

				} else if (currentPhase == phase) {
					sb.append(phase.readable + " - WORKING <a:loading:403289047054942209>\n");

				} else if (phase.ordinal() > currentPhase.ordinal()) {
					sb.append(phase.readable + " - WAITING\n");

				} else if (phase.ordinal() < currentPhase.ordinal()) {
					sb.append(phase.readable + " - COMPLETED " + Constants.ACCEPT_EMOJI + "\n");
				}
			}
		}

		return BotUtils.buildEmbed((currentPhase == Phase.DONE ? "Completed restoring " : "Restoring ") + guildName,
		    sb.toString(), color);

	}

	@Nullable
	private static Message startStatus(@Nonnull PrivateChannel dm, @Nonnull Map<Phase, FailureReason> failures,
	                                   @Nonnull Phase currentPhase, @Nonnull String guildName) {
		MessageEmbed embed = getStatus(failures, currentPhase, guildName);

		try {
			return dm.sendMessage(embed).complete();
		} catch (Exception e) {
			return null;
		}
	}

	private static void updateStatus(@Nullable Message message, @Nonnull Map<Phase, FailureReason> failures,
	                                 @Nonnull Phase currentPhase, @Nonnull String guildName) {
		if (message == null)
			return;

		message.editMessage(getStatus(failures, currentPhase, guildName)).queue();
	}

	public static JsonElement extractFromJson(Map<String, Object> data, String key) {
		Object yourMap = data.get(key);

		return new Gson().toJsonTree(yourMap);
	}

	@SuppressWarnings("null")
	private static void setOverrides(@Nonnull List<OverrideInfo> overrides, @Nonnull GuildChannel channel,
	                                 @Nonnull Map<Long, Role> roleMappings, @Nonnull Map<Long, Member> memberMappings) {
		channel.getPermissionOverrides().forEach(po -> po.delete().complete());

		for (OverrideInfo oi : overrides) {
			if (memberMappings.get(oi.getTargetId()) != null || roleMappings.get(oi.getTargetId()) != null) {
				PermissionOverride po = null;
				if (oi instanceof MemberPermissionOverrideInfo) {
					po = channel.getPermissionOverride(memberMappings.get(oi.getTargetId()));

					if (po == null) {
						po = channel.createPermissionOverride(memberMappings.get(oi.getTargetId())).complete();
					}

				} else if (oi instanceof RolePermissionOverrideInfo) {
					po = channel.getPermissionOverride(roleMappings.get(oi.getTargetId()));

					if (po == null) {
						po = channel.createPermissionOverride(roleMappings.get(oi.getTargetId())).complete();
					}

				} else {
					continue;
					// Shouldn't happen as of now, indicates that an unknown permission override type has
					// been encountered
				}

				if (oi.getAllowed().equals(po.getAllowedRaw()) && oi.getDenied().equals(po.getDeniedRaw())) {
					continue;
				}

				po.getManager().deny(oi.getDenied()).grant(oi.getAllowed()).complete();
			}
		}
	}

	@SuppressWarnings("null")
	private static void reorderCategories(@Nonnull Guild guild, @Nonnull Map<Category, Integer> positionMappings) {
		ChannelOrderAction coa = guild.modifyCategoryPositions();
		for (Entry<Category, Integer> entry : positionMappings.entrySet()) {
			coa.selectPosition(entry.getKey());
			coa.moveTo(entry.getValue());
		}
		coa.complete();

	}

	@SuppressWarnings("null")
	private static void reorderChannels(@Nonnull Guild guild, @Nonnull Map<GuildChannel, Integer> positionMappings) {
		ChannelOrderAction tcoa = guild.modifyTextChannelPositions();
		ChannelOrderAction vcoa = guild.modifyVoiceChannelPositions();

		for (Entry<GuildChannel, Integer> entry : positionMappings.entrySet()) {
			if (entry.getKey() instanceof TextChannel) {
				tcoa.selectPosition(entry.getKey());
				tcoa.moveTo(entry.getValue());

			} else if (entry.getKey() instanceof VoiceChannel) {
				vcoa.selectPosition(entry.getKey());
				vcoa.moveTo(entry.getValue());
			}

		}

		tcoa.complete();
		vcoa.complete();
	}

	private static void handleThrowable(Throwable t, Phase phase, Map<Phase, FailureReason> failures) {
		if (t instanceof JsonParseException) {
			failures.put(phase, FailureReason.ILLEGAL_JSON);

		} else if (t instanceof PermissionException) {
			failures.put(phase, FailureReason.MISSING_PERMISSIONS);

		} else {
			failures.put(phase, FailureReason.UNKNOWN);
		}
	}

	@SuppressWarnings("null")
	@SuppressFBWarnings({ "ES_COMPARING_STRINGS_WITH_EQ", "RV_RETURN_VALUE_IGNORED" })
	public static void restore(Guild guild, User author, MessageChannel channel) throws IOException {
		if (!guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
			throw new CommandException("Not an admin",
			    "Looks like "
			        + BotData.getName()
			        + " does currently not have the 'Administrator' permission for this guild. "
			        + "Administrator permission is required for the restoration process, so please grant it to "
			        + BotData.getName()
			        + " before running the guild restoration process!",
			    Constants.WARN, false);
		}

		if (!guild.getMembersWithRoles(guild.getRoles().get(0)).contains(guild.getSelfMember())) {
			throw new CommandException("Not in the highest role",
			    "Looks like "
			        + BotData.getName()
			        + " is currently not in the highest role for this guild ("
			        + guild.getRoles().get(0).getName()
			        + ". "
			        + BotData.getName()
			        + " needs to be in the highest role in order to be able to restore all the roles. "
			        + "Please put "
			        + BotData.getName()
			        + " in the highest role before running the guild restoration process!",
			    Constants.WARN, false);
		}

		EventWaiter ew = new EventWaiter(author, channel);

		String json = null;
		boolean encrypted = false;
		{
			Message msg = channel
			    .sendMessage(
			        BotUtils.buildEmbed("Restore", "Please upload your backup file or type `EXIT`", Constants.LITHIUM))
			    .complete();
			while (json == null) {

				Message input = ew.getMessage();

				if ("exit".equalsIgnoreCase(input.getContentDisplay()))
					throw new CanceledException();

				if (input.getAttachments().isEmpty()) {
					channel.sendMessage("Please upload a _file_!").queue();
					continue;
				}

				Attachment attachment = input.getAttachments().get(0);

				StringWriter writer = new StringWriter();
				try (InputStream openStream = new URL(attachment.getProxyUrl()).openStream()) {
					IOUtils.copy(openStream, writer, StandardCharsets.UTF_8);
				}
				String backupString = new String(
				    Base64.decodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

				if (ew.getBoolean(BotUtils.buildEmbed("Encryption",
				    "Is this backup file encrypted? More specifically, "
				        + "did you choose to protect this backup file with a password when creating it?",
				    Constants.LITHIUM))) {
					encrypted = true;

					channel
					    .sendMessage(
					        BotUtils.buildEmbed(null, "Please type in the password or `EXIT`", Constants.LITHIUM))
					    .queue();
					Message passwordMessage = ew.getMessage();
					passwordMessage.getContentDisplay();

					CTE cte = new CTE(new Key(passwordMessage.getContentDisplay().getBytes(StandardCharsets.UTF_8)));

					passwordMessage.delete().queue();

					json = cte.process(backupString, Mode.DECRYPT);

				} else {
					json = backupString;
				}
			}

			msg.delete().queue(m -> {}, t -> {});
		}

		Type map = new TypeToken<Map<String, Object>>() {}.getType();

		Gson gson = new Gson();
		Map<String, Object> data = null;
		try {
			data = gson.fromJson(json, map);
		} catch (JsonParseException e) {
			throw new CommandException("Pardon?",
			    "Looks like this is not indeed a valid backup file. This could also mean that "
			        + (encrypted ? "you've entered the wrong password" : "the backup file is indeed encrypted")
			        + ".",
			    Constants.FAILURE, false);
		}

		boolean onOldGuild = false;
		try {
			onOldGuild = gson.fromJson(extractFromJson(data, "server"), GuildInfo.class)
			    .getId()
			    .equals(guild.getIdLong());
		} catch (JsonParseException e) {
			onOldGuild = false;
		}

		if (!ew.getBoolean(BotUtils.buildEmbed("Are you sure?",
		    "Are you sure you wan't to do this? "
		        + "Even though you will be prompted before deletion of any text channels, "
		        + "consider creating a backup of the current guild, just for good measure.\nYou currently **"
		        + (onOldGuild ? "ARE" : "AREN'T")
		        + "** restoring this backup on the original guild "
		        + "_(the one it was created on)_.",
		    Constants.SUCCESS))) {
			throw new CanceledException();
		}
		// Starts restoration process

		Map<Phase, FailureReason> failures = new EnumMap<>(Phase.class);
		Message status = null;
		// Status trackers

		GuildManager gm = guild.getManager();
		// Guild moderation controllers

		status = startStatus(author.openPrivateChannel().complete(), failures, Phase.MODERATION_INFO, guild.getName());
		try {
			ModerationInfo moderation = gson.fromJson(extractFromJson(data, "moderation"), ModerationInfo.class);

			gm.setExplicitContentLevel(moderation.getExplicit())
			    .setVerificationLevel(moderation.getVerification())
			    .setDefaultNotificationLevel(moderation.getNotifications())
			    .complete();

		} catch (Throwable t) {
			handleThrowable(t, Phase.MODERATION_INFO, failures);
		}
		/*
		 * Restores moderation info
		 */

		updateStatus(status, failures, Phase.ROLES, guild.getName());
		Map<Long, Role> roleMappings = new HashMap<>();
		Map<Long, Member> memberMappings = new HashMap<>();
		Map<Long, Category> categoryMappings = new HashMap<>();
		Map<Long, GuildChannel> channelMappings = new HashMap<>();
		try {
			Type type = new TypeToken<List<RoleInfo>>() {}.getType();

			List<RoleInfo> roles = gson.fromJson(extractFromJson(data, "roles"), type);
			// Gets all backuped roles

			guild.getRoles().forEach(role -> {
				if (!role.isManaged() && !role.isPublicRole() && guild.getSelfMember().canInteract(role))
					role.delete().complete();
			});
			// Deletes all roles

			Collections.sort(roles, (info1, info2) -> info1.compareTo(info2));
			Collections.reverse(roles);
			// Creates a list of roles

			roles.forEach(roleInfo -> {
				Role role = null;
				if (roleInfo.getPosition() == -1) {
					guild.getPublicRole().getManager().setPermissions(roleInfo.getPermissions()).complete();
					role = guild.getPublicRole();

				} else {
					Role target = guild.getRoleById(roleInfo.getId());
					if (roleInfo.isManaged() && target != null) {
						if (guild.getSelfMember().canInteract(target)) {
							target.getManager()
							    .setColor(roleInfo.getColor())
							    .setName(roleInfo.getName())
							    .setHoisted(roleInfo.isHoisted())
							    .setMentionable(roleInfo.isMentionable())
							    .setPermissions(roleInfo.getPermissions())
							    .complete();
						}

						role = target;
						// Updates the role

					} else {
						role = guild.createRole()
						    .setColor(roleInfo.getColor())
						    .setName(roleInfo.getName())
						    .setHoisted(roleInfo.isHoisted())
						    .setMentionable(roleInfo.isMentionable())
						    .setPermissions(roleInfo.getPermissions())
						    .complete();
					}
				}

				roleMappings.put(roleInfo.getId(), role);
			});

		} catch (Throwable t) {
			handleThrowable(t, Phase.ROLES, failures);
		}
		/*
		 * Restores roles
		 */

		updateStatus(status, failures, Phase.MEMBER_DATA, guild.getName());
		try {
			Type type = new TypeToken<List<MemberInfo>>() {}.getType();
			List<MemberInfo> members = gson.fromJson(extractFromJson(data, "members"), type);

			for (MemberInfo memberInfo : members) {
				Member member = guild.getMemberById(memberInfo.getId());

				if (member != null) {
					if (guild.getSelfMember().canInteract(member)) {
						guild
						    .modifyMemberRoles(member,
						        memberInfo.getRoles()
						            .stream()
						            .map(roleMappings::get)
						            .filter(r -> guild.getSelfMember().canInteract(r))
						            .collect(Collectors.toList()))
						    .complete();
						member.modifyNickname(memberInfo.getNickname()).complete();
						member.deafen(memberInfo.isDeafened()).complete();
						member.mute(memberInfo.isDeafened()).complete();
					}

					memberMappings.put(memberInfo.getId(), member);
				}
			}

		} catch (Throwable t) {
			handleThrowable(t, Phase.MEMBER_DATA, failures);
		}
		/*
		 * Restores member data
		 */

		updateStatus(status, failures, Phase.CATEGORIES, guild.getName());
		try {
			Type type = new TypeToken<List<CategoryInfo>>() {}.getType();
			List<CategoryInfo> categories = gson.fromJson(extractFromJson(data, "categories"), type);
			Map<Category, Integer> positionMappings = new HashMap<>();

			guild.getCategories().forEach(c -> {

				c.getChannels().forEach(ch -> ch.getManager().setParent(null).complete());
				c.delete().complete();

			});
			// Uncategorizes all channels & deletes all categories

			for (

			CategoryInfo info : categories) {
				Category category = guild.getCategoryById(info.getId());
				if (category == null)
					category = guild.createCategory(info.getName()).complete();

				setOverrides(info.getOverrides(), category, roleMappings, memberMappings);
				// Configures overrides

				category.getManager().setName(info.getName()).complete();
				// Sets category's name

				positionMappings.put(category, info.getPosition());
				categoryMappings.put(info.getId(), category);
				// Maps category
			}

			reorderCategories(guild, positionMappings);
			// Reorders categories

		} catch (Throwable t) {
			handleThrowable(t, Phase.CATEGORIES, failures);
		}
		/*
		 * Restores categories
		 */

		updateStatus(status, failures, Phase.CHANNELS, guild.getName());
		List<TextChannel> toDelete = new ArrayList<>();
		try {
			Type ttype = new TypeToken<List<TextChannelInfo>>() {}.getType();
			Type vtype = new TypeToken<List<VoiceChannelInfo>>() {}.getType();

			List<ChannelInfo> channels = new ArrayList<>();
			channels.addAll(gson.fromJson(extractFromJson(data, "tchannels"), ttype));
			channels.addAll(gson.fromJson(extractFromJson(data, "vchannels"), vtype));

			Map<GuildChannel, Integer> positionMappings = new HashMap<>();

			List<Long> channelIds = channels.stream().map(ChannelInfo::getId).collect(Collectors.toList());

			guild.getTextChannels().forEach(c -> {
				if (!channelIds.contains(c.getIdLong()))
					toDelete.add(c);
			});

			guild.getVoiceChannels().forEach(c -> c.delete().complete());

			if (!toDelete.isEmpty()) {
				EventWaiter dmEw = new EventWaiter(author, author.openPrivateChannel().complete());
				StringBuilder sb = new StringBuilder(
				    "**The following text channels exist but are not defined in the backup file:**");

				toDelete.forEach(c -> sb.append("\n#" + c.getName()));

				if (dmEw.getBoolean(BotUtils.buildEmbed("Are you sure", sb.toString()
				    + "_\nDo you want to delete them _(WARNING: you'll lose all the messages in them if you accept)_. "
				    + "Note that if you aren't restoring this backup on the guild it originated from, "
				    + "choosing 'X' here may create a lot of duplicate text channels!", Constants.WARN)))
					toDelete.forEach(tc -> tc.delete().complete());
			}

			guild.getVoiceChannels().forEach(VoiceChannel::delete);

			for (ChannelInfo channelInfo : channels) {
				ChannelType type = channelInfo instanceof TextChannelInfo ? ChannelType.TEXT : ChannelType.VOICE;

				GuildChannel targetChannel = null;
				if (type == ChannelType.TEXT) {
					targetChannel = guild.getTextChannelById(channelInfo.getId());

					if (targetChannel == null) {
						targetChannel = guild.createTextChannel(channelInfo.getName()).complete();
					}

				} else if (type == ChannelType.VOICE) {
					targetChannel = guild.getVoiceChannelById(channelInfo.getId());

					if (targetChannel == null) {
						targetChannel = guild.createVoiceChannel(channelInfo.getName()).complete();
					}

				} else {
					continue;
					// Shouldn't happen as of now, indicates that an unknown channel type has
					// been encountered
				}
				// Creates/gets a channel based on channelInfo type

				ChannelManager cm = targetChannel.getManager();
				cm.setParent(categoryMappings.get(channelInfo.getParent()));
				cm.setName(channelInfo.getName());

				if (type == ChannelType.TEXT) {
					TextChannelInfo info = (TextChannelInfo) channelInfo;
					cm.setNSFW(info.isNsfw());
					cm.setTopic(info.getTopic());
				}
				// Configures a text channel

				if (type == ChannelType.VOICE) {
					VoiceChannelInfo info = (VoiceChannelInfo) channelInfo;
					cm.setBitrate(info.getBitrate());
					cm.setUserLimit(info.getLimit());
				}
				// Configures a voice channel

				cm.complete();
				// Updates the channel manager

				setOverrides(channelInfo.getOverrides(), targetChannel, roleMappings, memberMappings);
				// Configures overrides

				positionMappings.put(targetChannel, channelInfo.getPosition());
				channelMappings.put(channelInfo.getId(), targetChannel);
				// Maps channel

			}

			reorderChannels(guild, positionMappings);
		} catch (Throwable t) {
			handleThrowable(t, Phase.CHANNELS, failures);
		}
		/*
		 * Restores voice & text channels
		 */

		updateStatus(status, failures, Phase.GUILD_DATA, guild.getName());
		try {
			GuildInfo guildInfo = gson.fromJson(extractFromJson(data, "server"), GuildInfo.class);
			gm.setAfkChannel((VoiceChannel) channelMappings.get(guildInfo.getAfkChannel()))
			    .setAfkTimeout(Guild.Timeout.fromKey(guildInfo.getAfkChannelTimeout()))
			    .setName(guildInfo.getName())
			    .setSystemChannel((TextChannel) channelMappings.get(guildInfo.getSystemChannel()))
			    .setRegion(guildInfo.getRegion())
			    .complete();

		} catch (Throwable t) {
			handleThrowable(t, Phase.GUILD_DATA, failures);
		}
		// Restores guild configuration

		updateStatus(status, failures, Phase.DONE, guild.getName());

		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(failures.size() == 0 ? Constants.SUCCESS : Constants.FAILURE);
		builder.setTitle("Restoration complete" + (failures.size() == 0 ? "" : "d with failures"));

		StringBuilder failedPhases = new StringBuilder();
		for (Phase phase : failures.keySet()) {
			failedPhases.append("\n" + phase.readable);
		}

		builder.setDescription(guild.getName()
		    + " has been successfully restored from the backup file"
		    + (failures.size() == 0 ? "!"
		        : " with "
		            + failures.size()
		            + " errors! Errors occurred when "
		            + BotData.getName()
		            + " was"
		            + failedPhases.toString()
		            + "."));

		author.openPrivateChannel().complete().sendMessage(builder.build()).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		TextChannel txtChannel = event.getChannel();
		User author = event.getAuthor();
		Guild guild = event.getGuild();

		restore(guild, author, txtChannel);
	}

	@Override
	public String getInfo() {
		return "Restores a guild from a backup file. All instructions are included in the command.";
	}

	@Override
	public String getName() {
		return "RestoreGuild";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public boolean pausesThread() {
		return true;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.ADMINISTRATOR);
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("restore", "restoreserver");
	}

}
