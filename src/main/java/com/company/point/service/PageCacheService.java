package com.company.point.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for caching page fragment combinations.
 * This reduces rendering time for frequently accessed pages.
 */
@Service
public class PageCacheService {

	private final ConcurrentHashMap<String, CachedPage> cache = new ConcurrentHashMap<>();
	private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

	/**
	 * Cached page data structure.
	 */
	private static class CachedPage {
		final List<String> blocks;
		final long timestamp;

		CachedPage(List<String> blocks) {
			this.blocks = blocks;
			this.timestamp = System.currentTimeMillis();
		}

		boolean isExpired() {
			return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
		}
	}

	/**
	 * Get cached page blocks if available and not expired.
	 *
	 * @param pageName the page name
	 * @return list of blocks, or null if not cached or expired
	 */
	public List<String> getCachedBlocks(String pageName) {
		CachedPage cachedPage = cache.get(pageName);

		if (cachedPage == null) {
			return null;
		}

		if (cachedPage.isExpired()) {
			cache.remove(pageName);
			return null;
		}

		return cachedPage.blocks;
	}

	/**
	 * Cache page blocks for a given page name.
	 *
	 * @param pageName the page name
	 * @param blocks   the list of fragment blocks
	 */
	public void cacheBlocks(String pageName, List<String> blocks) {
		cache.put(pageName, new CachedPage(blocks));
	}

	/**
	 * Clear all cached pages.
	 */
	public void clearCache() {
		cache.clear();
	}

	/**
	 * Clear cache for a specific page.
	 *
	 * @param pageName the page name to clear from cache
	 */
	public void clearPageCache(String pageName) {
		cache.remove(pageName);
	}

	/**
	 * Get cache statistics.
	 *
	 * @return string with cache statistics
	 */
	public String getCacheStats() {
		long validEntries = cache.values().stream().filter(page -> !page.isExpired()).count();

		return String.format("Cache size: %d (valid: %d, expired: %d)", cache.size(), validEntries, cache.size() - validEntries);
	}

	/**
	 * Remove expired entries from cache.
	 */
	public void cleanupExpired() {
		cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
	}
}
