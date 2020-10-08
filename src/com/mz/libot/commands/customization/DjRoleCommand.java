package com.mz.libot.commands.customization;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.core.data.providers.ProviderManager;
import com.mz.libot.utils.entities.Customization;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class DjRoleCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		Customization customization = ProviderManager.CUSTOMIZATIONS.getCustomization(event.getGuild());
		Role role = event.getMessage().getMentionedRoles().stream().findAny().orElse(null);
		if (role != null) {
			this.checkPermissions(event.getMember());

			ProviderManager.CUSTOMIZATIONS.setCustomization(event.getGuild(), customization.setDjRole(role));

			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("DJ role configured",
			        "Only members with "
			            + role.getAsMention()
			            + " role will be able to use music commands from now on.",
			        Constants.SUCCESS))
			    .queue();

			return;
		}

		if (event.getMessage().mentionsEveryone()) {
			this.checkPermissions(event.getMember());

			ProviderManager.CUSTOMIZATIONS.setCustomization(event.getGuild(), customization.setDjRole(null));

			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("DJ role disabled",
			        "Everyone will be able to use music commands from now on.", Constants.DISABLED))
			    .queue();

			return;
		}

		Long roleId = customization.getDjRoleId();
		Role djRole = roleId == null ? null : event.getGuild().getRoleById(roleId);
		if (djRole != null) {
			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed("DJ role for this guild is "
			        + djRole.getAsMention()
			        + ". Only members that either have this role or the 'Administrator'"
			        + "permission will be able to use music commands", Constants.SUCCESS))
			    .queue();

		} else {
			event.getChannel()
			    .sendMessage(BotUtils.buildEmbed(
			        "DJ role is not configured for this guild. Everyone has access to the music commands.",
			        Constants.DISABLED))
			    .queue();

		}

	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.CUSTOMIZATION;
	}

	@Override
	public String getInfo() {
		return "Lets you manage the DJ role. DJ role allows you to simply manage who can use the music commands. "
		    + "If no DJ role is set, everyone will be able to use the music commands. "
		    + "Keep in mind that those with the 'Administrator' permission "
		    + "(including guild's owner, of course) can play music no matter their roles. "
		    + "To disable this feature, use it with `@everyone` as the parameter.";
	}

	@Override
	public String getName() {
		return "DJRole";
	}

	@Override
	public int getMinParameters() {
		return 0;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("@role (optional)");
	}

	@Override
	public Permission[] getPermissions() {
		return Commands.toArray(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.MANAGE_SERVER);
	}

	@Override
	public void startupCheck(GuildMessageReceivedEvent event, Parameters params) throws Throwable {
		// Do not perform the permission check - it's performed if the user actually wants to
		// alter the configuration
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("dj");
	}

}
