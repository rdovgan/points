# Thymeleaf Page Assembler Documentation

## Overview

The Page Assembler is a server-side system that composes full HTML pages by combining multiple Thymeleaf fragments. This approach promotes reusability and maintainability by separating page components into modular pieces.

## Architecture

### 1. **Thymeleaf Fragment Structure**

Location: `/src/main/resources/templates/fragments/`

Available fragments:
- `header.html` - Site navigation and branding
- `footer.html` - Site footer with links and copyright
- `sidebar.html` - Categories and navigation sidebar
- `content.html` - Hero section for home page
- `offers.html` - Featured offerings grid
- `productDetails.html` - Product detail view
- `reviews.html` - Customer reviews section

### 2. **Page Block Registry**

Location: `src/main/java/com/company/point/config/PageBlockRegistry.java`

An enum that maps page names to their required Thymeleaf fragments:

```java
HOME("home", Arrays.asList(
    "fragments/header",
    "fragments/content",
    "fragments/offers",
    "fragments/footer"
))
```

**Registered Pages:**
- **home** - Header, Content (Hero), Offers, Footer
- **product** - Header, Sidebar, Product Details, Reviews, Footer
- **about** - Header, Sidebar, Footer
- **contact** - Header, Footer

### 3. **Base Layout Template**

Location: `/src/main/resources/templates/layouts/layout.html`

The master template that dynamically includes fragments based on the `blocks` model attribute:

```html
<div th:each="block : ${blocks}" th:replace="~{__${block}__ :: fragment}"></div>
```

Each fragment is defined with:
```html
<section th:fragment="fragment">
    <!-- Fragment content -->
</section>
```

### 4. **Page Composition Controller**

Location: `src/main/java/com/company/point/controller/PageCompositionController.java`

**Endpoint:** `GET /page/{pageName}`

**Flow:**
1. Checks cache for page blocks
2. If cache miss, looks up page configuration in registry
3. Caches the blocks for future requests
4. Adds blocks and metadata to model
5. Returns layout template which renders all fragments

### 5. **Caching Mechanism**

Location: `src/main/java/com/company/point/service/PageCacheService.java`

**Features:**
- In-memory cache using `ConcurrentHashMap`
- 5-minute TTL (Time To Live)
- Automatic expiration checking
- Cache statistics and cleanup methods

**Benefits:**
- Reduced lookup time for repeated pages
- Better performance for frequently accessed pages
- Configurable TTL

## Testing the Page Assembler

### Starting the Application

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Available URLs

Test these endpoints in your browser:

1. **Home Page**
   ```
   http://localhost:8080/page/home
   ```
   Displays: Header → Hero → Offers → Footer

2. **Product Page**
   ```
   http://localhost:8080/page/product
   ```
   Displays: Header → Sidebar → Product Details → Reviews → Footer

3. **About Page**
   ```
   http://localhost:8080/page/about
   ```
   Displays: Header → Sidebar → Footer

4. **Contact Page**
   ```
   http://localhost:8080/page/contact
   ```
   Displays: Header → Footer

5. **Root URL (redirects to home)**
   ```
   http://localhost:8080/
   ```

6. **Invalid Page (404 error)**
   ```
   http://localhost:8080/page/nonexistent
   ```
   Displays: Custom error page

### Verifying Caching

Check the application logs to see caching in action:

```
DEBUG c.c.p.controller.PageCompositionController : Cache miss for page: home
DEBUG c.c.p.controller.PageCompositionController : Cached blocks for page: home
```

Subsequent requests will show:
```
DEBUG c.c.p.controller.PageCompositionController : Cache hit for page: home
```

## Adding New Pages

### Step 1: Create New Fragments (if needed)

Create new HTML files in `/src/main/resources/templates/fragments/`:

```html
<section class="my-section" th:fragment="fragment">
    <!-- Your content here -->
    <h2 th:text="${dynamicContent}">Default Content</h2>
</section>
```

### Step 2: Register the Page

Add a new entry to `PageBlockRegistry.java`:

