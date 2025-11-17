# Shopware App Server

**A Java Spring Boot library for building a Shopware 6 App Backend server.**

Unlike a Shopware Plugin, a [Shopware App][1] cannot directly extend or modify the core functionality of a Shopware
shop using PHP code. When the desired features cannot be fully realized with [App Scripts][2]
or [Storefront templates and JavaScript][3], the app must rely on its own backend service—this is where the App Server
comes into play.

[1]: https://developer.shopware.com/docs/concepts/extensions/apps-concept.html

[2]: https://developer.shopware.com/docs/guides/plugins/apps/app-scripts/

[3]: https://developer.shopware.com/docs/guides/plugins/apps/storefront/

This library provides the core components needed to build such an App Backend:

- 🔄 **App Registration & Lifecycle** - Registration, confirmation, and app lifecycle event handling
- 🔐 **Authentication & Security** - Shop request [signature verification and response signing][4]
- 🌐 **API Client** - Pre-configured client for the Shopware Admin API
- 📊 **Event Handling** - Webhook processing and event dispatching

[4]: https://developer.shopware.com/docs/guides/plugins/apps/app-signature-verification.html

## Do You Need Your Own Backend Service?

If your Shopware App requires its own backend service, your application will consist of two coupled artifacts:

1. The **Shopware App**: The artifact installed in Shopware shops, containing at least the `manifest.xml` file.
2. The **App Backend**: The backend service that handles requests from your Shopware App, built upon this library.

Both components are connected through endpoints exposed by the App Backend and referenced in the `manifest.xml`.
These endpoints are invoked when your app is installed in Shopware or when users interact with it.
See the section about endpoints exposed by the App Server library below.

For detailed configuration instructions and implementation guidance, continue reading.

## Quick Start

A comprehensive Getting Started Guide with step-by-step examples is coming soon.
In the meantime, see the implementation example in the "Start implementing your App Backend" section below.

## The App Server

The App Server library allows you to host multiple **App Backend implementations**—one per Shopware App—within a single
**App Server** service.

Each App Backend implementation is identified by its unique App Key and invoked when requests arrive
from the corresponding Shopware App to its designated subdomain.

### App Subdomain

Each app must run on its own unique subdomain.

The subdomain must not contain dots, as everything from the first character until the first dot (reading left to right)
is considered the subdomain.

### App Key

The subdomain serves as the **App Key**. All requests to this subdomain will be routed to the corresponding App Backend
implementation.
This is why the subdomain and App Key must match.

For example, if your app name is `my-shopware-app` and your domain is `my-domain.de`, the registration endpoint will be:
`https://my-shopware-app.my-domain.de/shopware/api/v1/registration/register`

When implementing your App Backend, you must return the **App Key** from the `getAppKey()` method of your `ShopwareApp`
implementation.

### Start Implementing Your App Backend

Create your own App Backend by implementing the `ShopwareApp` interface. Add the `@Component` annotation to register it
as a Spring Bean:

```java

@Component // Make this a Spring Bean
public class MyShopwareApp implements ShopwareApp {

    @Override
    public String getAppKey() {
        // The App Key aka the subdomain
        return "my-shopware-app";
    }

    @Override
    public String getAppSecret() {
        // The shared secret used by Shopware to sign requests to the app backend.
        // During development or testing, this matches the <secret> tag value in your manifest.xml
        return "testsecret";
    }

    @Override
    public String getAppName() {
        return "MyAppTechnicalName";
    }
    
   ...
}
```

Now implement the methods to respond to actions or webhooks, or add your own REST controllers with custom endpoints to
your App Backend.

See the "Local development and testing" section below for development guidance.

## Endpoints Exposed by the App Server

The App Server automatically registers endpoints and provides its own Spring Security configuration.
Incoming requests from Shopware to these endpoints are automatically verified using HMAC-SHA256 signatures by validating
the `shopware-shop-signature` header.
See `ShopwareSignatureVerificationFilter` for the implementation details.

> ⚠️ **Important**: If you add your own custom endpoints, you must implement security measures yourself. See the
> section "Endpoints exposed by you" below.

