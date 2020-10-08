package com.mz.libot.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

public final class FinderUtils {

	private static final StringSimilarityService SIMILARITY_SERVICE = new StringSimilarityServiceImpl(
	    new JaroWinklerStrategy());

	private FinderUtils() {}

	//////////////////////////////////////////////////////////////////////////////////////
	// Users
	//////////////////////////////////////////////////////////////////////////////////////

	public static List<User> preferFromGuild(List<User> users, Guild guild) {
		List<User> result = new ArrayList<>(users);
		Collections.sort(result, (u1, u2) -> {

			int comparatorReturn = 0;
			if (!(guild.isMember(u1) ^ guild.isMember(u2))) {
				comparatorReturn = 0;

			} else if (guild.isMember(u1)) {
				comparatorReturn = -1;

			} else if (guild.isMember(u2)) {
				comparatorReturn = 1;
			}
			return comparatorReturn;
		});
		return result;
	}

	/**
	 * Searches all visible {@link User}s for matching/similar name and mention.
	 *
	 * @param query
	 * @return list of found users, sorted by similarity to the query, with direct
	 *         mentions first
	 */
	public static List<User> findUsers(String query) {
		String text = query.toLowerCase().trim();

		List<User> found = new ArrayList<>();
		SnowflakeCacheView<User> userCache = BotData.getJDA().getUserCache();

		findSnowflakeById(userCache, query).ifPresent(found::add);
		found.addAll(findUsersFromMentions(userCache, text));
		found.addAll(findUsersFromStream(userCache.stream(), text));

		return Collections.unmodifiableList(found);
	}

	private static List<User> findUsersFromMentions(SnowflakeCacheView<User> cache, String query) {
		List<User> matched = new ArrayList<>();

		Matcher matcher = MentionType.USER.getPattern().matcher(query);

		while (matcher.find()) {
			try {
				User u = cache.getElementById(matcher.group(1));
				if (u != null && !matched.contains(u))
					matched.add(u);
			} catch (NumberFormatException e) {
				// Can be ignored
			}
		}

		return matched;
	}

	private static List<User> findUsersFromStream(Stream<User> users, String query) {
		List<User> bruteforce = users.filter(u -> getPriority(query, u.getName().toLowerCase().trim()) > 0)
		    .collect(Collectors.toList());

		bruteforce
		    .sort((u1, u2) -> compare(u2.getName().toLowerCase().trim(), u1.getName().toLowerCase().trim(), query));

		return bruteforce;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Roles
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Searches all {@link Role}s in a {@link Guild} for matching/similar name and
	 * mention.
	 *
	 * @param query
	 * @param guild
	 * @return list of found roles, sorted by similarity to the query, with direct
	 *         mentions first (can be empty)
	 */
	public static List<Role> findRoles(String query, Guild guild) {
		String text = query.toLowerCase().trim();

		List<Role> found = new ArrayList<>();
		SnowflakeCacheView<Role> roleCache = guild.getRoleCache();

		findSnowflakeById(roleCache, query).ifPresent(found::add);
		found.addAll(findRolesFromMentions(roleCache, text));
		found.addAll(findRolesFromStream(roleCache.stream(), text));

		return Collections.unmodifiableList(found);
	}

	private static List<Role> findRolesFromMentions(SnowflakeCacheView<Role> cache, String query) {
		List<Role> matched = new ArrayList<>();

		Matcher matcher = MentionType.ROLE.getPattern().matcher(query);

		while (matcher.find()) {
			try {
				Role role = cache.getElementById(matcher.group(1));
				if (role != null && !matched.contains(role))
					matched.add(role);
			} catch (NumberFormatException e) {
				// Can be ignored
			}
		}

		return matched;
	}

	private static List<Role> findRolesFromStream(Stream<Role> roles, String query) {
		List<Role> bruteforce = roles.filter(r -> getPriority(query, r.getName().toLowerCase().trim()) > 0)
		    .collect(Collectors.toList());

		bruteforce
		    .sort((r1, r2) -> compare(r2.getName().toLowerCase().trim(), r1.getName().toLowerCase().trim(), query));

		return bruteforce;
	}

	private static <T extends ISnowflake> Optional<T> findSnowflakeById(SnowflakeCacheView<T> index, String query) {
		try {
			long id = MiscUtil.parseSnowflake(query);
			return Optional.ofNullable(index.getElementById(id));

		} catch (NumberFormatException e) {
			// Query is not an ID, we can ignore this exception
			return Optional.empty();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Members
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Searches all {@link Member}-s in a {@link Guild} for matching/similar name and
	 * mention.
	 *
	 * @param query
	 * @param guild
	 * @return list of found members, sorted by similarity to the query, with direct
	 *         mentions first (can be empty)
	 */
	public static List<Member> findMembers(String query, Guild guild) {
		String text = query.toLowerCase().trim();

		List<Member> found = new ArrayList<>();
		MemberCacheView memberCache = guild.getMemberCache();
		findMemberById(memberCache, query).ifPresent(found::add);
		found.addAll(findMembersFromMentions(memberCache, text));
		found.addAll(findMembersFromStream(guild.getMemberCache().stream(), text));

		return Collections.unmodifiableList(found);
	}

	private static Optional<Member> findMemberById(MemberCacheView cache, String query) {
		try {
			long id = MiscUtil.parseSnowflake(query);
			return Optional.ofNullable(cache.getElementById(id));

		} catch (NumberFormatException e) {
			// Query is not an ID, we can ignore this exception
			return Optional.empty();
		}
	}

	private static List<Member> findMembersFromMentions(MemberCacheView cache, String query) {
		List<Member> matched = new ArrayList<>();

		Matcher matcher = MentionType.USER.getPattern().matcher(query);

		while (matcher.find()) {
			try {
				Member u = cache.getElementById(matcher.group(1));
				if (u != null && !matched.contains(u))
					matched.add(u);
			} catch (NumberFormatException e) {
				// Can be ignored
			}
		}

		return matched;
	}

	private static List<Member> findMembersFromStream(Stream<Member> members, String query) {
		List<Member> bruteforce = members.filter(m -> getPriority(query, m.getEffectiveName().toLowerCase().trim()) > 0)
		    .collect(Collectors.toList());

		bruteforce.sort((m1, m2) -> compare(m2.getEffectiveName().toLowerCase().trim(),
		    m1.getEffectiveName().toLowerCase().trim(), query));

		return bruteforce;
	}

	private static int compare(String name1, String name2, String query) {
		int compared = Integer.compare(getPriority(query, name1.toLowerCase().trim()),
		    getPriority(query, name2.toLowerCase().trim()));

		if (compared == 0) {
			return Double.compare(SIMILARITY_SERVICE.score(name1, query), SIMILARITY_SERVICE.score(name2, query));
		}

		return compared;

	}

	private static int getPriority(String actual, String expected) {
		int priority = 0;
		if (expected.equals(actual)) {
			priority = 4;

		} else if (expected.startsWith(actual)) {
			priority = 3;

		} else if (expected.endsWith(actual)) {
			priority = 2;

		} else if (expected.contains(actual)) {
			priority = 1;
		}

		return priority;
	}

}
