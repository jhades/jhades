package org.jhades.model;

import java.util.Objects;

/**
 *
 * Model class for a overlapping pair of jars.
 *
 */
public class JarPair {

    private ClasspathEntry jar1;
    private ClasspathEntry jar2;
    private long dupClassesTotal;

    public JarPair(ClasspathEntry jar1, ClasspathEntry jar2) {
        this.jar1 = jar1;
        this.jar2 = jar2;
    }

    public ClasspathEntry getJar1() {
        return jar1;
    }

    public ClasspathEntry getJar2() {
        return jar2;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.jar1);
        hash = 37 * hash + Objects.hashCode(this.jar2);
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
        final JarPair other = (JarPair) obj;
        if (this.jar1.equals(other.jar1) && this.jar2.equals(other.jar2)
                || this.jar1.equals(other.jar2) && this.jar2.equals(other.jar1)) {
            return true;
        }
        return false;
    }

    public void incrementDupClassesTotal() {
        dupClassesTotal++;
    }

    public Long getDupClassesTotal() {
        return dupClassesTotal;
    }
}
