package com.mz.libot.core.commands.exceptions.startup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mz.libot.core.commands.exceptions.CommandException;

import net.dv8tion.jda.api.Permission;

/**
 * Signals that a member does not have sufficient permissions to execute an action.
 * 
 * @author Marko Zajc
 */
public class MemberInsufficientPermissionsException extends CommandException {

	private final List<Permission> missingPermissions;

	public MemberInsufficientPermissionsException(Permission... missingPermissions) {
		super(false);
		this.missingPermissions = Collections.unmodifiableList(Arrays.asList(missingPermissions));
	}

	public MemberInsufficientPermissionsException(List<Permission> missingPermissions) {
		super(false);
		this.missingPermissions = Collections.unmodifiableList(missingPermissions);
	}

	public List<Permission> getMissingPermissions() {
		return this.missingPermissions;
	}

}
