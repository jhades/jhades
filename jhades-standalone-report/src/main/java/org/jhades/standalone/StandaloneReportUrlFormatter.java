package org.jhades.standalone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jhades.reports.UrlFormatter;

public class StandaloneReportUrlFormatter implements UrlFormatter {

    private static final Pattern JAR_NAME = Pattern.compile("^.*/(.*jar)$");

    @Override
    public String formatUrl(String url) {
        Matcher matcher = JAR_NAME.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return url;
        }
    }
}
