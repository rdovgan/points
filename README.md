# JSP Page Assembler Documentation

## Overview

The Page Assembler is a server-side system that composes full HTML pages by combining multiple JSP fragments. This approach promotes reusability and maintainability by separating page components into modular pieces.

## Architecture

### 1. **JSP Fragment Structure**

Location: `/src/main/webapp/WEB-INF/views/fragments/`

Available fragments:
- `header.jsp` - Site navigation and branding
- `footer.jsp` - Site footer with links and copyright
- `sidebar.jsp` - Categories and navigation sidebar
- `hero.jsp` - Hero section for home page
- `offers.jsp` - Featured offerings grid
- `productDetails.jsp` - Product detail view
- `reviews.jsp` - Customer reviews section

### 2. **Page Block Registry**

Location: `src/main/java/com/company/point/config/PageBlockRegistry.java`

An enum that maps page names to their required JSP fragments:

```java
HOME("home", Arrays.asList(
    "fragments/header",
    "fragments/hero",
    "fragments/offers",
    "fragments/footer"
))
```

**Registered Pages:**
- **home** - Header, Hero, Offers, Footer
- **product** - Header, Sidebar, Product Details, Reviews, Footer
- **about** - Header, Sidebar, Footer
- **contact** - Header, Footer

### 3. **Base Layout Template**

Location: `/src/main/webapp/WEB-INF/views/layouts/layout.jsp`

The master template that dynamically includes fragments based on the `blocks` model attribute:

```jsp
<c:forEach var="block" items="${blocks}">
    <jsp:include page="../${block}.jsp" />
</c:forEach>
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
   http://localhost:8080/page/
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

Create new JSP files in `/src/main/webapp/WEB-INF/views/fragments/`:

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<section class="my-section">
    <!-- Your content here -->
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

### JSP Settings

In `application.properties`:

```properties
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp
```

### Cache TTL

In `PageCacheService.java`, modify:

```java
private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
```

## Benefits of This Approach

1. **Modularity** - Fragments can be reused across multiple pages
2. **Maintainability** - Update a fragment once, changes reflect everywhere
3. **Performance** - Caching reduces rendering overhead
4. **Flexibility** - Easy to create new page layouts by combining fragments
5. **Separation of Concerns** - Controller logic separate from view composition
6. **Type Safety** - Enum-based registry prevents typos and provides compile-time safety

## Troubleshooting

### JSPs Not Rendering

1. Verify Tomcat Jasper dependency is in pom.xml
2. Check that JSP files are in `/src/main/webapp/WEB-INF/views/`
3. Ensure `spring.mvc.view.prefix` and `spring.mvc.view.suffix` are configured

### Fragments Not Loading

1. Verify fragment paths in `PageBlockRegistry` match actual file locations
2. Check that JSTL is properly configured
3. Look for errors in application logs

### Cache Issues

Clear cache using:
```java
@Autowired
private PageCacheService cacheService;

cacheService.clearCache();
```

## Future Enhancements

- Add support for conditional fragments based on user roles
- Implement distributed caching (Redis/Memcached)
- Add fragment versioning for cache busting
- Create admin interface for page composition
- Add A/B testing support for fragments
