package com.mz.libot.utils.eval;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PackageImport implements Import {

	private String packagePath;

	/**
	 * Lets you easily create a ClassImport from a class
	 *
	 * @param packageToImport
	 *            package to import
	 * @return import of <code>packageToImport</code>
	 */
	public static PackageImport getPackageImport(Package packageToImport) {
		if (packageToImport == null)
			throw new IllegalArgumentException("packageToImport can't be null!");

		return new PackageImport(packageToImport.getName());

	}

	public static List<PackageImport> getPackageImports(List<Package> packagesToImport) {
		return Collections.unmodifiableList(
		    packagesToImport.stream().map(PackageImport::getPackageImport).collect(Collectors.toList()));
	}

	public static List<PackageImport> getStringPackageImports(List<String> packagesToImport) {
		return Collections
		    .unmodifiableList(packagesToImport.stream().map(PackageImport::new).collect(Collectors.toList()));
	}

	/**
	 * Creates a new ClassImport
	 *
	 * @param packagePath
	 *            path to class
	 */
	public PackageImport(String packagePath) {
		this.packagePath = packagePath;
	}

	@Override
	public String getImport() {
		return this.packagePath + ".*";
	}

}