### App Registration Endpoints

* `GET /shopware/api/v1/registration/register`
* `POST /shopware/api/v1/registration/confirm`

App registration and confirmation are handled automatically by the App Server library.
Provide the correct URL in the `<registrationUrl>` tag of your `manifest.xml`.
For local development and testing, also specify the `<secret>` tag.
Return the same secret value from the `getAppSecret()` method of your `ShopwareApp` implementation.

> ⚠️ **Never commit credentials to version control!**

### App Lifecycle Endpoints

* `POST /shopware/api/v1/lifecycle/updated`
* `POST /shopware/api/v1/lifecycle/deleted`
* `POST /shopware/api/v1/lifecycle/activated`
* `POST /shopware/api/v1/lifecycle/deactivated`

After successful installation, these endpoints are called by Shopware when the respective app lifecycle event occurs.
Implement your app backend to handle these events gracefully and be tolerant of delivery failures.

### Shopware Event Endpoints

* `POST /shopware/api/v1/action`
* `POST /shopware/api/v1/event`

These endpoints are called when an action button is clicked or when a Shopware event occurs that the app has registered
for in the `manifest.xml`.

### Admin Extension Endpoint

* `GET /shopware/admin/{admin-extension-folder}/{version}/index.html`

This endpoint exposes the UI of your Shopware App, which will be rendered as an iframe within the Shopware
Administration.

Your UI must be built externally and will be served statically by the App Backend.

> **Note**: Future enhancements will make this endpoint optional and allow configuration of `data-token` and
`data-version` injection.

## Configuration

The App Server library uses Spring's auto-configuration mechanism.

### App Server Properties

All App Server configuration uses the `app-server` prefix:

```yaml
app-server:
  # Enforce SSL-only communication with Shopware
  ssl-only: true  # Default: true

  # Map localhost IP to domain name for development
  map-localhost-ip-to-localhost-domain-name: false  # Default: false

  # HTTP request/response logging for debugging
  http-request-response-logging-enabled: false  # Default: false

  # Active execution of user-defined Liquibase changesets
  database:
    user-migrations: false # Default: false
```

**SSL Enforcement:**

```yaml
app-server:
  ssl-only: true  # Default: true - Reject HTTP URLs in production
```

**Development Helpers:**

```yaml
app-server:
  map-localhost-ip-to-localhost-domain-name: true  # Map 127.0.0.1 to localhost
```

**Access Configuration Bean:**

```java

@Autowired
private AppServerConfiguration appServerConfiguration;

if(appServerConfiguration.

isSslOnly()){
        // Enforce HTTPS URLs only
        }
```

## Database

### Data source

The App Server uses **Spring Boot's standard DataSource configuration** with defaults for easy development.

#### Quick Start (Zero Configuration)

No configuration needed - the App Server automatically provides SQLite in-memory.

#### Other cases (Standard Spring Boot)

Use standard Spring Boot DataSource configuration - the App Server automatically detects it:

```yaml
spring:
  datasource: # Standard Spring Boot DataSource configuration
    url: jdbc:sqlite:shopware_apps.db
    driver-class-name: org.sqlite.JDBC
    hikari: # configuration needed for SQLite to work correctly, prepared to be used with LiteStream:
      maximum-pool-size: 1
      connection-timeout: 5000
      data-source-properties:
        foreign_keys: true
        busy_timeout: 5000
        journal_mode: WAL

  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: none  # Use Liquibase
    open-in-view: false
```

**How it works:**

- App Server provides `DataSourceProperties` defaults only when none exist
- Spring Boot's `DataSourceAutoConfiguration` handles the rest
- Your configuration automatically overrides App Server defaults
- No manual switches or App Server-specific properties needed

### Database Migrations

The App Server uses **Liquibase** to separate core App Server migrations from your custom migrations:

**App Server Migrations (Automatic):**

The App Server library relies on a database to store its data and runs Liquibase migrations automatically on startup.

- Runs automatically when the application starts
- Creates core tables required by the App Server (`SHOPWARE_SHOP`, etc.)
- Uses `app-server-core` context to avoid conflicts
- Is required for App Server functionality

