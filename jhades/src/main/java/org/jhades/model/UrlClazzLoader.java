package org.jhades.model;

import java.net.URL;

/**
 *
 * Provides support for Url classLoaders.
 *
 */
public class UrlClazzLoader extends ClazzLoader {

    public UrlClazzLoader(String name, String details, URL[] urls) {
        super(name, details, true);
        for (URL url : urls) {
            if (url != null) {
                addClasspathEntry(new ClasspathEntry(this, url.toString()));
            }
        }
    }
}
