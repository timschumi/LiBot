package com.mz.libot.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import javax.script.ScriptException;

import org.junit.jupiter.api.Test;

import com.mz.libot.core.BotUtils;
import com.mz.libot.core.Constants;
import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandListBuilder;
import com.mz.libot.core.commands.exceptions.runtime.NumberOverflowException;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.Parser;
import com.mz.libot.utils.Timestamp;
import com.mz.libot.utils.eval.EvalEngine;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiBotTest {

	@Test
	void testCommands() {
		CommandListBuilder cb = new CommandListBuilder();
		cb.registerAll();
		List<Command> invalid = cb.getInvalid();
		assertTrue(invalid.size() == 0, "Illegal command configuration found in " + invalid.size() + "");
	}

	@Test
	void testTimestamp() {
		assertEquals("1970-01-01", Timestamp.formatTimestamp(0, "yyyy-MM-dd"));

		assertEquals("1970-01-01", Timestamp
			.formatTimestamp(Instant.ofEpochMilli(0).atZone(ZoneId.systemDefault()).toOffsetDateTime(), "yyyy-MM-dd"));

	}

	@Test
	void testEvalEngine() throws ScriptException {
		EvalEngine engine = EvalEngine.getEngine();

		assertEquals(engine.eval("print(\"assertion\")", null, null).getOutput(), "assertion");
		assertEquals(engine.eval("return \"assertion\"", null, null).getResult(), "assertion");

		try {
			engine.eval("throw new IllegalArgumentException()", null, null);
			fail();
		} catch (ScriptException e) {
			if (!(e.getCause().getCause() instanceof IllegalArgumentException)) {
				e.printStackTrace();
				fail();
			}
		}

	}

	@Test
	void testParameters() {
		String input = "genericCommand yes 1 remove -r fal se";

		Parameters paramsWithPrefix = new Parameters(5, Constants.DEFAULT_COMMAND_PREFIX + input);

		assertArrayEquals(new String[] {
			"yes", "1", "remove", "-r", "fal se"
		}, paramsWithPrefix.asArray());
		assertArrayEquals(new String[] {
			"yes", "1", "remove", "-r", "fal se"
		}, new Parameters(5, "" + input).asArray());

	}

	@Test
	void testParameterParser() {
		String input = "hello\n\n	world  ";

		assertArrayEquals(new String[] {
			"hello", "world"
		}, Parameters.parseParameters(input, 0, true, false));
		assertArrayEquals(new String[] {
			"hello", "	world"
		}, Parameters.parseParameters(input, 0, false, false));

		assertArrayEquals(new String[] {
			"hello", "	world"
		}, Parameters.parseParameters("<@!1> command " + input, 0, false, true));
		assertArrayEquals(new String[] {
			"hello", "	world"
		}, Parameters.parseParameters("<@!1>command " + input, 0, false, true));
		assertArrayEquals(new String[] {
			"hello", "	world"
		}, Parameters.parseParameters("*command " + input, 0, false, true));
	}

	@Test
	void testParser() throws NumberFormatException, NumberOverflowException {
		assertEquals(2147483647, Parser.parseInt("2147483647"));
		assertThrows(NumberOverflowException.class, () -> Parser.parseInt("2147483648"));
	}

	@Test
	void testMarkdownParser() {
		assertEquals("\\*\\*bold\\*\\*", BotUtils.escapeMarkdown("**bold**"));
		assertEquals("**bold**", BotUtils.unescapeMarkdown("\\*\\*bold\\*\\*"));
	}

}
