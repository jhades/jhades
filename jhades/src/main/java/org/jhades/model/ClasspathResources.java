package org.jhades.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * Utility class for
 *
 * @see ClasspathResource
 *
 */
public final class ClasspathResources {

    private ClasspathResources() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    /**
     *
     * Filters a list of classpath resources, selecting only class files.
     *
     * @param classpathResources - a list of classpath resources
     * @return - the filtered list containing only class files, or an empty list if no matches found
     */
    public static final List<ClasspathResource> filterClassFilesOnly(List<ClasspathResource> classpathResources) {

        if (classpathResources == null) {
            throw new IllegalArgumentException("Classpath resources cannot be null.");
        }
        List<ClasspathResource> filtered = new ArrayList<>();

        for (ClasspathResource classpathResource : classpathResources) {
            String resourceName = classpathResource.getName();
            if (resourceName != null && resourceName.endsWith(".class")) {
                filtered.add(classpathResource);
            }
        }
        return filtered;
    }

    /**
     * Takes a list of classpath resources and sorts them by the number of classpath versions.
     *
     * The resources with the biggest number of versions will be first on the list.
     *
     *
     * @param resources to be sorted
     *
     */
    public static void sortByNumberOfVersionsDesc(List<ClasspathResource> resources) {
        // sort by number of version occurrences
        Comparator<ClasspathResource> sortByNumberOfVersionsDesc = new Comparator<ClasspathResource>() {
            @Override
            public int compare(ClasspathResource resource1, ClasspathResource resource2) {
                return -1 * new Integer(resource1.getResourceFileVersions().size()).compareTo(resource2.getResourceFileVersions().size());
            }
        };
        Collections.sort(resources, sortByNumberOfVersionsDesc);
    }

    /**
     * Inspects a given list of classpath resources, and returns only the resources that contain multiple versions.
     *
     * @param resourceFiles - the resource files to be inspected
     * @param excludeSameSizeDups - true to consider only as duplicates files with multiple versions with different file
     * sizes
     * @return - the list of resources with duplicates
     */
    public static List<ClasspathResource> findResourcesWithDuplicates(List<ClasspathResource> resourceFiles, boolean excludeSameSizeDups) {
        List<ClasspathResource> resourcesWithDuplicates = new ArrayList<>();

        // keep only entries with duplicates
        for (ClasspathResource resource : resourceFiles) {
            if (resource.hasDuplicates(excludeSameSizeDups)) {
                resourcesWithDuplicates.add(resource);
            }
        }
        return resourcesWithDuplicates;

    }
}
