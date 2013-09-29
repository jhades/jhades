package org.jhades.model;

import java.util.Objects;

/**
 *
 * A classpath resource can have several versions, for example a class file can have different versions on different
 * jars.
 *
 *
 */
public class ClasspathResourceVersion {

    private final ClasspathEntry classpathEntry;
    private String resourceName;
    private final long fileSize;

    public ClasspathResourceVersion(ClasspathEntry classpathEntry, String resourceName, long classSize) {
        this.classpathEntry = classpathEntry;
        this.resourceName = resourceName;
        this.fileSize = classSize;
    }

    public ClasspathEntry getClasspathEntry() {
        return classpathEntry;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getResourceName() {
        return resourceName;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.classpathEntry);
        hash = 97 * hash + Objects.hashCode(this.resourceName);
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
        final ClasspathResourceVersion other = (ClasspathResourceVersion) obj;
        if (!Objects.equals(this.classpathEntry, other.classpathEntry)) {
            return false;
        }
        if (!Objects.equals(this.resourceName, other.resourceName)) {
            return false;
        }
        return true;
    }
}
