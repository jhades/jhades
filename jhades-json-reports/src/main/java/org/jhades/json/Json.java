package org.jhades.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * Simple JSON serializer - allows to generate json based on a single level map of string keys and values.
 *
 *
 */
public class Json {

    private Map<String, String> content = new HashMap<>();

    public void setProperty(String propertyName, String propertyValue) {
        content.put(propertyName, propertyValue);
    }

    public String stringify() {
        String json = "";
        int totalEntries = content.size();
        int counter = 1;
        for (Entry<String, String> entry : content.entrySet()) {
            if (counter == 1) {
                json += "{";
            } else {
                json += ",";
            }
            json += "\"" + entry.getKey() + "\" : \"" + entry.getValue() + "\" ";

            if (counter == totalEntries) {
                json += "}";
            }
            counter++;
        }
        return json;
    }
}
