package org.jhades.utils;

/**
 *
 * A logger to write results to stdout. No logging libraries such as slf4j, log4j, logback, etc. where used on jHades -
 * this is intentional.
 *
 * This is because logging libraries are an ocasional source of classpath problems on development environments, and
 * using this class allows JHades to be of help on those situations as well.
 *
 */
public class StdOutLogger {

    private static StdOutLogger instance;
    private boolean debug = false;

    public void debug(String msg) {
        if (debug) {
            System.out.println(msg);
        }
    }

    public void warn(String msg) {
        if (debug) {
            System.out.println("WARN - " + msg);
        }
    }

    public void error(String error) {
        System.out.println("\nERROR - " + error + "\n");
    }

    public static synchronized StdOutLogger getLogger() {
        if (instance == null) {
            instance = new StdOutLogger();
        }
        return instance;
    }

    public static void setDebug(boolean debug) {
        instance.debug = debug;
    }

    public void info(String info) {
        System.out.println(info + "\n");
    }

    public void error(String message, java.lang.Exception ex) {
        error(message);
        ex.printStackTrace();
    }
}
