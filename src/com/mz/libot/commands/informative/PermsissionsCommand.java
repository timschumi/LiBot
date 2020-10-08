package com.mz.libot.commands.informative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.mz.libot.core.Constants;
import com.mz.libot.core.FinderUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.startup.UnpredictedStateException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PermsissionsCommand extends Command {

	@Nonnull
	private static Field forPermissions(@Nonnull EnumSet<Permission> perms, @Nonnull String roleName,
	                                    @Nonnull EnumSet<Permission> listed) {
		StringBuilder sb = new StringBuilder();
		List<Permission> permissions = new ArrayList<>();
		permissions.addAll(perms);
		permissions.removeAll(listed);

		if (permissions.isEmpty()) {
			sb.append("_All already inherited_");

		} else {
			sb.append(permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")));
			listed.addAll(permissions);
		}

		return new Field(roleName, sb.toString(), false);
	}

	private static Field forRole(@Nonnull Role role, @Nonnull EnumSet<Permission> listed) {
		return forPermissions(role.getPermissions(), role.getName(), listed);
	}


	@SuppressWarnings("null")
	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		Member target = null;
		if (params.check()) {
			List<Member> members = FinderUtils.findMembers(params.get(0), event.getGuild());

			if (!members.isEmpty())
				target = members.get(0);
		}

		if (target == null)
			target = event.getMember();

		if (target == null)
			throw new UnpredictedStateException();

		List<Field> fields = new ArrayList<>();
		EnumSet<Permission> listed = EnumSet.noneOf(Permission.class);

		fields.add(forRole(event.getGuild().getPublicRole(), listed));
		// Adds @everyone and its permissions

		List<Role> roles = new ArrayList<>();
		roles.addAll(target.getRoles());
		Collections.reverse(roles);

		roles.forEach(r -> fields.add(forRole(r, listed)));
		// Adds all roles & their permissions

		Collections.reverse(fields);
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Constants.LITHIUM)
		    .setTitle(target.getEffectiveName() + "'s permissions in " + event.getGuild().getName());

		fields.forEach(builder::addField);

		event.getChannel().sendMessage(builder.build()).queue();
	}

	@Override
	public String getInfo() {
		return "Lists someone's permissions and roles they inherited them from. "
		    + "This command will NOT list any channel-specific permission overrides."
		    + "If no member is mentioned, your permissions will be listed.";
	}

	@Override
	public String getName() {
		return "Permissions";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.INFORMATIVE;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("@member (optional)");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("perms");
	}
}
