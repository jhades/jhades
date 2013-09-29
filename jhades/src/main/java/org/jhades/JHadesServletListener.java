package org.jhades;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * Classpath scanning servlet listener. Allows using JHades to debug web applications.
 *
 * This servlet listener (or a subclass) should be the first configured in web.xml
 *
 * The default behaviour is to print to the server log information that is normally needed to debug classpath problems:
 * classloader chain, jar locations, overlapping jars, etc.
 *
 */
public class JHadesServletListener implements ServletContextListener {

    private JHades console = new JHades();

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        try {
            runJHades(console);
        } catch (Exception exc) {
            System.out.println("JHades - error occurred: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    protected void runJHades(JHades console) {
        console.overlappingJarsReport()
                .printClassLoaderNames()
                .dumpClassloaderInfo()
                .printClasspath()
                .multipleClassVersionsReport();
    }
}
