package de.george.g3dit.util;

import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class ClasspathScanUtil {
	public static <T> List<Class<T>> findSubtypesOf(Class<T> type, String... packageNames) {
		try (ScanResult scanResult = new ClassGraph().enableClassInfo().whitelistPackages(packageNames).scan()) {
			return scanResult.getClassesImplementing(type.getName()).loadClasses(type);
		}
	}
}
