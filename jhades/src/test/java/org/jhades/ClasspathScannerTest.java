package org.jhades;

import static org.jhades.TestUtils.*;
import java.net.URL;
import java.util.List;
import org.jhades.model.ClasspathEntry;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jhades.model.ClasspathResource;
import org.jhades.model.ClazzLoader;
import org.jhades.model.UrlClazzLoader;
import org.jhades.service.ClasspathScanner;

public class ClasspathScannerTest {

    private ClasspathScanner scanner = new ClasspathScanner();

    @Test
    public void testFindAllClassLoaders() {

        List<ClazzLoader> classLoaders = scanner.findAllClassLoaders();

        final int expectClassLoadersCount = 3;
        assertNotNull("list of classloaders cannot be null.", classLoaders);
        assertTrue("list of classloaders does not have the expected size of " + expectClassLoadersCount + ", actual size is: "
                + classLoaders.size() + " - " + classLoaders, classLoaders.size() == expectClassLoadersCount);
    }

    @Test
    public void findAllClasspathEntries() {

        List<ClasspathEntry> classpathEntries = scanner.findAllClasspathEntries();

        assertNotNull("list of classpath entries cannot be null.", classpathEntries);
        assertTrue("list of classpath entries cannot be empty.", classpathEntries.size() > 0);
    }

    @Test
    public void testFindAllResourceVersions() {
        String resourceName = "java/lang/String.class";

        List<URL> allVersions = scanner.findAllResourceVersions(resourceName);

        assertNotNull("the tested method should never return null.", allVersions);
        assertTrue("there must be at least a version of the String class", allVersions.size() > 0);
        assertTrue("This is not the expected class: " + allVersions.get(0), allVersions.get(0).toString().endsWith(resourceName));
    }

    @Test
    public void testFindCurrentResourceVersion() {
        String resourceName = "java/lang/String.class";

        URL currentVersion = scanner.findCurrentResourceVersion(resourceName);

        assertNotNull("there should be a version of the String class.", currentVersion);
        assertTrue("This is not the expected class: " + currentVersion, currentVersion.toString().endsWith(resourceName));
    }

    @Test
    public void testFindClass() {
        ClasspathResource found = scanner.findClass(String.class);

        assertNotNull("String class must exist on the classpath.", found);
        assertTrue("matches cannot be empty.", found.getResourceFileVersions().size() > 0);
        assertTrue("String class not found.", found.getName().contains("String"));
    }

    @Test
    public void testFindWithRegularExpression() {
        List<ClasspathResource> matches = scanner.findByRegex("java/lang/String.class");

        assertNotNull("matches cannot be null.", matches);
        assertTrue("matches size must be 1, but it's " + matches.size(), matches.size() == 1);
        assertTrue("java.lang.String class not found.", matches.get(0).getName().contains("String"));
    }

    @Test
    public void testFindAllResourcesWithDuplicates() {

        List<ClasspathResource> dups = scanner.findAllResourcesWithDuplicates(true);

        assertNotNull("dups cannot be null.", dups);
        assertTrue("dups cannot be empty.", dups.size() > 0);
    }
}
