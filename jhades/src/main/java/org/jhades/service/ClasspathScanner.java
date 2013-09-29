package org.jhades.service;

import org.jhades.model.ClazzLoaderFactory;
import org.jhades.model.ClasspathResource;
import org.jhades.model.ClasspathEntry;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jhades.model.ClasspathEntries;
import org.jhades.model.ClasspathResourceVersion;
import org.jhades.model.ClasspathResources;
import org.jhades.model.ClazzLoader;
import org.jhades.model.ClazzLoaders;
import org.jhades.model.JarPair;
import org.jhades.utils.StdOutLogger;

/**
 *
 * This class contains a series of commands for querying the classpath.
 *
 * The classpath is scanned from the classloader where the scanner was created, going all the way down to the bootstrap
 * classloader.
 *
 * For each classloader, the location of it's jars and class folders is extracted. Each jar/class folder is unzipped and
 * scanned for class files and any other types of resource files.
 *
 * If the classpath scanner was created on the top-most class loader of the application, it should be able to access all
 * the classes / resource files of your application.
 *
 * It's possible in some cases that some classes are not found via classloader inspection, here are 2 known cases:
 *
 * 1. jHades was NOT created on the top-most class loader, this should be very uncommon
 *
 * 2. One of your classloaders is not yet supported - there should be a warning message on your log.
 *
 * The classpath folders and jars are scanned using JDK 7 functionality provided by the Java NIO framework.
 *
 * jHades only depends on JDK 7 classes, in order to prevent introducing library dependencies (that could themselves
 * cause classpath problems).
 *
 * Note: For the moment jHades only supports URL class loaders,
 *
 * @see ClazzLoaderFactory
 *
 */
public class ClasspathScanner {

    public static final String BOOTSTRAP_CLASS_LOADER = "Bootstrap class loader";
    private StdOutLogger logger = StdOutLogger.getLogger();

