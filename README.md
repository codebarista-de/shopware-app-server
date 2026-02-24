# Shopware App Server

[![Tests](https://github.com/codebarista-de/shopware-app-server/actions/workflows/test.yaml/badge.svg)](https://github.com/codebarista-de/shopware-app-server/actions/workflows/test.yaml)

A Java Spring Boot library for building Shopware 6 App backends.

## Overview

Unlike a Shopware Plugin, a [Shopware App][1] cannot directly extend or modify the core functionality
of a Shopware shop using PHP code. If the desired features cannot be fully realized with [App Scripts][2]
or [Storefront templates and JavaScript][3], the app must rely on the shop’s REST APIs and webhooks
to implement its features.

Such a Shopware App is therefore essentially a server application, the **App Backend**.

[1]: https://developer.shopware.com/docs/concepts/extensions/apps-concept.html
[2]: https://developer.shopware.com/docs/guides/plugins/apps/app-scripts/
[3]: https://developer.shopware.com/docs/guides/plugins/apps/storefront/

This library provides the core components needed to build a Shopware App Backend:

- 🔄 **App Registration & Lifecycle** - Registration, confirmation, and app lifecycle event handling
- 🔐 **Authentication & Security** - Shop request [signature verification and response signing][4]
- 🌐 **API Client** - Pre-configured client for the Shopware Admin API
- 📊 **Event Handling** - Webhook processing and event dispatching

[4]: https://developer.shopware.com/docs/guides/plugins/apps/app-signature-verification.html

**A Shopware App with a backend consists of two parts:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SHOPWARE SHOP                                  │
│                                                                             │
│  ┌─────────────────────┐                                                    │
│  │   Your App          │  (installed from zipped manifest.xml)              │
│  │   - manifest.xml    │                                                    │
│  └─────────┬───────────┘                                                    │
│            │                                                                │
└────────────┼────────────────────────────────────────────────────────────────┘
             │  HTTP calls (registration, webhooks, actions)
             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           YOUR APP BACKEND                                  │
│                                                                             │
│  ┌─────────────────────┐      ┌─────────────────────┐                       │
│  │  App Server Library │ ───► │  Your Business Code │                       │
│  │  (this library)     │      │  (ShopwareApp impl) │                       │
│  └─────────────────────┘      └─────────────────────┘                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

1. **The Shopware App** is a ZIP file containing at least a `manifest.xml`. It defines your app's metadata, permissions,
   webhooks, and action buttons. The manifest points to your backend's URLs.

2. **The App Backend** (built with this library) receives HTTP requests from Shopware when:
    - A shop installs your app (registration)
    - Events occur that your app subscribed to (webhooks)
    - Users click action buttons you defined

The App Server library handles the security handshake, verifies request signatures, manages OAuth tokens, and routes
requests to your code.

## Prerequisites

- Java 17 or later
- Gradle 8+ or Maven

### Add the app-server as a dependency:

**Gradle:**

```groovy
dependencies {
    implementation 'de.codebarista:shopware-app-server:1.1.0'
}
```

**Maven:**

```xml

<dependency>
    <groupId>de.codebarista</groupId>
    <artifactId>shopware-app-server</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Quick Start

See the [basic-app example](examples/basic-app/) for a complete working application demonstrating app registration,
webhooks, action buttons, and Admin API usage.

## Architecture

### Multiple Apps, One Server

The App Server library can host multiple App Backends within a single service. Each app is identified by its **App
Key**, which corresponds to a subdomain.

### App Key and Subdomain

Each app runs on its own subdomain. The subdomain (everything before the first dot) serves as the **App Key**.

For example, with app key `my-app` and domain `example.com`:

- Registration URL: `https://my-app.example.com/shopware/api/v1/registration/register`

Your `ShopwareApp` implementation must return this app key from `getAppKey()`.

## Implementing Your App Backend

Create a Spring Bean implementing the `ShopwareApp` interface:

```java

@Component // <-- This is important!
public class MyShopwareApp implements ShopwareApp {

    @Override
    public String getAppKey() {
        return "my-app";  // Must match subdomain
    }

    @Override
    public String getAppSecret() {
        return "my-secret";  // Matches <secret> in manifest.xml (dev only)
    }

    @Override
    public String getAppName() {
        return "MyShopwareApp";  // Matches <name> in manifest.xml
    }

    @Override
    public void onEvent(ShopwareEventDto event, long internalShopId,
                        Locale userLocale, String shopwareLanguageId) {
        // Handle webhook events
    }

    @Override
    public ActionResponseDto<?> onAction(ActionRequestDto action, long internalShopId,
                                         Locale userLocale, String shopwareLanguageId) {
        // Handle action button clicks
        return null;
    }
}
```

## Endpoints

The App Server automatically registers these endpoints and secures them with signature verification.

### How Signature Verification Works

Every request from Shopware includes a `shopware-shop-signature` header containing an HMAC-SHA256 hash of the request
body, signed with the shared secret (your app secret during registration, or the shop's secret after confirmation).

The App Server automatically:

1. Extracts the signature from the header
2. Computes the expected signature from the request body
3. Rejects requests where signatures don't match

This ensures requests genuinely come from Shopware and haven't been tampered with. You don't need to implement this
yourself—it's handled by the `ShopwareSignatureVerificationFilter`.

### Registration

| Endpoint                                     | Description                                                                                                                                                                    |
|----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GET /shopware/api/v1/registration/register` | Called when a shop installs your app; returns credentials for the handshake. Calls the `onRegisterShop()` or `onReRegisterShop()` method of your `ShopwareApp` implementation. |
| `POST /shopware/api/v1/registration/confirm` | Called by Shopware to confirm the registration and exchange API credentials                                                                                                    |

### Events

| Endpoint                       | Description                                                                                                                                 |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| `POST /shopware/api/v1/action` | Receives action button clicks. Handle them in the `onAction()` method of your `ShopwareApp` implementation.                                 |
| `POST /shopware/api/v1/event`  | Receives webhook events your app subscribed to in manifest.xml. Handle them in the `onEvent()` method of your `ShopwareApp` implementation. |

### Lifecycle

Lifecycle events notify your backend when the app is activated, deactivated, updated, or deleted in a shop.
These events are opt-in—Shopware only sends them if you register for them in your manifest.

> **Note:** Currently, only the `deleted` event has built-in handling (removes the shop from the database).
> The other events are received and **logged only!**
> See [GitHub Issue](https://github.com/codebarista-de/shopware-app-server/issues/9).
> 
> **Workaround:** Lifecycle events are standard webhook events you can subscribe to. 
> Route them to the `/shopware/api/v1/event` endpoint (see Events section above) and handle them there.

You need to define the URLs listed below in your manifest to use the app-server's Lifecycle controller.

| Endpoint                                      | Description                                                                                                                                                        |
|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST /shopware/api/v1/lifecycle/activated`   | Called when the shop admin activates your app                                                                                                                      |
| `POST /shopware/api/v1/lifecycle/deactivated` | Called when the shop admin deactivates your app                                                                                                                    |
| `POST /shopware/api/v1/lifecycle/updated`     | Called when your app is updated to a new version                                                                                                                   |
| `POST /shopware/api/v1/lifecycle/deleted`     | Called when your app is uninstalled from a shop. Removes the shop from the database. Calls the `onDeleteShop()` method of your `ShopwareApp` implementation before deletion. |

See the Shopware documentation for more details:

* [App Base Guide: Lifecycle events](https://developer.shopware.com/docs/guides/plugins/apps/app-base-guide.html#app-lifecycle-events)
* [App Lifecycle](https://developer.shopware.com/docs/guides/plugins/apps/app-sdks/javascript/02-lifecycle.html)

### Admin Extension

| Endpoint                                            | Description                                       |
|-----------------------------------------------------|---------------------------------------------------|
| `GET /shopware/admin/{folder}/{version}/index.html` | Serves your custom Administration UI as an iframe |

> **Note:** Custom endpoints you add require their own security implementation.
> See [App Backend Security](#app-backend-security).

## Configuration

### App Server Properties

```yaml
app-server:
  ssl-only: true
  map-localhost-ip-to-localhost-domain-name: false
  http-request-response-logging-enabled: false
  database:
    user-migrations: false
```

| Property                                    | Default | Description                                                                                                                                                                                                               |
|---------------------------------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ssl-only`                                  | `true`  | When enabled, rejects shop URLs that don't use HTTPS. Shopware Cloud always uses HTTPS, so this protects against misconfigured on-premise installations or man-in-the-middle attacks. Disable only for local development. |
| `map-localhost-ip-to-localhost-domain-name` | `false` | Maps `127.0.0.1` to `localhost` in incoming requests. Required for local development because Shopware sends the IP while your app expects the hostname.                                                                   |
| `http-request-response-logging-enabled`     | `false` | Logs full HTTP request/response bodies for outgoing Admin API calls. Useful for debugging but verbose—don't enable in production.                                                                                         |
| `database.user-migrations`                  | `false` | Controls how Liquibase migrations run. See [Database Migrations](#database-migrations) for details.                                                                                                                       |

### Development Settings

For local development, you typically need:

```yaml
app-server:
  ssl-only: false                                  # Allow HTTP (Shopware local dev uses HTTP)
  map-localhost-ip-to-localhost-domain-name: true  # Fix localhost/127.0.0.1 mismatch
  http-request-response-logging-enabled: true      # See what's happening (optional)
```

> **Production Checklist:** Set `ssl-only: true`, `map-localhost-ip-to-localhost-domain-name: false`,
> and consider disabling logging before deploying!

## Database

The App Server needs a database to store registered shops.

### Quick Start (Zero Configuration)

Out of the box, the App Server provides an in-memory SQLite database. This is perfect for a first start but **data is lost when the application restarts**.

### Configuration

Configure a persistent database using standard Spring Boot properties:

```yaml
spring:
  datasource:
    url: jdbc:sqlite:shopware_apps.db    # File-based SQLite
    driver-class-name: org.sqlite.JDBC
    hikari:
      maximum-pool-size: 1               # SQLite supports only one writer
      connection-timeout: 5000
      data-source-properties:
        foreign_keys: true               # Enable FK constraints
        busy_timeout: 5000               # Wait for locks instead of failing
        journal_mode: WAL                # Better concurrency
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: none                     # Liquibase manages the schema
```

You can also use PostgreSQL, MySQL, or any database supported by Spring Data JPA.

### Database Migrations

The App Server uses [Liquibase](https://www.liquibase.org/) for schema management. Understanding how migrations work
helps you decide whether you need custom tables.

#### Default Behavior (`user-migrations: false`)

When your app starts, the App Server automatically:

1. Creates its own `SpringLiquibase` bean
2. Runs migrations from `db/changelog/app-server-changelog-master.xml` (bundled in the library)
3. Creates the `SHOPWARE_SHOP` table for storing shop registrations

This is all you need if you're only handling webhooks and actions without storing custom data.

#### Custom Tables (`user-migrations: true`)

If your app needs its own database tables you take control of migrations:

1. **Set the flag:**

```yaml
app-server:
  database:
    user-migrations: true
```

2. **The App Server now skips creating its Liquibase bean**, allowing you to define your own.

3. **Create your master changelog** and include the App Server migrations first:

```xml
<!-- src/main/resources/db/changelog/my-changelog-master.xml -->
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog" ...>

    <!-- REQUIRED: App Server tables (SHOPWARE_SHOP) must be created first -->
    <include file="db/changelog/app-server-changelog-master.xml"/>

    <!-- Your custom tables come after -->
    <include file="changesets/0001-my-custom-table.xml" relativeToChangelogFile="true"/>

</databaseChangeLog>
```

4. **Point Spring to your changelog:**

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/my-changelog-master.xml
```

**Why include App Server migrations?** The `SHOPWARE_SHOP` table is required for the library to function. By including
`app-server-changelog-master.xml`, you ensure it's created before your tables (important if you have foreign keys
referencing it).

See the [custom-db-migration](examples/custom-db-migration/)
and [db-shop-reference-example](examples/db-shop-reference-example/) for working examples.

## App Manifest Configuration

The `manifest.xml` is the heart of your Shopware App. It's a configuration file that tells Shopware:

- What your app is called and who made it
- Where your backend is hosted (URLs)
- What permissions your app needs
- Which events your app wants to receive
- What action buttons to add to the Administration

You package this file into a ZIP (optionally with assets) and upload it to a Shopware shop or the Shopware Store.

### Registration

The `<setup>` section tells Shopware where to register when your app is installed:

```xml

<manifest xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="https://raw.githubusercontent.com/shopware/platform/trunk/src/Core/Framework/App/Manifest/Schema/manifest-1.0.xsd">
    <meta>
        <name>MyShopwareApp</name>  <!-- Must match getAppName() in your ShopwareApp -->
        <label>My Shopware App</label>
        <author>Your Company</author>
        <version>1.0.0</version>
        <license>MIT</license>
    </meta>
    <setup>
        <registrationUrl>https://my-app.example.com/shopware/api/v1/registration/register</registrationUrl>
        <secret>my-secret</secret>
    </setup>
</manifest>
```

**About the `<secret>` tag:**

- For **local development**: Include it and return the same value from `getAppSecret()` in your code
- For **production** (Shopware Store): Remove it—Shopware generates and manages the secret automatically
- **Never commit real secrets** to version control

### Webhooks

Webhooks let your app react to events in Shopware. When something happens (order created, product updated, etc.),
Shopware sends an HTTP POST to your backend.

```xml

<webhooks>
    <webhook name="orderWritten"
             url="https://my-app.example.com/shopware/api/v1/event"
             event="order.written"/>
    <webhook name="productWritten"
             url="https://my-app.example.com/shopware/api/v1/event"
             event="product.written"/>
</webhooks>
```

| Attribute | Description                                                                                                                                                      |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `name`    | Your identifier for this webhook (used in logs)                                                                                                                  |
| `url`     | Always use the `/shopware/api/v1/event` endpoint                                                                                                                 |
| `event`   | The Shopware event to subscribe to (see [Event Reference](https://developer.shopware.com/docs/resources/references/app-reference/webhook-events-reference.html)) |

Handle events in your `ShopwareApp` implementation:

```java

@Override
public void onEvent(ShopwareEventDto event, long internalShopId,
                    Locale userLocale, String shopwareLanguageId) {
    String eventName = event.data().event();  // e.g., "order.written"
    JsonNode payload = event.data().payload(); // Event-specific data

    switch (eventName) {
        case "order.written" -> processOrder(payload, internalShopId);
        case "product.written" -> syncProduct(payload, internalShopId);
    }
}
```

### Action Buttons

Action buttons add custom actions to the Shopware Administration. Users click them and your backend responds.

```xml

<admin>
    <action-button action="processOrder"
                   url="https://my-app.example.com/shopware/api/v1/action"
                   entity="order" view="detail">
        <label>Process Order</label>
        <label lang="de-DE">Bestellung verarbeiten</label>
    </action-button>
</admin>
```

| Attribute | Description                                                                     |
|-----------|---------------------------------------------------------------------------------|
| `action`  | Your identifier for this action (received in `onAction()`)                      |
| `url`     | Always use the `/shopware/api/v1/action` endpoint                               |
| `entity`  | Which entity type this button appears on (`order`, `product`, `customer`, etc.) |
| `view`    | Where the button appears: `list`, `detail`, or both                             |

Handle actions in your `ShopwareApp` implementation:

```java

@Override
public ActionResponseDto<?> onAction(ActionRequestDto action, long internalShopId,
                                     Locale userLocale, String shopwareLanguageId) {
    String actionName = action.data().action();  // e.g., "processOrder"
    List<String> ids = action.data().ids();      // Selected entity IDs

    if ("processOrder".equals(actionName)) {
        processOrders(ids, internalShopId);
        return ActionResponseDto.notification(
                NotificationResponseDto.success("Processed " + ids.size() + " orders!")
        );
    }
    return null;
}
```

**Response types** you can return:

- `ActionResponseDto.notification(...)` - Show a toast notification
- `ActionResponseDto.modal(...)` - Open a modal with custom content
- `ActionResponseDto.reload(...)` - Reload the current page

Returning `null` results in HTTP 401 (Unauthorized). Use this as a fallback for unhandled actions.

### Administration UI

The App Server can serve custom Administration UI pages.

Configure the base URL in your manifest:

```xml

<admin>
    <base-app-url>https://{app-key}.example.com/shopware/admin/{admin-extension-folder}/v1/index.html</base-app-url>
</admin>
```

The `admin-extension-folder` in the manifest must match the return value of the `getAdminExtensionFolderName()` method
of your `ShopwareApp`.

Place your HTML files in `src/main/resources/public/shopware/admin/{admin-extension-folder}/{version}/index.html`.

The admin extension folder can contain multiple versions of the UI.
Each version folder must contain the respective index.html and required content.
The version defined in the manifest will be served.

The App Server can inject these attributes into the HTML before serving:

| Placeholder       | Injected Value                                                                                                  |
|-------------------|-----------------------------------------------------------------------------------------------------------------|
| `data-token=""`   | Authentication token (validate with `TokenService`)                                                             |
| `data-version=""` | Value from `ShopwareApp#getVersion()`, or empty string if `null`. Intended to expose the backend version to UI. |

See [App Backend Security](#app-backend-security) for how to validate tokens in your endpoints.

See
Shopware [Meteor Admin SDK](https://developer.shopware.com/docs/guides/plugins/apps/administration/meteor-admin-sdk.html)
for more information.

### Notifications

Your backend can push notifications to the Shopware Administration at any time (not just in response to actions). First,
request permission:

```xml

<permissions>
    <create>notification</create>
    <permission>notification:create</permission>
</permissions>
```

Then use the `AdminApi` to send notifications:

```java

@Autowired
private AdminApi adminApi;

void notifyTest() {
    adminApi.pushSuccessMessage(myApp, shopId, "Operation completed!");
    adminApi.pushErrorMessage(myApp, shopId, "Something went wrong");
}
```

## App Backend Security

Only the endpoints called by Shopware are secured by the app server.

### Shopware Endpoints (Automatic)

Requests from Shopware to the App Server's built-in endpoints (registration, webhooks, actions) are automatically
secured via HMAC-SHA256 signature verification. You don't need to do anything—this is handled by
`ShopwareSignatureVerificationFilter`.

See the Shopware documentation on [Signing & Verification in the App System](https://developer.shopware.com/docs/guides/plugins/apps/app-signature-verification.html).

### All other Endpoints (Your Responsibility)

All endpoints other than `/shopware/api/v1/**` and `/shopware/admin/**` are not secured by default.

We recommend that you add a security configuration that denies all other endpoints by default:
```java
@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain denyOther(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(ar -> ar.anyRequest().denyAll())
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }
}
```

If your app has an Admin Extension UI (served as an iframe in Shopware Administration), that UI runs in the user's
browser. When it calls your custom backend endpoints, those requests come from the browser, not from Shopware, so they
don't have Shopware's signature.

For these requests, you need to implement your own authentication. The App Server helps by injecting a token into your
HTML.

### Token Injection for Admin Extensions

When Shopware loads your Admin Extension, it requests `/shopware/admin/{folder}/{version}/index.html` from your backend.
Before serving the file, the App Server can inject authentication tokens into your HTML.

The token is valid for one hour for the app that requested it.

**Step 1: Add placeholder attributes to your HTML**

In your `index.html`, add empty `data-token` attribute:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Admin Extension</title>
</head>
<body data-token="">
<div id="app"></div>
<script src="main.js"></script>
</body>
</html>
```

**Step 2: The App Server injects the token**

When serving this file, the App Server replaces the empty value:

`data-token=""` → `data-token="01234asdf..."`

**Step 3: Your JavaScript reads the token**

```javascript
const token = document.body.dataset.token;
const version = document.body.dataset.version;

// Include token in API calls to your backend
fetch('/api/my-endpoint?shopId=' + shopId + '&token=' + token)
```

### Validating Tokens in Your Endpoints

The injected token can be validated with the `TokenService`.

**Option 1: Using `@PreAuthorize` (Recommended)**

```java

@RestController
@RequestMapping("/api")
public class MyApiController {

    @GetMapping("/my-endpoint")
    @PreAuthorize("@tokenService.isAppTokenValid(@myShopwareApp, #shopId, #token)")
    public ResponseEntity<?> myEndpoint(
            @RequestParam("shopId") String shopId,
            @RequestParam("token") String token) {
        // Token is valid if we get here
        return ResponseEntity.ok("Success");
    }
}
```

The `@PreAuthorize` annotation:

- `@tokenService` - References the `TokenService` bean
- `@myShopwareApp` - References your `ShopwareApp` bean by name (the class name in camelCase)
- `#shopId`, `#token` - References the method parameters

For `@PreAuthorize` to work, enable method security in your application:

```java

@EnableMethodSecurity
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

**Option 2: Programmatic validation**

```java

@Autowired
private TokenService tokenService;

@Autowired
private MyShopwareApp myShopwareApp;

@GetMapping("/my-endpoint")
public ResponseEntity<?> myEndpoint(
        @RequestParam("shopId") String shopId,
        @RequestParam("token") String token) {

    if (!tokenService.isAppTokenValid(myShopwareApp, shopId, token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Token is valid
    return ResponseEntity.ok("Success");
}
```

## HTTP Client

> **Note:** Avoid using the `shopwareRestTemplate` directly. Use the `AdminApi` instead.

The App Server provides a pre-configured `RestTemplate`:

```java

@Autowired
@Qualifier("shopwareRestTemplate")
private RestTemplate shopwareRestTemplate;
```

Features: disabled redirects, buffered requests, enhanced error handling, optional request/response logging.

## Examples

| Example                                                          | Description                                               |
|------------------------------------------------------------------|-----------------------------------------------------------|
| [basic-app](examples/basic-app/)                                 | Complete app with webhooks, action buttons, and Admin API |
| [custom-db-migration](examples/custom-db-migration/)             | Custom database tables with Liquibase                     |
| [db-shop-reference-example](examples/db-shop-reference-example/) | Foreign key relationships to SHOPWARE_SHOP                |

## Versioning

This project follows [Semantic Versioning 2.0.0](https://semver.org/).

- **MAJOR** - Breaking changes
- **MINOR** - New features (backwards compatible)
- **PATCH** - Bug fixes

See [CHANGELOG.md](CHANGELOG.md) for release history.

## License

MIT License

## Contributing

Contributions welcome! Please submit a Pull Request.

## Support

- [GitHub Issues](https://github.com/codebarista-de/shopware-app-server/issues) - Bug reports and feature requests
- [GitHub Discussions](https://github.com/codebarista-de/shopware-app-server/discussions) - Questions and community help
