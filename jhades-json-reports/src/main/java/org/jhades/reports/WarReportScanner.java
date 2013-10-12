package org.jhades.reports;

import org.jhades.model.ClasspathResource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jhades.json.Json;
import org.jhades.model.ClasspathResourceVersion;
import org.jhades.model.JarPair;
import org.jhades.service.ClasspathScanner;
import org.jhades.utils.StdOutLogger;

/**
 *
 * Takes a WAR file, unzip's it and runs the jHades duplicates report on it.
 *
 *
 */
public class WarReportScanner extends WarScannerTemplate {

    private static final Pattern FILE_NAME = Pattern.compile("^/(.*)/([^/]+)\\.class$");
    private static final Pattern JAR_NAME = Pattern.compile("^.*/([^/]+\\.jar)$");
    private static final StdOutLogger logger = StdOutLogger.getLogger();
    private ClasspathScanner scanner = new ClasspathScanner();
    // for the moment this functionality is permanently turned off
    private boolean isReportClassFileDuplicatesOn = false;

    public WarReportScanner(String warFilePath, String tmpPath) {
        super(warFilePath, tmpPath);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {

        if (args.length != 2) {
            printUsage();
            System.exit(-1);
        }

        logger.setDebug(false);

        String warFilePath = args[0];
        String tmpPath = args[1];
        logger.info("warFilePath = " + warFilePath);
        logger.info("tmpPath = " + tmpPath);

        WarReportScanner warScanner = new WarReportScanner(warFilePath, tmpPath);

        warScanner.scan();

    }

    public static void printUsage() {
        System.out.println("\njHades war scanner utility - the following arguments are needed:\n");
        System.out.println("    warFilePath - the path to your war file");
        System.out.println("    tmpPath - the path to a temporary directory, needed to unzip files");

    }

    @Override
    protected void processClasspathResources(List<ClasspathResource> classpathResources) {

        updateStatus("Searching for overlaping jars");
        List<JarPair> overlapReportLines = scanner.findOverlappingJars(classpathResources, false);

        for (JarPair overlapPair : overlapReportLines) {
            Json jsonObject = new Json();
            jsonObject.setProperty("jar1", overlapPair.getJar1().getUrl());
            jsonObject.setProperty("jar2", overlapPair.getJar2().getUrl());
            jsonObject.setProperty("dupsTotal", overlapPair.getDupClassesTotal().toString());
            String json = jsonObject.stringify();
            System.out.println("#OVERLAP_JARS# " + json);
        }

        System.out.println("#SUMMARY_FINISHED#");

        if (isReportClassFileDuplicatesOn) {
            updateStatus("Searching for class file duplicates");
            List<ClasspathResource> classFilesWithDuplicates = scanner.findClassFileDuplicates(classpathResources, false);

            // class file duplicates report
            for (ClasspathResource classFile : classFilesWithDuplicates) {
                Matcher matcher = FILE_NAME.matcher(classFile.getName());
                if (matcher.matches()) {
                    String packageName = matcher.group(1);
                    String className = matcher.group(2);

                    if (packageName != null) {
                        packageName = packageName.replaceAll("/", "\\.");
                    }

                    Json jsonObject = new Json();
                    jsonObject.setProperty("packageName", packageName);
                    jsonObject.setProperty("className", className);
                    jsonObject.setProperty("numberOfVersions", "" + classFile.getNumberOfVersions());
                    String json = jsonObject.stringify();
                    System.out.println("#DUPLICATE_CLASS# " + json);
                } else {
                    logger.error("could not process " + classFile.getName());
                }
            }

            for (ClasspathResource classFile : classFilesWithDuplicates) {
                for (ClasspathResourceVersion resourceVersion : classFile.getResourceFileVersions()) {
                    String fileFullPathName = classFile.getName();
                    String classpathEntry = resourceVersion.getClasspathEntry().getUrl();
                    if (classpathEntry != null) {
                        Matcher matcher = JAR_NAME.matcher(classpathEntry);
                        if (matcher.matches()) {
                            classpathEntry = matcher.group(1);
                        }
                    }
                    long size = resourceVersion.getFileSize();
                    Json json = new Json();
                    json.setProperty("size", Long.toString(size));
                    json.setProperty("file", fileFullPathName);
                    json.setProperty("entry", classpathEntry);
                    System.out.println("#DETAIL# " + json.stringify());
                }
            }

        }
    }
}
