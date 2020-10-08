package com.mz.libot.core.commands;

public enum CommandCategory {

	ADMINISTRATIVE,
	AUTOMATE,
	AUTOMOD,
	CUSTOMIZATION,
	GAMES,
	INFORMATIVE,
	LIBOT,
	MESSAGING,
	MODERATION,
	MONEY,
	MUSIC,
	SEARCHING,
	UTILITIES;

	public static CommandCategory getCategory(String categoryName) {
		String categoryNameUpper = categoryName.toUpperCase();
		for (CommandCategory category : CommandCategory.values()) {
			if (category.name().equals(categoryNameUpper))
				return category;
		}

		return null;

	}

}
