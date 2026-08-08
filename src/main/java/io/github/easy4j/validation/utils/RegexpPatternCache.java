package io.github.easy4j.validation.utils;

import java.util.concurrent.ConcurrentMap;

/**
 * Bounded cache write strategy shared by all regex utility classes in this package.
 *
 * <p>Regular expressions may originate from external configuration, so the static caches
 * must not grow without bound.  When the cache reaches {@link #MAX_ENTRIES} it is cleared
 * and patterns are recompiled on demand, preserving the original public API and concurrent
 * read behaviour of the utility classes.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
final class RegexpPatternCache {

    static final int MAX_ENTRIES = 256;

    private RegexpPatternCache() {
    }

    /**
     * Inserts the candidate into the cache, clearing the cache first if it has reached
     * the maximum size.
     *
     * @param cache     the concurrent cache map
     * @param cacheKey  the cache key
     * @param candidate the value to insert
     * @param <T>       the value type
     * @return the value that ends up in the cache (either the existing entry or the candidate)
     */
    static <T> T cache(ConcurrentMap<String, T> cache, String cacheKey, T candidate) {
        if (cache.size() >= MAX_ENTRIES) {
            cache.clear();
        }
        T existing = cache.putIfAbsent(cacheKey, candidate);
        return existing == null ? candidate : existing;
    }
}
