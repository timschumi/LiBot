package com.mz.libot.core.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.commands.exceptions.startup.MemberInsufficientPermissionsException;
import com.mz.libot.core.commands.exceptions.startup.UnpredictedStateException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class Command {

	private static final int DEFAULT_RATELIMIT = 0;

	public final void checkPermissions(@Nullable IPermissionHolder permissionHolder) {
		if(permissionHolder == null)
			throw new UnpredictedStateException();

		Permission[] permissions = this.getPermissions();

		if (permissionHolder.hasPermission(permissions))
			return;

		throw new MemberInsufficientPermissionsException(Arrays.asList(permissions)
		    .stream()
		    .filter(p -> !permissionHolder.hasPermission(p))
		    .collect(Collectors.toList()));

	}

	public abstract void execute(@Nonnull GuildMessageReceivedEvent event, @Nonnull Parameters params) throws Throwable; // NOSONAR

	@Nonnull
	public abstract CommandCategory getCategory();

	@Nonnull
	public abstract String getInfo();

	@Nonnegative
	public int getMinParameters() {
		return getParameters().length;
	}

	@Nonnull
	public abstract String getName();

	@Nonnull
	public String[] getAliases() {
		return new String[0];
	}

	@Nonnull
	public String[] getParameters() {
		return new String[0];
	}

	@Nonnull
	public Permission[] getPermissions() {
		return new Permission[0];
	}

	public int getRatelimit() {
		return DEFAULT_RATELIMIT;
	}

	@Nonnull
	public String getRatelimitId() {
		return getName();
	}

	@Nonnull
	public final String getUnescapedUsage(@Nullable Guild guild) {
		return BotUtils.unescapeMarkdown(getUsage(guild));
	}

	@Nonnull
	public final String getUsage(@Nullable Guild guild) {
		return Commands.buildUsage(this, BotUtils.getCommandPrefixEscaped(guild));
	}

	public boolean pausesThread() {
		return false;
	}

	@SuppressWarnings("unused")
	public void startupCheck(@Nonnull GuildMessageReceivedEvent event, @Nonnull Parameters params) throws Throwable { // NOSONAR
		checkPermissions(event.getMember());
	}

	@Nonnegative
	public final int getId() {
		return getName().hashCode();
	}

	@Nullable
	public String getAdditionalData() {
		return null;
	}
}
