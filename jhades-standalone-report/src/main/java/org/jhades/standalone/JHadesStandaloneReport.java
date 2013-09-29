package org.jhades.standalone;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jhades.model.ClasspathEntries;
import org.jhades.model.ClasspathEntry;
import org.jhades.model.ClasspathResource;
import org.jhades.model.ClasspathResourceVersion;
import org.jhades.model.JarPair;
import org.jhades.reports.DuplicatesReport;
import org.jhades.service.ClasspathScanner;
import org.jhades.service.ClasspathScannerListener;
import org.jhades.utils.FileUtils;
import org.jhades.utils.StdOutLogger;
import org.jhades.utils.ZipUtils;

public class JHadesStandaloneReport {

    private static final StdOutLogger logger = StdOutLogger.getLogger();
    private ClasspathScanner scanner = new ClasspathScanner();
    private static final Pattern JAR_NAME = Pattern.compile("^.*/(.*jar)$");
    private static final String SEP = System.getProperty("file.separator");
    private final String warFilePath;
    private final String tmpPath;

    public JHadesStandaloneReport(String warFilePath, String tmpPath) {
        this.warFilePath = warFilePath;
        this.tmpPath = tmpPath;
    }

    public static void printUsage() {
        System.out.println("\njHades standalone war scanner utility - the following arguments are needed:\n");
        System.out.println("    warFilePath - the path to your war file");
        System.out.println("    tmpPath (optional) - the path to a temporary directory, needed to unzip files");
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        System.out.println("    -Ddetail=true -> displays classes with duplicates and their locations");
        System.out.println("    -Dexclude.same.size.dups=true -> don't count as classpath duplicates the classes that have multiple class files, but they all have the same size");
        System.out.println("    -Dsearch.by.file.name=\"<search regex>\" -> searches the WAR for a resource file using a Java regular expression");
        System.out.println();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length == 0 || args.length > 2) {
            printUsage();
            System.exit(-1);
        }

        logger.setDebug(false);

        String warFilePath = args[0];

        String tmpPath;
        if (args.length == 2) {
            tmpPath = args[1];
        } else {
            tmpPath = System.getProperty("java.io.tmpdir") + "/jhades";
            Files.createDirectories(Paths.get(tmpPath));
        }

        logger.info("warFilePath = " + warFilePath);
        logger.info("tmpPath = " + tmpPath);

        JHadesStandaloneReport warScanner = new JHadesStandaloneReport(warFilePath, tmpPath);

        warScanner.scan();

    }

    public void scan() throws IOException, URISyntaxException {
        logger.debug("Extracting war " + warFilePath + "...");

        updateStatus("Deleting temporary directory");
        FileUtils.deleteDirectory(tmpPath);

        updateStatus("Unziping WAR");
        ZipUtils.unzip(warFilePath, tmpPath, new ZipUtils.UnzipProgressListener() {
            @Override
            public void onBeginFileExtract(String fileName) {
                Matcher matcher = JAR_NAME.matcher(fileName);
                if (matcher.matches()) {
                    updateStatus("Extracting jar " + matcher.group(1));
                }
            }
        });

        final List<ClasspathEntry> classpathEntries = new ArrayList<>();

        // add classes folder
        String classesFolderPath = tmpPath + SEP + "WEB-INF" + SEP + "classes";
        Path classesFolder = Paths.get(classesFolderPath);
        if (Files.exists(classesFolder)) {
            classpathEntries.add(new ClasspathEntry(null, classesFolderPath));
        }

        Path start = Paths.get(tmpPath);

        updateStatus("Scanning WAR");

        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String filePath = file.toString();
                if (filePath.contains("WEB-INF" + SEP + "lib") && filePath.endsWith(".jar")) {
                    Matcher matcher = JAR_NAME.matcher(filePath);
                    if (matcher.matches()) {
                        updateStatus("Processing jar " + matcher.group(1));
                    }
                    logger.debug("Adding jar: " + filePath);

                    filePath = "file:///" + filePath.replaceAll("\\\\", "/");
                    logger.debug("jar URL: " + filePath);
                    classpathEntries.add(new ClasspathEntry(null, filePath));
                }
                return CONTINUE;
            }
        });

        ClasspathScannerListener listener = (new ClasspathScannerListener() {
            @Override
            public void onEntryScanStart(ClasspathEntry entry) {
                String filePath = entry.getUrl().toString();
                Matcher matcher = JAR_NAME.matcher(filePath);
                if (matcher.matches()) {
                    updateStatus("Processing jar " + matcher.group(1));
                }
            }

            @Override
            public void onEntryScanEnd(ClasspathEntry entry) {
                String filePath = entry.getUrl().toString();
                Matcher matcher = JAR_NAME.matcher(filePath);
                if (matcher.matches()) {
                    updateStatus("Finished processing jar " + matcher.group(1));
                }
            }
        });

        List<ClasspathResource> classpathResources = ClasspathEntries.findClasspathResourcesInEntries(classpathEntries, logger, listener);

        processClasspathResources(classpathResources);
    }

    private void processClasspathResources(List<ClasspathResource> classpathResources) {

        boolean isDetailedMode = "true".equals(System.getProperty("detail"));
        boolean isExcludeSameSizeDups = "true".equals(System.getProperty("exclude.same.size.dups"));

        List<JarPair> overlapReportLines = scanner.findOverlappingJars(classpathResources, isExcludeSameSizeDups);

        long totalDupClasses = 0;

        System.out.println("\n>>>> Jar overlap report: \n");

        for (JarPair jarOverlapReportLine : overlapReportLines) {
            String reportLine = getJarName(jarOverlapReportLine.getJar1().getUrl()) + " overlaps with "
                    + getJarName(jarOverlapReportLine.getJar2().getUrl())
                    + " - total overlapping classes: " + jarOverlapReportLine.getDupClassesTotal();
            System.out.println(reportLine);
            totalDupClasses += jarOverlapReportLine.getDupClassesTotal();
        }

        System.out.println("\nTotal number of classes with more than one version: " + totalDupClasses + "\n");

        if (!isExcludeSameSizeDups) {
            System.out.println("\nUse -Dexclude.same.size.dups=true for considering as a duplicate only classes with multiple class files of different sizes.\n");
        }


        if (isDetailedMode) {
            List<ClasspathResource> resourcesWithDifferentSizeDups = scanner.findClassFileDuplicates(classpathResources, isExcludeSameSizeDups);
            DuplicatesReport report = new DuplicatesReport(resourcesWithDifferentSizeDups, new StandaloneReportUrlFormatter());
            report.print();
        }

        String searchByFileName = System.getProperty("search.by.file.name");

        if (searchByFileName != null) {
            List<ClasspathResource> searchResults = scanner.findByRegex(searchByFileName);
            if (searchResults != null && !searchResults.isEmpty()) {
                System.out.println("\nSearch results using regular expression: " + searchByFileName + "\n");
                for (ClasspathResource match : searchResults) {
                    System.out.println(match.getName() + "\n");
                    for (ClasspathResourceVersion version : match.getResourceFileVersions()) {
                        System.out.println("    " + version.getClasspathEntry().getUrl());
                    }
                    System.out.println("");
                }
            }
        }

    }

    private String getJarName(String url) {
        String jarName = "";
        if (url != null) {
            Matcher matcher = JAR_NAME.matcher(url);
            if (matcher.matches()) {
                jarName = matcher.group(1);
            }
        }

        return jarName;
    }

    protected void updateStatus(String statusUpdate) {
        System.out.println(statusUpdate);
    }
}
