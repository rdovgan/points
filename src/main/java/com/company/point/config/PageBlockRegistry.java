package com.company.point.config;

import java.util.Arrays;
import java.util.List;

/**
 * Registry that maps page names to their required JSP fragment blocks.
 * Each page is composed of multiple fragments that are assembled at runtime.
 */
public enum PageBlockRegistry {

	HOME("home", Arrays.asList("fragments/header", "fragments/content", "fragments/offers", "fragments/footer")),

	PRODUCT("product", Arrays.asList("fragments/header", "fragments/sidebar", "fragments/productDetails", "fragments/reviews", "fragments/footer")),

	ABOUT("about", Arrays.asList("fragments/header", "fragments/sidebar", "fragments/footer")),

	CONTACT("contact", Arrays.asList("fragments/header", "fragments/footer"));

	private final String pageName;
	private final List<String> blocks;

	PageBlockRegistry(String pageName, List<String> blocks) {
		this.pageName = pageName;
		this.blocks = blocks;
	}

	public String getPageName() {
		return pageName;
	}

	public List<String> getBlocks() {
		return blocks;
	}

	/**
	 * Find page configuration by page name.
	 *
	 * @param pageName the name of the page
	 * @return the PageBlockRegistry entry for the page, or null if not found
	 */
	public static PageBlockRegistry findByPageName(String pageName) {
		for (PageBlockRegistry page : values()) {
			if (page.getPageName().equalsIgnoreCase(pageName)) {
				return page;
			}
		}
		return null;
	}

	/**
	 * Check if a page name is registered.
	 *
	 * @param pageName the name of the page
	 * @return true if the page is registered, false otherwise
	 */
	public static boolean isPageRegistered(String pageName) {
		return findByPageName(pageName) != null;
	}
}
