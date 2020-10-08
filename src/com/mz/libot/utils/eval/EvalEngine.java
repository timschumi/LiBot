package com.mz.libot.utils.eval;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.libot.utils.entities.EvalResult;

public class EvalEngine {

	private static final Logger LOG = LoggerFactory.getLogger(EvalEngine.class);

	public static final List<PackageImport> DEFAULT_JAVA_IMPORTS = PackageImport.getStringPackageImports(
	    Arrays.asList("java.lang", "java.io", "java.math", "java.util", "java.util.concurrent", "java.time"));

	public static final List<PackageImport> DEFAULT_JDA_IMPORTS = PackageImport
	    .getStringPackageImports(Arrays.asList("net.dv8tion.jda.api.entities.impl", "net.dv8tion.jda.api.managers",
	        "net.dv8tion.jda.api.entities", "net.dv8tion.jda.api"));
	public static final List<PackageImport> DEFAULT_LIBOT_IMPORTS;

	static {
		String base = "com.mz.libot";
		DEFAULT_LIBOT_IMPORTS = PackageImport.getStringPackageImports(Arrays.asList( // NOSONAR it's unmodifiable
		// @formatter:off
				base,

				base + ".commands.administrative",
				base + ".commands.automod",
				base + ".commands.customization",
				base + ".commands.fun",
				base + ".commands.informative",
				base + ".commands.libot",
				base + ".commands.messaging",
				base + ".commands.moderation",
				base + ".commands.music",
				base + ".commands.searching",
				base + ".commands.utilities",

				base + ".core",
				base + ".core.commands",
				base + ".core.commands.exceptions.launch",
				base + ".core.commands.exceptions.runtime",
				base + ".core.commands.utils",
				base + ".core.data",
				base + ".core.data.properties",
				base + ".core.data.properties.impl",
				base + ".core.data.providers",
				base + ".core.data.providers.impl",
				base + ".core.entities",
				base + ".core.entities.ftp",
				base + ".core.ftp",
				base + ".core.handlers",
				base + ".core.handlers.command",
				base + ".core.handlers.exception",
				base + ".core.listeners",
				base + ".core.managers",
				base + ".core.modified",
				base + ".core.music",
				base + ".core.music.entities",
				base + ".core.music.entities.exceptions",
				base + ".core.processes",
				base + ".core.processes.tasks",

				base + ".main.changelog",
				base + ".main.cli",
				base + ".main.ui",

				base + ".utils",
				base + ".utils.entities",
				base + ".utils.eval"
			// @formatter:on
		));

	}

	private final ScriptEngine engine;
	private static final EvalEngine SINGLETON_ENGINE = new EvalEngine();

	/**
	 * @return the singleton engine
	 */
	public static EvalEngine getEngine() {
		return SINGLETON_ENGINE;
	}

	private EvalEngine() {
		this.engine = new GroovyScriptEngineImpl();
	}

	/**
	 * Evaluates a script
	 *
	 * @param script
	 *            script to evaluate
	 * @param imports
	 *            imports to use
	 * @param bindings
	 *            bindings (=shortcuts) to use
	 * @return evaluation result
	 * @throws ScriptException
	 *             if script fails to compile
	 */
	public EvalResult eval(String script, List<? extends Import> imports,
	                       List<Binding> bindings) throws ScriptException {
		ScriptEngine engine = this.engine;

		StringWriter outSw = new StringWriter();
		StringWriter errorSw = new StringWriter();

		PrintWriter outPw = new PrintWriter(outSw);
		PrintWriter errorPw = new PrintWriter(errorSw);

		engine.getContext().setWriter(outPw);
		engine.getContext().setErrorWriter(errorPw);
		// Sets err and out writers

		Bindings engineBindings = new SimpleBindings();

		if (bindings != null) {
			for (Binding binding : bindings) {
				engineBindings.put(binding.getKey(), binding.getValue());
			}
		}
		engine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
		// Places bindings

		StringBuilder importScript = new StringBuilder();
		if (imports != null)
			for (Import importToAdd : imports)
				importScript.append("import " + importToAdd.getImport() + ";\n");
		String scriptFull = importScript.toString() + script;
		// Adds imports

		LOG.debug("Evaluating {}", scriptFull);
		Object result = this.engine.eval(scriptFull);
		LOG.debug("Evaluation done with no errors");

		return new EvalResult(result, outSw.toString(), errorSw.toString());
	}
}
