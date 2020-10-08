package com.mz.libot.commands.automod;

import java.util.List;

import com.mz.libot.core.BotData;
import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.FinderUtils;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Guild.MFALevel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AutoRoleCommand extends Command {

	private static void autoRoleStatus(MessageChannel channel, Guild guild, String usage) {
		Long targetRoleId = null;

		if (ProviderManager.AUTO_ROLE.getData().get(guild.getIdLong()) != null) {
			targetRoleId = ProviderManager.AUTO_ROLE.getData().get(guild.getIdLong());
		}
		// If not configured, sets targetRole to null

		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("AutoRole health report for " + guild.getName());

		if (targetRoleId == null) {
			builder.setColor(Constants.DISABLED);
			builder.setDescription("No AutoRole active for this guild! Initiate it with " + usage + "!");
			channel.sendMessage(builder.build()).queue();
			// If AutoRole is not configured for this guild
			return;
		}

		Role targetRole = guild.getRoleById(targetRoleId);
		Member self = guild.getSelfMember();
		if (targetRole == null) {
			builder.setColor(Constants.FAILURE);
			builder.setDescription("AutoRole is active for this guild, but the target role could not have been found. "
			    + "Please fix configuration with "
			    + usage
			    + "!");
			// If the role AutoRole's configuration is pointing at is no longer available
			// (determined by role's ID)

		} else if (guild.getRequiredMFALevel() == MFALevel.TWO_FACTOR_AUTH) {
			builder.setColor(Constants.FAILURE);
			builder.setDescription(
			    "AutoRole is active for this guild but 2FA now is required to execute any administrative action. "
			        + "Please disable 2FA requirement in Settings > Moderation > Disable 2FA Requirement!"
			        + ".\n\nAutoRole is pointing towards "
			        + targetRole.getName());
			// If the guild requires 2FA for administrative actions

		} else if (!self.canInteract(targetRole)) {
			builder.setColor(Constants.FAILURE);
			builder.setDescription("AutoRole is active for this guild but "
			    + BotData.getName()
			    + " no longer has permission to interact with the target role ("
			    + targetRole.getName()
			    + "). Please move "
			    + BotData.getName()
			    + " in a role __above__ "
			    + targetRole.getName()
			    + " to solve this issue!.\n\nAutoRole is pointing towards "
			    + targetRole.getName());
			// If LiBot can no longer interact with the target role

		} else if (!self.hasPermission(Permission.MANAGE_ROLES)) {
			builder.setColor(Constants.FAILURE);
			builder
			    .setDescription("AutoRole active for this guild, but none of my roles has permission to manage roles."
			        + "\n\nAutoRole is pointing towards "
			        + targetRole.getName());
			// If LiBot no longer has the "Manage roles" permission

		} else {
			builder.setColor(Constants.SUCCESS);
			builder.setDescription(
			    "AutoRole active for this guild and should work just fine!\n\nAutoRole is pointing towards "
			        + targetRole.getName());
			// If everything works fine
		}

		channel.sendMessage(builder.build()).queue();
	}

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		if (!params.check()) {
			autoRoleStatus(event.getChannel(), event.getGuild(), this.getUsage(event.getGuild()));

		} else {
			Member member = event.getGuild().getMember(event.getAuthor());
			this.checkPermissions(member);

			List<Role> roles = FinderUtils.findRoles(params.get(0), event.getGuild());
			if (roles.isEmpty())
				throw new CommandException("Role '" + params.get(0) + "' does not exist!", false);

			if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
				throw new CommandException(
				    BotData.getName() + " must have the \"Manage roles\" permission for that operation!", false);

			if (event.getGuild().getRequiredMFALevel() == MFALevel.TWO_FACTOR_AUTH)
				throw new CommandException("You must disable the 2FA requirement for your guild first!", false);

			Role role = roles.get(0);
			if (member != null && !member.canInteract(role))
				throw new CommandException("That role is higher in hierarchy tree than " + BotData.getName() + "'s is!",
				    false);

			if (!event.getGuild().getSelfMember().canInteract(role))
				throw new CommandException("That role is higher in hierarchy tree than " + BotData.getName() + "'s is!",
				    false);

			if (ProviderManager.AUTO_ROLE.getData().containsKey(event.getGuild().getIdLong()))
				event.getChannel().sendMessage("Overwritten old AutoRole configuration..").queue();

			ProviderManager.AUTO_ROLE.register(event.getGuild(), role);

			event.getChannel()
			    .sendMessage(BotUtils
			        .buildEmbed("AutoRole has been configured! Every member that joins from now on will be given the \""
			            + role.getName()
			            + "\" role!", Constants.SUCCESS))
			    .queue();
		}
	}

	@Override
	public String getInfo() {
		return "Once enabled, every new member that joins will be promoted to the chosen role. "
		    + "To get AutoRole's health report for your guild, "
		    + "use command 'autorole' without any parameters.";
	}

	@Override
	public String getName() {
		return "AutoRole";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.AUTOMOD;
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.MANAGE_ROLES);
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		// Do not perform the permission check - it's performed if the user actually wants to
		// alter the configuration
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("role (optional)");
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

}
