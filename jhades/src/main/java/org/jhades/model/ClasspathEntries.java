package org.jhades.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jhades.service.ClasspathScannerListener;
import org.jhades.utils.StdOutLogger;

/**
 *
 * Utility class for the
 *
 * @see ClasspathEntry class
 */
public final class ClasspathEntries {

    private ClasspathEntries() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    /**
     *
     * Scans for classpath resources in a list of classpath entries (jars or class folders).
     *
     * class folders are walked through using some of the java 7 NIO functionality.
     *
     * @param classpathEntries - the list of classpath entries to be scanned
     * @return - the list of classpath resources found by scanning the provided classpath entries.
     */
    public static List<ClasspathResource> findClasspathResourcesInEntries(List<ClasspathEntry> classpathEntries,
            StdOutLogger logger, ClasspathScannerListener listener) {

        // find all classpath resource versions
        List<ClasspathResourceVersion> allResourceVersions = new ArrayList<>();

        Map<String, ClasspathResource> resourcesPerNameMap = new HashMap<>();

        try {

            for (ClasspathEntry entry : classpathEntries) {
                if (listener != null) {
                    listener.onEntryScanStart(entry);
                }
                allResourceVersions.addAll(entry.getResourceVersions());
                if (listener != null) {
                    listener.onEntryScanEnd(entry);
                }
            }

            for (ClasspathResourceVersion resourceVersion : allResourceVersions) {
                String resourceName = resourceVersion.getResourceName();
                if (!resourcesPerNameMap.containsKey(resourceName)) {
                    resourcesPerNameMap.put(resourceName, new ClasspathResource(resourceName, resourceVersion));
                } else {
                    resourcesPerNameMap.get(resourceName).getResourceFileVersions().add(resourceVersion);
                }
            }
        } catch (URISyntaxException | IOException ex) {
            logger.error(ex.getMessage(), ex);
        }

        return new ArrayList<>(resourcesPerNameMap.values());
    }
}
