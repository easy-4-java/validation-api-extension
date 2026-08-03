package io.github.easy4j.validation.utils;

import java.util.concurrent.ConcurrentMap;

/**
 * 历史正则工具共享的有界缓存写入策略。
 *
 * <p>正则表达式可能来自外部配置，不能让静态缓存无限增长。缓存满时清空并继续按需编译，
 * 保持原有工具类的公开 API 与并发读取行为不变。</p>
 */
final class RegexpPatternCache {

    static final int MAX_ENTRIES = 256;

    private RegexpPatternCache() {
    }

    static <T> T cache(ConcurrentMap<String, T> cache, String cacheKey, T candidate) {
        if (cache.size() >= MAX_ENTRIES) {
            cache.clear();
        }
        T existing = cache.putIfAbsent(cacheKey, candidate);
        return existing == null ? candidate : existing;
    }
}
