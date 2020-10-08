package com.mz.libot.commands.moderation;

import java.util.List;

import javax.annotation.Nonnull;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.FinderUtils;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.exceptions.runtime.CanceledException;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.EventWaiter;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;

final class Utils {

	public static class RolePack {

		@Nonnull
		private Member target;
		@Nonnull
		private Role role;

		public RolePack(@Nonnull Role role, @Nonnull Member target) {
			this.role = role;
			this.target = target;
		}

		@Nonnull
		public Member getTarget() {
			return this.target;
		}

		@Nonnull
		public Role getRole() {
			return this.role;
		}

	}

	private Utils() {}

	/**
	 * Checks if the issuer has all the required permissions to change target's roles
	 *
	 * @param msg
	 *            command as a string
	 * @param channel
	 *            channel of execution
	 * @param usage
	 *            command's usage to be displayed if parameters are incorrect
	 * @param message
	 *            command message
	 * @param guild
	 *            guild of execution
	 * @param author
	 *            command issuer
	 *
	 * @return Role and Member to interact with, null something failed or command isn't
	 *         ready to be executed yet
	 */
	@Nonnull
	static RolePack getRolePack(@Nonnull Parameters params, @Nonnull GuildMessageReceivedEvent event) {

		Member member = findMember(params, event);

		if (!event.getGuild().retrieveMember(event.getAuthor(), false).complete().canInteract(member))
			throw new CommandException("You can't interact with members in higher/equal roles than you!",
			    Constants.FAILURE, false);
		// Checks permissions

		Role role = findRole(params, event);
		return new RolePack(role, member);
	}

	@SuppressWarnings("null")
	@Nonnull
	private static Member findMember(@Nonnull Parameters params, @Nonnull GuildMessageReceivedEvent event) {
		String memberName = params.get(0);
		List<Member> members = FinderUtils.findMembers(memberName, event.getGuild());

		if (members.isEmpty())
			throw new CommandException("Member \"" + memberName + "\" does not exist!", Constants.FAILURE, false);

		return members.get(0);
	}

	@SuppressWarnings("null")
	@Nonnull
	private static Role findRole(@Nonnull Parameters params, @Nonnull GuildMessageReceivedEvent event) {
		String roleName = params.get(1);
		List<Role> roles = FinderUtils.findRoles(roleName, event.getGuild());

		if (roles.isEmpty())
			throw new CommandException("Role \"" + roleName + "\" does not exist!", Constants.FAILURE, false);

		return roles.get(0);
	}

	public enum ModAction {
		KICK,
		BAN;
	}

	static void getModTarget(@Nonnull ModAction action, @Nonnull MessageChannel channel, @Nonnull Guild guild,
	                         @Nonnull Member member, @Nonnull Parameters params) {

		List<Member> members = FinderUtils.findMembers(params.get(0), guild);

		if (members.isEmpty())
			throw new CommandException("Member \"" + params.get(0) + "\" has not been found!", Constants.FAILURE,
			    false);

		Member target = members.get(0);

		final String reason;
		if (params.check(2)) {
			reason = member.getUser().getName() + ": " + params.get(1);

		} else {
			reason = member.getUser().getName() + ": No reason specified.";
		}
		// Gets reason from parameters

		if (target.getUser().getIdLong() == BotData.getJDA().getSelfUser().getIdLong())
			throw new CommandException("I can not " + action.toString().toLowerCase() + " myself!", false);

		if (!member.canInteract(target))
			throw new CommandException("Target user is higher in hierarchy than you are!", false);
		// Checks if the issuer can interact with the target

		if (!guild.getSelfMember().canInteract(target))
			throw new HierarchyException(null);
		// Checks if the issuer can interact with the target

		EventWaiter ew = new EventWaiter(member.getUser(), channel);
		ew.setTimeout(20);
		if (!ew.getBoolean("Are you sure you want to "
		    + action.toString().toLowerCase()
		    + " **`"
		    + target.getUser().getName()
		    + "#"
		    + target.getUser().getDiscriminator()
		    + "`**?"))
			throw new CanceledException();
		// Creates the "Are you sure?" prompt

		// TODO sends even if hierarchy error
		if (!target.getUser().isBot())
			target.getUser()
			    .openPrivateChannel()
			    .queue(dm -> dm
			        .sendMessage("You got "
			            + (action.equals(ModAction.BAN) ? "banned" : "kicked")
			            + " out of \""
			            + guild.getName()
			            + "\" by "
			            + member.getUser().getName()
			            + " ("
			            + member.getEffectiveName()
			            + "); "
			            + reason)
			        .queue(null, t -> channel.sendMessage(BotUtils.buildEmbed(null,
			            "Could not send the reason message to the target user!", Constants.WARN))));

		// Sends the reason to the target if possible

		if (action == ModAction.KICK) {
			target.kick(reason).queue();
		} else if (action == ModAction.BAN) {
			target.ban(0, reason).queue();
		}
	}

}
