package org.jhades.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Utility class for
 *
 * @see ClazzLoader
 */
public final class ClazzLoaders {

    private ClazzLoaders() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    public static List<ClasspathEntry> findAllClasspathEntries(List<ClazzLoader> classLoaders) {
        List<ClasspathEntry> classpathEntries = new ArrayList<>();
        if (classLoaders != null) {
            for (ClazzLoader classLoader : classLoaders) {
                classpathEntries.addAll(classLoader.getClasspathEntries());
            }
        }
        return classpathEntries;
    }
}
