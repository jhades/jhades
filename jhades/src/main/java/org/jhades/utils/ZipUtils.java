package org.jhades.utils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Utility methods for handling zip files.
 *
 */
public final class ZipUtils {

    private static final StdOutLogger logger = StdOutLogger.getLogger();

    public interface UnzipProgressListener {

        void onBeginFileExtract(String fileName);
    }

    private ZipUtils() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    /**
     *
     * Unzips a file to a given directory
     *
     * @param zipFilename - the zip file to unzip
     * @param destDirname - the destination directory
     * @throws IOException
     */
    public static void unzip(String zipFilename, String destDirname) throws IOException {
        unzip(zipFilename, destDirname);
    }

    public static void unzip(String zipFilename, String destDirname, final UnzipProgressListener progressListener)
            throws IOException {

        final Path destDir = Paths.get(destDirname);
        //if the destination doesn't exist, create it
        if (Files.notExists(destDir)) {
            logger.debug(destDir + " does not exist. Creating...");
            Files.createDirectories(destDir);
        }

        try (FileSystem zipFileSystem = createZipFileSystem(zipFilename, false)) {
            final Path root = zipFileSystem.getPath("/");

            //walk the zip file tree and copy files to the destination
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                    if (progressListener != null) {
                        progressListener.onBeginFileExtract(file.toString());

                    }
                    final Path destFile = Paths.get(destDir.toString(),
                            file.toString());
                    logger.debug("Extracting file " + file + " to " + destFile + "\n");
                    Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) throws IOException {
                    final Path dirToCreate = Paths.get(destDir.toString(),
                            dir.toString());
                    if (Files.notExists(dirToCreate)) {
                        logger.debug("Creating directory " + dirToCreate + "\n");
                        Files.createDirectory(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Returns a zip file system
     *
     * @param zipFilename to construct the file system from
     * @param create true if the zip file should be created
     * @return a zip file system
     * @throws IOException
     */
    private static FileSystem createZipFileSystem(String zipFilename,
            boolean create)
            throws IOException {
        // convert the filename to a URI
        final Path path = Paths.get(zipFilename);
        final URI uri = URI.create("jar:file:" + path.toUri().getPath());

        final Map<String, String> env = new HashMap<>();
        if (create) {
            env.put("create", "true");
        }
        return FileSystems.newFileSystem(uri, env);
    }
}