```java
MY_PAGE("mypage", Arrays.asList(
    "fragments/header",
    "fragments/myCustomFragment",
    "fragments/footer"
))
```

### Step 3: (Optional) Add Page-Specific Data

Update the `addPageSpecificData` method in `PageCompositionController.java`:

```java
case "mypage":
    model.addAttribute("customData", "value");
    break;
```

### Step 4: Test

Access: `http://localhost:8080/page/mypage`

## Configuration

### Thymeleaf Settings

In `application.properties`:

```properties
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.cache=false
spring.thymeleaf.mode=HTML
```

### Cache TTL

In `PageCacheService.java`, modify:

```java
private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
```

## Thymeleaf Features Used

### Dynamic Content
```html
<h1 th:text="${pageTitle}">Default Title</h1>
```

### Conditional Rendering
```html
<div th:if="${productName != null}">
    <span th:text="${productName}">Product</span>
</div>
```

### Iteration
```html
<div th:each="item : ${items}">
    <span th:text="${item}">Item</span>
</div>
```

### Fragment Inclusion
```html
<!-- Static fragment -->
<div th:replace="~{fragments/header :: fragment}"></div>

<!-- Dynamic fragment -->
<div th:replace="~{__${block}__ :: fragment}"></div>
```

## Benefits of This Approach

1. **Modularity** - Fragments can be reused across multiple pages
2. **Maintainability** - Update a fragment once, changes reflect everywhere
3. **Performance** - Caching reduces rendering overhead
4. **Flexibility** - Easy to create new page layouts by combining fragments
5. **Separation of Concerns** - Controller logic separate from view composition
6. **Type Safety** - Enum-based registry prevents typos and provides compile-time safety
7. **Modern Template Engine** - Thymeleaf provides better HTML5 support and natural templating
8. **JAR Packaging** - No need for WAR files or external servlet containers

## Troubleshooting

### Templates Not Rendering

1. Verify Thymeleaf dependency is in pom.xml
2. Check that HTML files are in `/src/main/resources/templates/`
3. Ensure `spring.thymeleaf.prefix` and `spring.thymeleaf.suffix` are configured
4. Verify template files use `.html` extension

### Fragments Not Loading

1. Verify fragment paths in `PageBlockRegistry` match actual file locations (e.g., "fragments/header")
2. Ensure each fragment has `th:fragment="fragment"` attribute
3. Check for syntax errors in Thymeleaf expressions
4. Look for errors in application logs

### Dynamic Fragment Inclusion Issues

If fragments aren't loading dynamically, check:
1. Fragment paths include "fragments/" prefix in PageBlockRegistry
2. Using correct syntax: `th:replace="~{__${block}__ :: fragment}"`
3. Fragment name matches in both template and inclusion statement

### Cache Issues

Clear cache using:
```java
@Autowired
private PageCacheService cacheService;

cacheService.clearCache();
```

### Template Cache in Development

For development, disable Thymeleaf cache in `application.properties`:
```properties
spring.thymeleaf.cache=false
```

## Migration from JSP

This project was migrated from JSP to Thymeleaf. Key changes:

### Dependencies
- Removed: `tomcat-embed-jasper`, `jakarta.servlet.jsp.jstl`
- Changed packaging from `war` to `jar`
- Using `spring-boot-starter-thymeleaf`

### Templates
- Moved from `/src/main/webapp/WEB-INF/views/` to `/src/main/resources/templates/`
- Changed extension from `.jsp` to `.html`
- Replaced JSP directives with Thymeleaf namespace
- Converted JSTL tags to Thymeleaf attributes

### Configuration
- Updated view resolver configuration
- No changes required in controllers or services

## Future Enhancements

- Add support for conditional fragments based on user roles
- Implement distributed caching (Redis/Memcached)
- Add fragment versioning for cache busting
- Create admin interface for page composition
- Add A/B testing support for fragments
- Implement Thymeleaf layout dialect for more advanced layouts
- Add CSS/JS asset management
