package org.jhades.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * A classpath resource is a file on the classpath: such as a class file or an xml file.
 *
 * One classpath resource can have multiple versions.
 *
 * @see ClasspathResourceVersion
 *
 */
public class ClasspathResource {

    private final String name;
    private final List<ClasspathResourceVersion> resourceFileVersions = new ArrayList<>();

    public ClasspathResource(String name, ClasspathResourceVersion resourceFileVersion) {
        this.name = name;
        this.resourceFileVersions.add(resourceFileVersion);
    }

    public String getName() {
        return name;
    }

    public List<ClasspathResourceVersion> getResourceFileVersions() {
        return resourceFileVersions;
    }

    public int getNumberOfVersions() {
        return resourceFileVersions.size();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.resourceFileVersions);
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
        final ClasspathResource other = (ClasspathResource) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.resourceFileVersions, other.resourceFileVersions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Clazz{" + "name=" + name + ", classFileVersions=" + resourceFileVersions + '}';
    }

    public boolean hasDuplicates(boolean excludeSameSizeDups) {
        boolean hasDuplicates = false;

        if (!excludeSameSizeDups) {
            hasDuplicates = resourceFileVersions.size() > 1;
        } else if (resourceFileVersions.size() > 1) {
            // exclude resource files for which all versions have the same size
            boolean multipleSizesExist = false;
            long size = resourceFileVersions.get(0).getFileSize();
            for (ClasspathResourceVersion resourceFileVersion : resourceFileVersions) {
                if (resourceFileVersion.getFileSize() != size) {
                    multipleSizesExist = true;
                    break;
                }
            }
            hasDuplicates = multipleSizesExist;
        }

        return hasDuplicates;
    }
}