    /**
     *
     * finds all the entries on the classpath; this includes jars and class folders on all class loaders, all the way
     * down and including the JVM bootstrap class loader.
     *
     * @return a list of classpath entries, or an empty list if none where found.
     */
    public List<ClasspathEntry> findAllClasspathEntries() {
        // try to extract all classpath entries from the class loaders
        List<ClazzLoader> classLoaders = findAllClassLoaders(getClass().getClassLoader());

        List<ClasspathEntry> allClasspathEntries = ClazzLoaders.findAllClasspathEntries(classLoaders);

        // scan the class path variable for missing entries, just in case
        String classpath = System.getProperty("java.class.path");
        String separator = System.getProperty("path.separator");

        if (classpath != null && separator != null) {
            String[] paths = classpath.split(separator);
            for (String pathEntry : paths) {
                boolean found = false;
                for (ClasspathEntry classpathEntry : allClasspathEntries) {
                    if (classpathEntry.getUrl().contains(pathEntry)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (!pathEntry.endsWith("/")) {
                        pathEntry = pathEntry + "/";
                    }
                    logger.warn("This entry on the classpath system property java.class.path was not found on any classloader:" + pathEntry);
                }
            }
        } else {
            logger.warn("could not parse classpath.");
        }

        return allClasspathEntries;
    }

    /**
     *
     * Scans the classpath for all resource files.
     *
     * @return - the full list of resources on the classpath, including all its known versions.
     */
    public List<ClasspathResource> findAllClasspathResources() {
        List<ClasspathEntry> classpathEntries = findAllClasspathEntries();
        return ClasspathEntries.findClasspathResourcesInEntries(classpathEntries, logger, null);
    }

    /**
     * Finds all class loaders on the classpath.
     *
     * All classloaders names will be returned, with an indication if they are supported by jHades or not.
     *
     * @return
     */
    public List<ClazzLoader> findAllClassLoaders() {
        return findAllClassLoaders(getClass().getClassLoader());
    }

    /**
     *
     * Find all versions of a given classpath resource.
     *
     * @param resourceUrl - the resource url being searched
     * @return - a list of urls to all versions of the resource, or an empty list if not found.
     *
     */
    public List<URL> findAllResourceVersions(String resourceUrl) {
        ClassLoader cl = getClass().getClassLoader();
        List<URL> results = new ArrayList<>();

        try {
            Enumeration<URL> urls = cl.getResources(resourceUrl);
            while (urls.hasMoreElements()) {
                results.add(urls.nextElement());
            }
        } catch (IOException exception) {
            System.out.println("Could not find the versions of classpath resource: " + resourceUrl);
            exception.printStackTrace();
        }
        return results;
    }

    /**
     *
     * Finds the version of a classpath resource that is currently being loaded by the JVM.
     *
     * @param resourceUrl - the url of the classpath resource
     * @return - the URL of the resource version being used, or null if not found.
     */
    public URL findCurrentResourceVersion(String resourceUrl) {
        ClassLoader cl = getClass().getClassLoader();
        return cl.getResource(resourceUrl);
    }

    /**
     *
     * Search for a given class on the classpath, returns the list of all class versions.
     *
     * @param clazz - the class being searched.
     * @return - the classpath resource containing all the class versions, or null if not found
     */
    public ClasspathResource findClass(Class clazz) {
        String classResourceName = clazz.getName().replace(".", "/") + ".class";
        List<ClasspathResource> allResources = findAllClasspathResources();
        ClasspathResource foundResource = null;

        for (ClasspathResource resource : allResources) {
            if (resource != null && resource.getName() != null
                    && resource.getName().endsWith(classResourceName)) {
                foundResource = resource;
                break;
            }
        }
        return foundResource;
    }

    /**
     *
     * finds a resource on the classpath using a regular expression.
     *
     * @param search - search regular expression
     * @return - the list of classpath resources that match the regular expression
     */
    public List<ClasspathResource> findByRegex(String search) {
        List<ClasspathResource> allResources = findAllClasspathResources();
        List<ClasspathResource> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(search);

        for (ClasspathResource resource : allResources) {
            if (resource != null && resource.getName() != null
                    && pattern.matcher(resource.getName()).find()) {
                matches.add(resource);
            }
        }
        return matches;
    }

    /**
     *
     * Finds a list of all classpath resources that contain duplicates.
     *
     * A duplicate classpath resource can be for example a configuration file such as log4.xml, that exists by accident
     * multiple times on the classpath.
     *
     * @param excludeSameSizeDups - excludes from the reports files for which all classpath versions have the same size
     * @return classpath resources that have multiple versions on the classpath
     */
    public List<ClasspathResource> findAllResourcesWithDuplicates(boolean excludeSameSizeDups) {
        List<ClasspathResource> resourceFiles = findAllClasspathResources();

        return ClasspathResources.findResourcesWithDuplicates(resourceFiles, excludeSameSizeDups);
    }

    /**
     *
     * Searches for classpath entries on the classpath, starting on the given input classloader, and scanning
     * recursively it's parents, all the way until the JVM bootstrap classloader.
     *
     * @param classLoader the class loader to be scanned
     *
     * @return the list of classpath entries found on the given class loader.
     */
    private List<ClazzLoader> findAllClassLoaders(ClassLoader classLoader) {
        List<ClazzLoader> classLoaders = new ArrayList<>();
        if (classLoader != null) {
            ClazzLoader cl = ClazzLoaderFactory.createClazzLoader(classLoader);
            if (cl != null) {
                classLoaders.add(cl);
            }
            classLoaders.addAll(findAllClassLoaders(classLoader.getParent()));
        } // if the class loader is null, means we got all the way to the bootstrap class loader - add it as well
        else {
            classLoaders.add(ClazzLoaderFactory.createBootstrapClassLoader());
        }
        return classLoaders;
    }

    /**
     *
     * Finds all class files that have more than one version on the classpath
     *
     * @param classpathResources - the list of classpath resources
     * @param excludeSameSizeDups - true if only duplicates of different class sizes are considered
     * @return - the list of class files that have multiple versions
     */
    public List<ClasspathResource> findClassFileDuplicates(List<ClasspathResource> classpathResources, boolean excludeSameSizeDups) {
        List<ClasspathResource> classFilesWithDuplicates = ClasspathResources.findResourcesWithDuplicates(classpathResources, excludeSameSizeDups);
        return ClasspathResources.filterClassFilesOnly(classFilesWithDuplicates);
    }

    /**
     *
     * @return - a list of jar pairs that have overlapping class files - scans the whole classpath
     *
     * By default all duplicates are shown
     *
     */
    public List<JarPair> findOverlappingJars() {
        return findOverlappingJars(findAllClasspathResources(), false);
    }

    /**
     *
     * @return - a list of jar pairs that have overlapping class files - scans the whole classpath
     *
     */
    public List<JarPair> findOverlappingJars(boolean excludeSameSizeDups) {
        return findOverlappingJars(findAllClasspathResources(), excludeSameSizeDups);
    }

    /**
     *
     * @return - a list of jar pairs that have overlapping class files - only a limited list of classpath resources is
     * considered.
     *
     */
    public List<JarPair> findOverlappingJars(List<ClasspathResource> classpathResources, boolean excludeSameSizeDups) {
        List<ClasspathResource> classFilesWithDuplicates = findClassFileDuplicates(classpathResources, excludeSameSizeDups);

        //jar overlap report
        Map<JarPair, JarPair> overlapPairs = new HashMap<>();
        for (ClasspathResource classFile : classFilesWithDuplicates) {
            List<ClasspathResourceVersion> versions = classFile.getResourceFileVersions();
            findOverlappingJarsPairs(versions, overlapPairs, 0);
        }

        List<JarPair> overlapReportLines = new ArrayList<>(overlapPairs.keySet());

        Comparator<JarPair> comparator = new Comparator<JarPair>() {
            @Override
            public int compare(JarPair line1, JarPair line2) {
                return -1 * line1.getDupClassesTotal().compareTo(line2.getDupClassesTotal());
            }
        };

        Collections.sort(overlapReportLines, comparator);

        return overlapReportLines;
    }

    private void findOverlappingJarsPairs(List<ClasspathResourceVersion> versions, Map<JarPair, JarPair> overlapPairs, int anchorIndex) {
        ClasspathResourceVersion anchor = versions.get(anchorIndex);
        for (int i = anchorIndex + 1; i < versions.size(); i++) {
            JarPair overlapPair = new JarPair(
                    anchor.getClasspathEntry(), versions.get(i).getClasspathEntry());
            if (!overlapPairs.containsKey(overlapPair)) {
                overlapPairs.put(overlapPair, overlapPair);
            }
            overlapPairs.get(overlapPair).incrementDupClassesTotal();
        }
        if (anchorIndex + 1 < versions.size()) {
            findOverlappingJarsPairs(versions, overlapPairs, anchorIndex + 1);
        }
    }
}