**User Migrations (Optional):**

If you need your own database tables, set this property to `true`:

```yaml
app-server:
  database:
    user-migrations: true
```

This disables the automatic App Server migrations described above.
The App Server will not create a SpringLiquibase bean, allowing you to create one in your App Backend through either
configuration or Java code.

You then need to include the required App Server migrations into your Liquibase Changelog master file:

```xml

<databaseChangeLog ...>
<include file="db/changelog/app-server-changelog-master.xml"/>
        <!-- your migrations hereafter-->
        </databaseChangeLog>
```

This ensures the required database table(s) for the App Server to handle registration of shops will be created.

The App Server changelog needs to be run with no Liquibase context set or, if you use context,
the `app-server-core` context.

Lastly, register your Liquibase change databaseChangeLog file with Spring, e.g.:

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/my-user-changelog-master.xml
```

## HTTP Client Configuration

The App Server provides a pre-configured `RestTemplate` optimized for Shopware API communication:

**Features:**

- **Disabled redirects** - Prevents infinite redirect loops with Shopware APIs
- **Buffered requests** - Disables chunked encoding (required for some endpoints)
- **Enhanced error handling** - Treats all non-2xx responses as errors
- **Optional logging** - Request/response logging for debugging

**Configuration:**

```yaml
app-server:
  http-request-response-logging-enabled: true  # Enable detailed HTTP logging
```

**Bean name:** `shopwareRestTemplate` - Inject this RestTemplate for Shopware API calls

**Example usage:**

```java

@Autowired
private RestTemplate shopwareRestTemplate;

// This RestTemplate is automatically configured for Shopware APIs
ResponseEntity<String> response = shopwareRestTemplate.exchange(
        shopwareApiUrl, HttpMethod.GET, requestEntity, String.class);
```

## Local Development and Testing

For local development and testing, you can use `localhost` with a port such as `8080` as your domain.
Set the `app-server.map-localhost-ip-to-localhost-domain-name` property to `true` when using localhost.
**Remember to set it back to `false` for production!**
Specify the port using the Spring Boot `server.port` property in your application settings.

For development simplicity, you can use `http` instead of `https`.
Set the `app-server.ssl-only` property to `false` to allow HTTP connections.
**Remember to set it back to `true` for production!**

## Connect the App Backend to Your Shopware App

### The manifest.xml File

The `manifest.xml` file connects your Shopware App to the App Backend by specifying the correct URLs.

Remember that each app must run on its own unique subdomain. This is why all URLs start with the app key (e.g.,
`my-shopware-app`), followed by your domain, and then the respective endpoints exposed by the App Server library.

For local development and testing, you can use `localhost` as your domain. See the "Local Development and Testing"
section above for configuration details.

#### Registration URL

Configure the registration endpoint in your `manifest.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="https://raw.githubusercontent.com/shopware/platform/trunk/src/Core/Framework/App/Manifest/Schema/manifest-1.0.xsd">
    <meta>
        ...
    </meta>

    <setup>
        <registrationUrl>http://my-shopware-app.my-domain.de/shopware/api/v1/registration/register</registrationUrl>
        <secret>mysecret</secret>
    </setup>
</manifest>

```

Include the `<secret>` tag for local development and testing only. Remove this tag before uploading your app artifact to
your Shopware account.

#### Admin Extension URL

Configure the admin extension base URL in your `manifest.xml`:

```xml

<manifest>
    ...
    <admin>
        <base-app-url>http://my-shopware-app.my-domain.de/shopware/admin/admin-tool/v1/index.html</base-app-url>
    </admin>
</manifest>
```

#### URL for Action Button events

You can add
[Action Buttons](https://developer.shopware.com/docs/guides/plugins/apps/administration/add-custom-action-button.html)
to Shopware Administration pages.

```xml

<manifest>
    ...
    <admin>
        <action-button action="my_action_event" url="http://my-shopware-app.my-domain.de/shopware/api/v1/action"
                       entity="order" view="detail">
            <label>Trigger My Action</label>
            <label lang="de-DE">Meine Action ausführen</label>
        </action-button>
    </admin>
