package org.jhades.reports;

import org.jhades.model.ClasspathEntry;
import org.jhades.model.ClasspathResource;
import org.jhades.utils.FileUtils;
import org.jhades.utils.ZipUtils;
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
import org.jhades.json.Json;
import org.jhades.model.ClasspathEntries;
import org.jhades.service.ClasspathScannerListener;
import org.jhades.utils.StdOutLogger;

public abstract class WarScannerTemplate {

    private static final StdOutLogger logger = StdOutLogger.getLogger();
    private static final Pattern JAR_NAME = Pattern.compile("^.*/(.*jar)$");
    private static final String SEP = System.getProperty("file.separator");
    private final String tmpPath;
    private final String warFilePath;
    private Json status = new Json();

    public WarScannerTemplate(String warFilePath, String tmpPath) {
        this.warFilePath = warFilePath;
        this.tmpPath = tmpPath;
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

    protected abstract void processClasspathResources(List<ClasspathResource> classpathResources);

    public String getWarFilePath() {
        return warFilePath;
    }

    public String getTmpPath() {
        return tmpPath;
    }

    protected void updateStatus(String statusUpdate) {
        status.setProperty("statusUpdate", statusUpdate);
        System.out.println("#STATUS_UPDATE# " + status.stringify());
    }
}
