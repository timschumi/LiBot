package com.mz.libot.utils.eval;

public class ClassImport implements Import {

	private String classPath;

	/**
	 * Lets you easily create a ClassImport from a class
	 * 
	 * @param classToImport
	 *            class to import
	 * @return import of <code>classToImport</code>
	 */
	public static ClassImport getClassImport(Class<?> classToImport) {
		return new ClassImport(classToImport.getCanonicalName());
	}

	/**
	 * Creates a new ClassImport
	 * 
	 * @param classPath
	 *            path to class
	 */
	public ClassImport(String classPath) {
		this.classPath = classPath;
	}

	@Override
	public String getImport() {
		return this.classPath;
	}

}
