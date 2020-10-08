package com.mz.libot.commands.moderation;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.startup.UnpredictedStateException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;
import com.mz.utils.CTE;
import com.mz.utils.CTE.Key;
import com.mz.utils.CTE.Mode;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel;
import net.dv8tion.jda.api.entities.Guild.NotificationLevel;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class BackupGuildCommand extends Command {

	static interface Info {

		@Nonnull
		Long getId();
	}

	static class GuildInfo implements Info {

		@Nonnull
		private String name;
		@Nonnull
		private Region region;
		@Nullable
		private Long afkChannel;
		@Nullable
		private Long systemChannel;
		@Nonnull
		private Integer afkChannelTimeout;
		@Nonnull
		private Long id;

		GuildInfo(@Nonnull String name, @Nonnull Region region, @Nullable Long afkChannel, @Nullable Long systemChannel,
		          @Nonnull Integer afkChannelTimeout, @Nonnull Long id) {
			this.name = name;
			this.region = region;
			this.afkChannel = afkChannel;
			this.systemChannel = systemChannel;
			this.afkChannelTimeout = afkChannelTimeout;
			this.id = id;
		}

		@Nonnull
		public String getName() {
			return this.name;
		}

		@Nonnull
		public Region getRegion() {
			return this.region;
		}

		@Nullable
		public Long getAfkChannel() {
			return this.afkChannel;
		}

		@Nullable
		public Long getSystemChannel() {
			return this.systemChannel;
		}

		@Nonnull
		public Integer getAfkChannelTimeout() {
			return this.afkChannelTimeout;
		}

		@Override
		public Long getId() {
			return this.id;
		}
	}

	static class ModerationInfo implements Info {

		@Nonnull
		private VerificationLevel verification;
		@Nonnull
		private ExplicitContentLevel explicit;
		@Nonnull
		private NotificationLevel notifications;

		ModerationInfo(@Nonnull VerificationLevel verification, @Nonnull ExplicitContentLevel explicit,
		               @Nonnull NotificationLevel notifications) {
			this.verification = verification;
			this.explicit = explicit;
			this.notifications = notifications;
		}

		@Nonnull
		public VerificationLevel getVerification() {
			return this.verification;
		}

		@Nonnull
		public ExplicitContentLevel getExplicit() {
			return this.explicit;
		}

		@Nonnull
		public NotificationLevel getNotifications() {
			return this.notifications;
		}

		@Override
		public Long getId() {
			return -1L;
		}
	}

	static class RoleInfo implements Comparable<RoleInfo>, Info {

		@Nonnull
		private Long id;

		@Nonnull
		private String name;
		@Nonnull
		private Long permissions;
		@Nullable
		private Color color;
		@Nonnull
		private Integer position;
		@Nonnull
		private Boolean hoisted;
		@Nonnull
		private Boolean mentionable;
		@Nonnull
		private Boolean managed;

		RoleInfo(@Nonnull Long id, @Nonnull String name, @Nonnull Long permissions, @Nullable Color color,
		         @Nonnull Integer position, @Nonnull Boolean hoisted, @Nonnull Boolean mentionable,
		         @Nonnull Boolean managed) {
			this.id = id;
			this.name = name;
			this.permissions = permissions;
			this.color = color;
			this.position = position;
			this.hoisted = hoisted;
			this.mentionable = mentionable;
			this.managed = managed;
		}

		@Override
		public Long getId() {
			return this.id;
		}

		@Nonnull
		public String getName() {
			return this.name;
		}

		@Nonnull
		public Long getPermissions() {
			return this.permissions;
		}

		@Nullable
		public Color getColor() {
			return this.color;
		}

		@Nonnull
		public Integer getPosition() {
			return this.position;
		}

		@Nonnull
		public Boolean isHoisted() {
			return this.hoisted;
		}

		@Nonnull
		public Boolean isMentionable() {
			return this.mentionable;
		}

		@Nonnull
		public Boolean isManaged() {
			return this.managed;
		}

		@Override
		public int compareTo(RoleInfo o) {
			int compared = this.position.compareTo(o.position);

			if (compared == 0) {
				if (this.equals(o)) {
					return 0;
				}
				return 1;
			}

			return compared;
		}

	}

	static class MemberInfo implements Info {

		@Nonnull
		private Long id;

		private List<Long> roles;
		@Nullable
		private String nickname;
		@Nonnull
		private Boolean deafen;
		@Nonnull
		private Boolean mute;

		MemberInfo(@Nonnull Long id, @Nonnull List<Long> roles, @Nullable String nickname, @Nonnull Boolean deafen,
		           @Nonnull Boolean mute) {
			this.id = id;
			this.roles = new ArrayList<>(roles);
			this.nickname = nickname;
			this.deafen = deafen;
			this.mute = mute;
		}

		@Override
		public Long getId() {
			return this.id;
		}

		@SuppressWarnings("null")
		@Nonnull
		public List<Long> getRoles() {
			return Collections.unmodifiableList(this.roles);
		}

		@Nullable
		public String getNickname() {
			return this.nickname;
		}

		@Nonnull
		public Boolean isDeafened() {
			return this.deafen;
		}

		@Nonnull
		public Boolean isMuted() {
			return this.mute;
		}

	}

	static interface OverrideInfo extends Info {

		@Nonnull
		Long getAllowed();

		@Nonnull
		Long getDenied();

		@Nonnull
		Long getTargetId();
	}

	static class MemberPermissionOverrideInfo implements OverrideInfo {

		@Nonnull
		private Long memberId;
		@Nonnull
		private Long allowed;
		@Nonnull
		private Long denied;

		MemberPermissionOverrideInfo(@Nonnull Long memberId, @Nonnull Long allowed, @Nonnull Long denied) {
			this.memberId = memberId;
			this.allowed = allowed;
			this.denied = denied;
		}

		@Nonnull
		public Long getMemberId() {
			return this.memberId;
		}

		@Override
		public Long getAllowed() {
			return this.allowed;
		}

		@Override
		public Long getDenied() {
			return this.denied;
		}

		@Override
		public Long getId() {
			return -1L;
		}

		@Override
		public Long getTargetId() {
			return getMemberId();
		}
	}

	static class RolePermissionOverrideInfo implements OverrideInfo {

		@Nonnull
		private Long roleId;
		@Nonnull
		private Long allowed;
		@Nonnull
		private Long denied;

		RolePermissionOverrideInfo(@Nonnull Long roleId, @Nonnull Long allowed, @Nonnull Long denied) {
			this.roleId = roleId;
			this.allowed = allowed;
			this.denied = denied;
		}

		@Nonnull
		public Long getRoleId() {
			return this.roleId;
		}

		@Override
		public Long getAllowed() {
			return this.allowed;
		}

		@Override
		public Long getDenied() {
			return this.denied;
		}

		@Override
		public Long getId() {
			return -1L;
		}

		@Override
		public Long getTargetId() {
			return getRoleId();
		}
	}

	static class CategoryInfo implements Comparable<CategoryInfo>, Info {

		@Nonnull
		private Long id;

		@Nonnull
		private String name;
		@Nonnull
		private List<MemberPermissionOverrideInfo> moverrides;
		@Nonnull
		private List<RolePermissionOverrideInfo> roverrides;
		@Nonnull
		private List<Long> channels;
		@Nonnull
		private Integer position;

		CategoryInfo(@Nonnull Long id, @Nonnull String name, @Nonnull List<OverrideInfo> overrides,
		             @Nonnull List<Long> channels, @Nonnull Integer position) {
			this.id = id;
			this.name = name;
			this.channels = new ArrayList<>(channels);
			this.position = position;

			this.roverrides = new ArrayList<>();
			this.moverrides = new ArrayList<>();

			overrides.forEach(oi -> {

				if (oi instanceof RolePermissionOverrideInfo)
					this.roverrides.add((RolePermissionOverrideInfo) oi);

				else if (oi instanceof MemberPermissionOverrideInfo)
					this.moverrides.add((MemberPermissionOverrideInfo) oi);

			});
		}

		@Override
		public Long getId() {
			return this.id;
		}

		@Nonnull
		public String getName() {
			return this.name;
		}

		@Nonnull
		public List<OverrideInfo> getOverrides() {
			List<OverrideInfo> result = new ArrayList<>();
			result.addAll(this.roverrides);
			result.addAll(this.moverrides);

			return result;
		}

		@SuppressWarnings("null")
		@Nonnull
		public List<Long> getChannels() {
			return Collections.unmodifiableList(this.channels);
		}

		@Nonnull
		public Integer getPosition() {
			return this.position;
		}

		@Override
		public int compareTo(CategoryInfo o) {
			int compared = this.position.compareTo(o.position);

			if (compared == 0) {
				if (this.equals(o)) {
					return 0;
				}
				return 1;
			}

			return compared;
		}

	}

	static interface ChannelInfo extends Comparable<ChannelInfo>, Info {

		@Nonnull
		String getName();

		@Nullable
		Long getParent();

		@Nonnull
		Integer getPosition();

		@Nonnull
		List<OverrideInfo> getOverrides();

	}

	static class TextChannelInfo implements ChannelInfo {

		@Nonnull
		private Long id;

		@Nonnull
		private String name;
		@Nullable
		private Long parent;
		@Nonnull
		private Boolean nsfw;
		@Nonnull
		private List<MemberPermissionOverrideInfo> moverrides;
		@Nonnull
		private List<RolePermissionOverrideInfo> roverrides;
		@Nonnull
		private Integer position;
		@Nullable
		private String topic;

		TextChannelInfo(@Nonnull Long id, @Nonnull String name, @Nullable Long parent, @Nonnull Boolean nsfw,
		                @Nonnull List<OverrideInfo> overrides, @Nonnull Integer position, @Nullable String topic) {
			this.id = id;
			this.name = name;
			this.parent = parent;
			this.nsfw = nsfw;
			this.position = position;
			this.topic = topic;

			this.roverrides = new ArrayList<>();
			this.moverrides = new ArrayList<>();

			overrides.forEach(oi -> {

				if (oi instanceof RolePermissionOverrideInfo)
					this.roverrides.add((RolePermissionOverrideInfo) oi);

				else if (oi instanceof MemberPermissionOverrideInfo)
					this.moverrides.add((MemberPermissionOverrideInfo) oi);

			});
		}

		@SuppressWarnings("null")
		TextChannelInfo() {
			this(0L, "", null, false, Collections.emptyList(), 0, null);
		}

		@Override
		public Long getId() {
			return this.id;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Long getParent() {
			return this.parent;
		}

		@Nonnull
		public Boolean isNsfw() {
			return this.nsfw;
		}

		@Override
		public List<OverrideInfo> getOverrides() {
			List<OverrideInfo> result = new ArrayList<>();
			result.addAll(this.roverrides);
			result.addAll(this.moverrides);

			return result;
		}

		@Override
		public Integer getPosition() {
			return this.position;
		}

		@Nullable
		public String getTopic() {
			return this.topic;
		}

		@Override
		public int compareTo(ChannelInfo o) {
			int compared = this.position.compareTo(o.getPosition());

			if (compared == 0) {
				if (this.equals(o)) {
					return 0;
				}
				return 1;
			}

			return compared;
		}

	}

	static class VoiceChannelInfo implements ChannelInfo {

		@Nonnull
		private Long id;

		@Nonnull
		private String name;
		@Nullable
		private Long parent;
		@Nonnull
		private List<MemberPermissionOverrideInfo> moverrides;
		@Nonnull
		private List<RolePermissionOverrideInfo> roverrides;
		@Nonnull
		private Integer bitrate;
		@Nonnull
		private Integer userlimit;
		@Nonnull
		private Integer position;

		VoiceChannelInfo(@Nonnull Long id, @Nonnull String name, @Nullable Long categoryId,
		                 @Nonnull List<OverrideInfo> overrides, @Nonnull Integer bitrate, @Nonnull Integer userlimit,
		                 @Nonnull Integer position) {
			this.id = id;
			this.name = name;
			this.parent = categoryId;
			this.bitrate = bitrate;
			this.userlimit = userlimit;
			this.position = position;

			this.roverrides = new ArrayList<>();
			this.moverrides = new ArrayList<>();

			overrides.forEach(oi -> {

				if (oi instanceof RolePermissionOverrideInfo)
					this.roverrides.add((RolePermissionOverrideInfo) oi);

				else if (oi instanceof MemberPermissionOverrideInfo)
					this.moverrides.add((MemberPermissionOverrideInfo) oi);

			});
		}

		@SuppressWarnings("null")
		VoiceChannelInfo() {
			this(0L, "", null, Collections.emptyList(), 0, 0, 0);
		}

		@Override
		public Long getId() {
			return this.id;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Long getParent() {
			return this.parent;
		}

		@Override
		public List<OverrideInfo> getOverrides() {
			List<OverrideInfo> result = new ArrayList<>();
			result.addAll(this.roverrides);
			result.addAll(this.moverrides);

			return result;
		}

		@Nonnull
		public Integer getBitrate() {
			return this.bitrate;
		}

		@Nonnull
		public Integer getLimit() {
			return this.userlimit;
		}

		@Override
		public Integer getPosition() {
			return this.position;
		}

		@Override
		public int compareTo(ChannelInfo o) {
			int compared = this.position.compareTo(o.getPosition());

			if (compared == 0) {
				if (this.equals(o)) {
					return 0;
				}
				return 1;
			}

			return compared;
		}

	}

	@Nonnull
	private static List<OverrideInfo> getOverrides(@Nonnull GuildChannel channel) {
		List<OverrideInfo> overrides = new ArrayList<>();
		channel.getMemberPermissionOverrides().forEach(override -> {
			Member member = override.getMember();
			if (member == null)
				return; // Shouldn't occur
			overrides.add(new MemberPermissionOverrideInfo(member.getUser().getIdLong(),
			    Permission.getRaw(override.getAllowed()), Permission.getRaw(override.getDenied())));
		});

		channel.getRolePermissionOverrides().forEach(override -> {
			Role role = override.getRole();
			if (role == null)
				return; // Shouldn't occur
			overrides.add(new RolePermissionOverrideInfo(role.getIdLong(), Permission.getRaw(override.getAllowed()),
			    Permission.getRaw(override.getDenied())));
		});

		return overrides;
	}

	public static void backup(Guild guild, User author, MessageChannel channel) {
		EventWaiter ew = new EventWaiter(author, channel);
		if (!guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)
		    && !ew.getBoolean(BotUtils.buildEmbed("Warning!",
		        BotData.getName()
		            + " does currently not have the 'Administrator' permission on this guild "
		            + "which will result in it ignoring channels not visible to it (if there are any). "
		            + "Please ensure that the data about your guild provided below is correct"
		            + " and **CONTINUE ONLY IF IT IS**:\nNumber of text channels: **"
		            + guild.getTextChannels().size()
		            + "**,\nNumber of voice channels: **"
		            + guild.getVoiceChannels().size()
		            + "**.\nIf any of the numbers is lower than the actual, "
		            + "please grant "
		            + BotData.getName()
		            + " the 'Administrator' permission or allow "
		            + BotData.getName()
		            + " the 'Read Messages' permission in all channels.",
		        Constants.WARN))) {
			return;
		}

		String password = null;
		if (ew.getBoolean(BotUtils.buildEmbed("Encryption", "Do you want to encrypt this backup with a password?",
		    Constants.LITHIUM))) {

			channel.sendMessage(BotUtils.buildEmbed(null, "Type in a new password or EXIT", Constants.LITHIUM)).queue();
			Message passMessage = ew.getMessage();
			String newPass = passMessage.getContentDisplay();
			passMessage.delete().queue();

			if ("exit".equalsIgnoreCase(newPass))
				return;

			channel.sendMessage(BotUtils.buildEmbed(null, "Confirm password (repeat the old one)", Constants.LITHIUM))
			    .queue();
			Message passConfirm = ew.getMessage();
			String confirmPass = passConfirm.getContentDisplay();
			passConfirm.delete().queue();

			if (!confirmPass.equals(newPass)) {
				throw new CommandException("Failure", "Passwords didn't match", Constants.FAILURE, false);
			}

			password = newPass;
		}

		Map<String, Object> data = new HashMap<>();
		data.put("author", author.getIdLong());
		// Stores backup's author
		backupGuildInfo(guild, data);
		backupModeration(guild, data);
		backupRoles(guild, data);
		backupMembers(guild, data);
		backupCategories(guild, data);
		backupChannels(guild, data);
		data.put("createdon", System.currentTimeMillis());
		// Stores command's timestamp
		Gson gson = new GsonBuilder().serializeNulls()
		    .setLongSerializationPolicy(LongSerializationPolicy.STRING)
		    .create();
		String json = gson.toJson(data);

		if (password != null) {
			CTE cte = new CTE(new Key(password.getBytes(StandardCharsets.UTF_8)));
			json = cte.process(json, Mode.ENCRYPT);
		}

		byte[] backup = Base64.getEncoder().encode(json.getBytes(StandardCharsets.UTF_8));
		author.openPrivateChannel().queue(dm -> {
			MessageBuilder builder = new MessageBuilder();
			builder.setEmbed(BotUtils.buildEmbed("Backup of " + guild.getName() + " complete",
			    "Here's a few things you must know:\n"
			        + "-to restore this backup, run the 'restore' command in the preferred guild,"
			        + "\n-if you restore a backup on a guild other than the one it was created on, "
			        + "all text channels will need to be deleted and recreated in order to apply their settings "
			        + "(of course, you will be prompted first),"
			        + "\n-there's no way you can restore an encrypted backup if you forget the password.",
			    Constants.SUCCESS));
			dm.sendMessage(builder.build())
			    .addFile(backup, guild.getName() + ".bkp")
			    .queue(
			        m -> channel
			            .sendMessage(BotUtils.buildEmbed("Success",
			                "Backup was sent to you via direct messaging successfully!", Constants.SUCCESS))
			            .queue(),
			        t -> channel.sendMessage(BotUtils.buildEmbed("Error",
			            "Could not deliver you the backup file _(did you block " + BotData.getName() + "?)_",
			            Constants.FAILURE)).queue());

		});
	}

	private static void backupChannels(Guild guild, Map<String, Object> data) {
		List<TextChannelInfo> tchannels = new ArrayList<>();
		List<VoiceChannelInfo> vchannels = new ArrayList<>();

		guild.getTextChannels().forEach(c -> {
			Category parent = c.getParent();
			tchannels.add(new TextChannelInfo(c.getIdLong(), c.getName(), parent == null ? null : parent.getIdLong(),
			    c.isNSFW(), getOverrides(c), c.getPosition(), c.getTopic()));
		}

		);

		guild.getVoiceChannels().forEach(c -> {
			Category parent = c.getParent();
			vchannels.add(new VoiceChannelInfo(c.getIdLong(), c.getName(), parent == null ? null : parent.getIdLong(),
			    getOverrides(c), c.getBitrate(), c.getUserLimit(), c.getPosition()));
		}

		);

		data.put("tchannels", tchannels);
		data.put("vchannels", vchannels);
	}

	private static void backupCategories(Guild guild, Map<String, Object> data) {
		List<CategoryInfo> categories = new ArrayList<>();
		guild.getCategories().forEach(category -> {

			List<Long> channels = new ArrayList<>();
			category.getChannels().forEach(c -> channels.add(c.getIdLong()));

			categories.add(new CategoryInfo(category.getIdLong(), category.getName(), getOverrides(category), channels,
			    category.getPosition()));
		});
		data.put("categories", categories);
	}

	private static void backupModeration(Guild guild, Map<String, Object> data) {
		data.put("moderation", new ModerationInfo(guild.getVerificationLevel(), guild.getExplicitContentLevel(),
		    guild.getDefaultNotificationLevel()));
	}

	private static void backupGuildInfo(Guild guild, Map<String, Object> data) {
		VoiceChannel afkChannel = guild.getAfkChannel();
		TextChannel systemChannel = guild.getSystemChannel();
		data.put("server",
		    new GuildInfo(guild.getName(), guild.getRegion(), afkChannel == null ? null : afkChannel.getIdLong(),
		        systemChannel == null ? null : systemChannel.getIdLong(), guild.getAfkTimeout().getSeconds(),
		        guild.getIdLong()));
	}

	@SuppressWarnings("null")
	private static void backupMembers(Guild guild, Map<String, Object> data) {
		List<MemberInfo> members = new ArrayList<>();
		guild.getMembers().forEach(member -> {
			GuildVoiceState voiceState = member.getVoiceState();
			if(voiceState == null)
				throw new UnpredictedStateException();

			members.add(new MemberInfo(member.getUser().getIdLong(),
			    member.getRoles()
			        .stream()
			        .filter(r -> !r.isManaged())
			        .map(Role::getIdLong)
			        .collect(Collectors
			            .toList()),
			    member.getNickname(), voiceState.isGuildDeafened(), voiceState.isDeafened()));
		});
		data.put("members", members);
	}

	private static void backupRoles(Guild guild, Map<String, Object> data) {
		List<RoleInfo> roles = new ArrayList<>();

		guild.getRoles()
		    .forEach(r -> roles.add(new RoleInfo(r.getIdLong(), r.getName(), Permission.getRaw(r.getPermissions()),
		        r.getColor(), r.getPosition(), r.isHoisted(), r.isMentionable(), r.isManaged())));

		data.put("roles", roles);
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();
		Guild guild = event.getGuild();

		backup(guild, author, channel);
	}

	@Override
	public String getInfo() {
		return "Creates a backup file of your guild that can later be used to restore its configuration.";
	}

	@Override
	public String getName() {
		return "BackupGuild";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public int getRatelimit() {
		return 30;
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
		return Commands.toArray("backup", "backupserver");
	}

}
