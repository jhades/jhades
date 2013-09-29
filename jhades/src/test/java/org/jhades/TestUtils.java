package org.jhades;

import java.util.List;

/**
 *
 * Commonly used test utilities.
 *
 */
public class TestUtils {

    public static <T> void assertListContains(String error, T search, List<T> list) {
        if (!list.contains(search)) {
            throw new AssertionError(error);
        }
    }
}
