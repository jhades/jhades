package org.jhades.reports;

/**
 *
 * Default implementation for Url formatting - this implementation returns the Url unformatted.
 *
 */
public class DefaultUrlFormatterImpl implements UrlFormatter {

    @Override
    public String formatUrl(String url) {
        return url;
    }
}
