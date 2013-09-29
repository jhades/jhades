package org.jhades.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * Model class for a java class loader.
 *
 * This class should be subclassed in order to provide support for different types of classloaders.
 *
 */
public abstract class ClazzLoader {

    private String name;
    private String details;
    private boolean isSupported;
    private List<ClasspathEntry> classpathEntries = new ArrayList<>();

    public ClazzLoader(String name, String details, boolean isSupported) {
        this.name = name;
        this.details = details;
        this.isSupported = isSupported;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public boolean isSupported() {
        return isSupported;
    }

    public List<ClasspathEntry> getClasspathEntries() {
        return new ArrayList<>(classpathEntries);
    }

    protected void addClasspathEntry(ClasspathEntry newEntry) {
        classpathEntries.add(newEntry);
        if (newEntry.isJar()) {
            List<ClasspathEntry> manifestClasspath = newEntry.findManifestClasspathEntries();
            for (ClasspathEntry manifestEntry : manifestClasspath) {
                addClasspathEntry(manifestEntry);
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final ClazzLoader other = (ClazzLoader) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.details, other.details)) {
            return false;
        }
        if (this.isSupported != other.isSupported) {
            return false;
        }
        return true;
    }
}
