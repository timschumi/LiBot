package com.mz.libot.core;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

class FinderUtilsTest {

	@Test
	void testPreferFromGuild() {
		User mockedUserFromGuild = mock(User.class);
		User mockedUserNotFromGuild = mock(User.class);
		Guild mockedGuild = mock(Guild.class);
		when(mockedGuild.isMember(mockedUserFromGuild)).thenReturn(true);
		when(mockedGuild.isMember(mockedUserNotFromGuild)).thenReturn(false);
		List<User> userList = Arrays.asList(mockedUserNotFromGuild, mockedUserFromGuild);
		List<User> invertedUserList = Arrays.asList(mockedUserFromGuild, mockedUserNotFromGuild);

		userList = FinderUtils.preferFromGuild(userList, mockedGuild);
		invertedUserList = FinderUtils.preferFromGuild(invertedUserList, mockedGuild);

		assertEquals(mockedUserFromGuild, userList.get(0));
		assertEquals(mockedUserFromGuild, invertedUserList.get(0));
	}

}
