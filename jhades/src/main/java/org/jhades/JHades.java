package org.jhades;

import java.net.URL;
import java.util.List;
import org.jhades.model.ClasspathEntry;
import org.jhades.model.ClasspathResource;
import org.jhades.model.ClasspathResourceVersion;
import org.jhades.model.ClazzLoader;
import org.jhades.model.JarPair;
import org.jhades.reports.DuplicatesReport;
import org.jhades.service.ClasspathScanner;

/**
 *
 * See jHades documentation for how to use these commands - http://jhades.org
 *
 */
public class JHades {

    private ClasspathScanner scanner = new ClasspathScanner();

    public JHades printClassLoaderNames() {

        System.out.println("\n>> jHades printClassLoaders >> Printing classloader class names (ordered from child to parent):\n");

        List<ClazzLoader> classLoaders = scanner.findAllClassLoaders();
        boolean notSupportedFound = false;

        for (ClazzLoader classLoader : classLoaders) {
            if (classLoader.isSupported()) {
                System.out.println(classLoader.getName());
            } else {
                notSupportedFound = true;
                System.out.println(classLoader.getName() + " - NOT SUPORTED");
            }
        }
        endCommand(classLoaders.size() > 0);

        if (notSupportedFound) {
            System.out.println("Note: NOT SUPPORTED class loader means that any classes loaded by such a classloader will not be found on any jHades queries. \n");
        }

        return this;
    }

    public JHades dumpClassloaderInfo() {

        System.out.println("\n>> jHades printClassLoaders >> Printing all classloader available info (from the class loader toString(), ordered from child to parent):\n");

        List<ClazzLoader> classLoaders = scanner.findAllClassLoaders();
        boolean notSupportedFound = false;

        for (ClazzLoader classLoader : classLoaders) {
            if (classLoader.isSupported()) {
                System.out.println("\n>>> Dumping available info for classloader " + classLoader.getName() + "\n");
                System.out.println(classLoader.getDetails());
            } else {
                notSupportedFound = true;
                System.out.println(classLoader.getName() + " - NOT SUPORTED");
            }
        }
        endCommand(classLoaders.size() > 0);

        if (notSupportedFound) {
            System.out.println("Note: NOT SUPPORTED class loader means that any classes loaded by such a classloader will not be found on any jHades queries. \n");
        }

        return this;
    }

    public JHades printClasspath() {

        System.out.println("\n>> jHades printClasspath >> Printing all class folder and jars on the classpath:\n");

        List<ClasspathEntry> classpathEntries = scanner.findAllClasspathEntries();
        ClazzLoader clazzLoader = null;

        for (ClasspathEntry entry : classpathEntries) {
            if (entry.getClassLoader() != null && !entry.getClassLoader().equals(clazzLoader)) {
                System.out.println(); // line break between class loaders
                clazzLoader = entry.getClassLoader();
            }
            System.out.println(entry.getClassLoaderName() + " - " + entry.getUrl());
        }

        endCommand(classpathEntries.size() > 0);

        return this;
    }

    public JHades findResource(String resource) {

        if (resource == null) {
            throw new IllegalArgumentException("Resource path cannot be null.");
        }

        System.out.println(">> jHades printResourcePath >> searching for " + resource + "\n");

        List<URL> allVersions = scanner.findAllResourceVersions(resource);
        boolean resultsFound = allVersions != null && allVersions.size() > 0;

        System.out.println("All versions:\n");
        for (URL version : allVersions) {
            System.out.println(version);
        }

        URL currentVersion = scanner.findCurrentResourceVersion(resource);

        if (resultsFound && currentVersion != null) {
            System.out.println("\nCurrent version being used: \n\n" + currentVersion);
        }

        endCommand(resultsFound);

        return this;

    }

    public JHades findClassByName(String classFullyQualifiedName) {
        if (classFullyQualifiedName == null) {
            throw new IllegalArgumentException("Class name cannot be null.");
        }

        String resourceName = classFullyQualifiedName.replaceAll("\\.", "/") + ".class";

        return findResource(resourceName);
    }

    public JHades findClass(Class clazz) {

        if (clazz == null) {
            throw new IllegalArgumentException("Class name cannot be null.");
        }

        System.out.println(">> jHades searchClass >> Searching for class: " + clazz.getCanonicalName() + "\n");

        ClasspathResource foundClass = scanner.findClass(clazz);

        for (ClasspathResourceVersion version : foundClass.getResourceFileVersions()) {
            System.out.println(version.getClasspathEntry().getUrl() + foundClass.getName() + " size = " + version.getFileSize());
        }

        endCommand(foundClass != null);

        return this;
    }

    public JHades findByRegex(String search) {

        if (search == null || search.isEmpty()) {
            throw new IllegalArgumentException("search string cannot be null or empty.");
        }

        System.out.println(">> jHades search >> Searching for resorce using search string: " + search + "\n");

        List<ClasspathResource> classpathResources = scanner.findByRegex(search);

        boolean resultsFound = classpathResources != null && classpathResources.size() > 0;

        if (resultsFound) {
            System.out.println("\nResults Found:\n");
            for (ClasspathResource classpathResource : classpathResources) {
                System.out.println(classpathResource.getName());
            }
        }

        endCommand(resultsFound);

        return this;
    }

    public JHades multipleClassVersionsReport() {
        multipleClassVersionsReport(true);
        return this;
    }

    public JHades multipleClassVersionsReport(boolean excludeSameSizeDups) {
        List<ClasspathResource> resourcesWithDuplicates = scanner.findAllResourcesWithDuplicates(excludeSameSizeDups);

        DuplicatesReport report = new DuplicatesReport(resourcesWithDuplicates);
        report.print();

        return this;
    }

    public JHades overlappingJarsReport() {
        System.out.println("\n>> jHades - scanning classpath for overlapping jars: \n");

        List<JarPair> jarOverlapReportLines = scanner.findOverlappingJars();

        for (JarPair jarOverlapReportLine : jarOverlapReportLines) {
            String reportLine = jarOverlapReportLine.getJar1().getUrl() + " overlaps with \n" + jarOverlapReportLine.getJar2().getUrl()
                    + " - total overlapping classes: " + jarOverlapReportLine.getDupClassesTotal() + " - ";
            if (jarOverlapReportLine.getJar1().getClassLoader().equals(jarOverlapReportLine.getJar2().getClassLoader())) {
                reportLine += "same classloader ! This is an ERROR!\n";
            } else {
                reportLine += "different classloaders.\n";
            }
            System.out.println(reportLine);
        }

        endCommand(jarOverlapReportLines.size() > 0);

        return this;
    }

    private void endCommand(boolean resultsFound) {
        if (!resultsFound) {
            System.out.println("No results found.\n");
        } else {
            System.out.println("");
        }
    }
}