</manifest>
```

Then handle the Event from the Action Button in your `ShopwareApp` by implementing the `onAction` method.
Return a suiting `ActionResponseDto` to make the
[Administration react](https://developer.shopware.com/docs/guides/plugins/apps/administration/add-custom-action-button.html#providing-feedback-in-the-administration)
to the triggered Action.

```java
    public ActionResponseDto<?> onAction(ActionRequestDto action, long internalShopId, Locale userLocale, String shopwareLanguageId) {
    String actionName = action.data().action();
    if ("my_action_event".equals(actionName)) {
        String shopId = action.source().shopId();
           ...
        return ActionResponseDto...;
    }
        ...
}
```

#### URL for Webhooks

You can register to [Shopware Events](https://developer.shopware.com/docs/guides/plugins/apps/webhook.html).

The `name` property is the name of the event in shopware. The value of the `event` property is the name used
in the app backend.

```xml

<manifest>
    ...
    <webhooks>
        <webhook name="shopwareEvent" url="http://my-shopware-app.my-domain.de/shopware/api/v1/event"
                 event="my_event_name"/>
    </webhooks>
</manifest>
```

Then handle the Event from the Webhook in your `ShopwareApp` by implementing the `onEvent` method:

```java
public void onEvent(ShopwareEventDto event, long internalShopId, @Nullable Locale userLocale, String shopwareLanguageId) {
    String eventName = event.data().event();
    if ("my_event_name".equalsIgnoreCase(eventName)) {
        String shopId = event.source().shopId();
           ...
    }
}
```

#### Notifications

You
can [send notifications to the Administration](https://developer.shopware.com/docs/guides/plugins/apps/app-base-guide.html#app-notification)
from your backend. To allow your app to push notifications, add these permissions:

```xml

<manifest>
    ...
    <permissions>
        ...
        <create>notification</create>
        <permission>notification:create</permission>
    </permissions>
</manifest>
```

Then you can use the `push{Success|Info|Warning|Error}Message` methods from the `AdminApi`.

## App Backend Security

### Endpoints Exposed by You

TODO

When you add custom endpoints to your App Backend, you are responsible for implementing security.

The App Server can inject a token into the `index.html` of your Admin Extension for use in authenticated requests to
your endpoints. To enable this, add the `data-token=""` attribute to one of your HTML tags (e.g., the `<body>` tag):

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>My Extension</title>
</head>
<body data-token="" data-version="">
<div id="app"></div>
<script type="module" src="main.ts"></script>
</body>
</html>
```

The `data-token` attribute will be populated with a generated token valid for your Shopware App and App Backend.

TODO: TOKEN VALIDITY, ...

> ⚠️ **Important**: You must implement authorization checks for your custom endpoints yourself.

Send the token with your requests to the App Backend and verify its validity.
The `TokenService` provides methods for token validation. You can use them in `@PreAuthorize` checks:

```java

@GetMapping("/my-endpoint")
@PreAuthorize("@tokenService.isAppTokenValid(@myAppBackend, #shopId, #token)")
public ResponseEntity<AdminToolFeatureListDto> getFeatures(
        @RequestParam("shopId") String shopId,
        @RequestParam(name = "language", required = false) String language,
        @RequestParam(ApiConstants.APP_TOKEN_PARAM) String token
) {
    ...
}
```

Your App Backend implementation is a Spring Bean and will be automatically injected when correctly referenced in the
method signature.

Remember to enable method security by adding the `@EnableMethodSecurity` annotation to your Spring Boot main class or
security configuration class.

## Database Tables

The App Server automatically creates and manages these tables via Liquibase:

- `SHOPWARE_SHOP` - Stores registered shop information and OAuth credentials

Configure your database using standard Spring Boot `spring.datasource.*` properties as described in the "Database"
section above.

## License

This project is licensed under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or feature requests, please open an issue on the project repository.

TODO

# OTHER THINGS TO DESCRIBE:

- `data-version=""` property similar to `data-token=""`