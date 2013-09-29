package org.jhades.model;

import java.net.URLClassLoader;
import static org.jhades.service.ClasspathScanner.BOOTSTRAP_CLASS_LOADER;
import sun.misc.Launcher;
import sun.misc.URLClassPath;

/**
 *
 * Factory class that identifies a class loader and builds the corresponding object model.
 *
 * Class loaders should be detected via reflection to avoid code dependencies towards specific implementations.
 *
 * For the moment only the Url classloader is supported, this already covers jetty, tomcat, jboss and standalone
 * applications.
 *
 */
public class ClazzLoaderFactory {

    public static ClazzLoader createClazzLoader(ClassLoader classLoader) {
        ClazzLoader cl = null;
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            if (urlClassLoader.getURLs() != null) {
                cl = new UrlClazzLoader(classLoader.getClass().getName(), classLoader.toString(), urlClassLoader.getURLs());
            }
        } else {
            System.out.println("WARNING: this classloader is not supported: " + classLoader.getClass().getName());
        }
        return cl;
    }

    public static ClazzLoader createBootstrapClassLoader() {
        URLClassPath cp = Launcher.getBootstrapClassPath();
        return new UrlClazzLoader(BOOTSTRAP_CLASS_LOADER, "N/A", cp.getURLs());
    }
}
