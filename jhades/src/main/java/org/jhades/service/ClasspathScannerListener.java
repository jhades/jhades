package org.jhades.service;

import org.jhades.model.ClasspathEntry;

/**
 *
 * Listener for the classpath scanning process.
 *
 */
public interface ClasspathScannerListener {

    /**
     *
     * Called when the scanning of a classpath entry begins
     *
     */
    void onEntryScanStart(ClasspathEntry classpathEntry);

    /**
     *
     * Called when the scanning of a classpath entry ends
     *
     */
    void onEntryScanEnd(ClasspathEntry classpathEntry);
}
