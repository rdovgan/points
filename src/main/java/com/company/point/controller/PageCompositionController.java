package com.company.point.controller;

import com.company.point.config.PageBlockRegistry;
import com.company.point.service.PageCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller responsible for composing pages from JSP fragments.
 * It takes a page name and assembles the appropriate fragments into a complete page.
 */
@Controller
@RequestMapping("/page")
public class PageCompositionController {

	private static final Logger logger = LoggerFactory.getLogger(PageCompositionController.class);

	@Autowired
	private PageCacheService cacheService;

	/**
	 * Compose a page by assembling its JSP fragments based on the page name.
	 *
	 * @param pageName the name of the page to compose
	 * @param model    the Spring MVC model to pass data to the view
	 * @return the view name (layout template)
	 */
	@GetMapping("/{pageName}")
	public String composePage(@PathVariable String pageName, Model model) {
		logger.debug("Composing page: {}", pageName);

		// Try to get blocks from cache first
		List<String> blocks = cacheService.getCachedBlocks(pageName);
		boolean cacheHit = blocks != null;

		if (!cacheHit) {
			logger.debug("Cache miss for page: {}", pageName);

			// Find the page configuration in the registry
			PageBlockRegistry pageConfig = PageBlockRegistry.findByPageName(pageName);

			// If page not found, return error page
			if (pageConfig == null) {
				logger.warn("Page not found: {}", pageName);
				model.addAttribute("errorMessage", "Page not found: " + pageName);
				model.addAttribute("pageTitle", "404 - Page Not Found");
				return "error";
			}

			// Get the list of JSP fragments for this page
			blocks = pageConfig.getBlocks();

			// Cache the blocks for future requests
			cacheService.cacheBlocks(pageName, blocks);
			logger.debug("Cached blocks for page: {}", pageName);
		} else {
			logger.debug("Cache hit for page: {}", pageName);
		}

		// Add blocks and page metadata to the model
		model.addAttribute("blocks", blocks);
		model.addAttribute("pageTitle", formatPageTitle(pageName));
		model.addAttribute("pageName", pageName);

		// Add any page-specific data
		addPageSpecificData(pageName, model);

		// Return the layout template which will include all fragments
		return "layouts/layout";
	}

	/**
	 * Root URL redirects to home page.
	 */
	@GetMapping("/")
	public String home() {
		return "redirect:/page/home";
	}

	/**
	 * Format page name for display as title.
	 *
	 * @param pageName the page name
	 * @return formatted title
	 */
	private String formatPageTitle(String pageName) {
		return pageName.substring(0, 1).toUpperCase() + pageName.substring(1) + " - Local Experiences";
	}

	/**
	 * Add page-specific data to the model based on the page name.
	 * This can be extended to load data from services/repositories.
	 *
	 * @param pageName the page name
	 * @param model    the model to add data to
	 */
	private void addPageSpecificData(String pageName, Model model) {
		switch (pageName.toLowerCase()) {
		case "product":
			// Add sample product data
			model.addAttribute("productName", "Historic Walking Tour");
			model.addAttribute("productPrice", "$49.00");
			model.addAttribute("productRating", 4.8);
			break;
		case "home":
			// Add home page specific data
			model.addAttribute("featuredCount", 3);
			break;
		case "about":
			// Add about page specific data
			model.addAttribute("companyName", "Local Experiences");
			break;
		case "contact":
			// Add contact page specific data
			model.addAttribute("contactEmail", "info@localexperiences.com");
			break;
		}
	}
}
