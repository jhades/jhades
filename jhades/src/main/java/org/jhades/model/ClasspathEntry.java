package org.jhades.model;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.jhades.utils.StdOutLogger;

/**
 *
 * A class path entry - It's either a class folder or a jar.
 *
 */
public class ClasspathEntry {

    private static final StdOutLogger logger = StdOutLogger.getLogger();
    private final ClazzLoader classLoader;
    private final String url;
    private List<ClasspathResourceVersion> resourceVersions = new ArrayList<>();
    private boolean lazyLoadDone = false;

    public ClasspathEntry(ClazzLoader classLoader, String url) {
        this.classLoader = classLoader;
        this.url = url;
    }

    public ClazzLoader getClassLoader() {
        return classLoader;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClasspathEntry other = (ClasspathEntry) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "classLoader=" + classLoader + ", path=" + url;
    }

    public boolean isJar() {
        return url != null && url.endsWith(".jar");
    }

    public boolean isClassFolder() {
        return url != null && url.endsWith("/");
    }

    public String getClassLoaderName() {
        return classLoader != null ? classLoader.getName() : null;
    }

    /**
     * The contents of a jar are only loaded if accessed the first time.
     *
     */
    public List<ClasspathResourceVersion> getResourceVersions() throws URISyntaxException, IOException {
        if (!lazyLoadDone) {
            if (isClassFolder()) {
                logger.debug("\nScanning class folder: " + getUrl());

                URI uri = new URI(getUrl());
                Path start = Paths.get(uri);

                scanClasspathEntry(start);

            } else if (isJar()) {
                logger.debug("\nScanning jar: " + getUrl());

                URI uri = new URI("jar:" + getUrl());

                try (FileSystem jarFS = FileSystems.newFileSystem(uri, new HashMap<String, String>())) {
                    Path zipInJarPath = jarFS.getPath("/");

                    scanClasspathEntry(zipInJarPath);

                } catch (Exception exc) {
                    logger.debug("Could not scan jar: " + getUrl() + " - reason:" + exc.getMessage());
                }
            }
            lazyLoadDone = true;
        }

        return resourceVersions;
    }

    /**
     *
     * @return the list of classpath entries (jars, folders) linked to this jar via it's manifest Class-Path property
     *
     */
    public List<ClasspathEntry> findManifestClasspathEntries() {
        List<ClasspathEntry> manifestClasspathEntries = new ArrayList<>();
        // scan for Class-Path entries on MANIFEST.MF
        if (isJar()) {
            JarInputStream jarStream;
            try {
                jarStream = new JarInputStream(new URL(getUrl()).openStream());
                Manifest manifest = jarStream.getManifest();
                if (manifest != null) {
                    Attributes attrs = manifest.getMainAttributes();
                    if (attrs != null) {
                        String manifestClasspath = attrs.getValue("Class-Path");
                        if (manifestClasspath != null) {
                            String[] jarPaths = manifestClasspath.split(" ");
                            if (jarPaths != null) {
                                for (String jarPath : jarPaths) {
                                    logger.debug("Manifest jar path: " + jarPath);
                                    manifestClasspathEntries.add(new ClasspathEntry(classLoader, jarPath));
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                logger.warn("Problem scanning Manifest classpath: " + ex.getMessage());
            }
        }
        return manifestClasspathEntries;
    }

    private List<ClasspathResourceVersion> scanClasspathEntry(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path classpathResourceFile, BasicFileAttributes attrs) throws IOException {
                String resourceName = classpathResourceFile.toString();
                logger.debug(getUrl() + " -" + resourceName);
                ClasspathResourceVersion classFileVersion = new ClasspathResourceVersion(ClasspathEntry.this, resourceName, attrs.size());
                resourceVersions.add(classFileVersion);
                return CONTINUE;
            }
        });
        return resourceVersions;
    }
}
