# Shopware App Server

**A Java Spring Boot library for building a Shopware 6 App Backend server.**

Unlike a Shopware Plugin, a [Shopware App][1] cannot directly extend or modify the core functionality of a Shopware
shop using PHP code. If the desired features cannot be fully realized with [App Scripts][2]
or [Storefront templates and JavaScript][3], the app must rely on its own backend service --
and this is where the app-server comes into the game.

[1]: https://developer.shopware.com/docs/concepts/extensions/apps-concept.html

[2]: https://developer.shopware.com/docs/guides/plugins/apps/app-scripts/

[3]: https://developer.shopware.com/docs/guides/plugins/apps/storefront/

This library provides the core components needed to build such an App Backend:

- 🔄 **App Registration & Lifecycle** - Registration, confirmation, and app lifecycle event handling
- 🔐 **Authentication & Security** - Shop request [signature verification and response signing][4]
- 🌐 **API Client** - Pre-configured client for the Shopware Admin API
- 📊 **Event Handling** - Webhook processing and event dispatching

[4]: https://developer.shopware.com/docs/guides/plugins/apps/app-signature-verification.html

## You need your own Backend Service?

If you need your own App Backend for your Shopware App, your *App* will consist of two coupled artifacts:

1. The actual **Shopware App**: the artifact that is installed in Shopware shops and contains at least the
   `manifest.xml`.
2. The **App Backend**: the backend service that serves the requests from your Shopware App and is build upon this
   library -- the App Server library.

Both are glued together by endpoints exposed by the App Backend and referenced in the `manifest.xml`.
The endpoints are then consumed by your app when its being installed in Shopware or used by the user.
See the section about Endpoints exposed by the App Server library below.

For a detailed description, information about how to configure your App Backend, read on here.

## Quick Start

For a quick start with examples, follow the Getting Started Guide TODO

## The App Server

The App Server library allows to have multiple **App Backend implementations** -- one per Shopware App --
within your single **App Server** service.

Every App Backend implementation is identified by its own App Key and invoked upon requests from the corresponding
Shopware App to a defined subdomain.

### App Subdomain

Every app has to run on its own unique subdomain.

The subdomain must not contain dots, as everything from the first symbol until the first dot from the left
is considered the subdomain.

### App Key

The subdomain is used as the **App Key**, all requests to this subdomain will be routed
to the corresponding App Backend implementation. This is why the subdomain and the App Key must match.

If the name of your app is `my-shopware-app`, and your domain is `my-domain.de`,
the registration endpoint will look like this:
`https://my-shopware-app.my-domain.de/shopware/api/v1/registration/register`.

When implementing the app backend, you need to return the **App Key** from the `getAppKey` method
of your `ShopwareApp` implementation.

### Start implementing your App Backend

After this consideration, start creating your own App Backend by implementing the `ShopwareApp` interface.
Add the `@Component` annotation to make it a Spring Bean:

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
        // The shared secret that is used by shopware to sign requests to the app backend.
        // During development or testing, this is probably the value of the <secret> tag from your manifest.xml
        return "testsecret";
    }

    @Override
    public String getAppName() {
        return "MyAppTechnicalName";
    }
    
   ...
}
```

Now implement the methods to react on actions or webhooks or add your own REST Controller
and add endpoints to your App Backend.

See hints for local development below.

## Endpoints exposed by the App Server

The App Server automatically registers endpoints and comes with its own Spring Security configuration.
Incoming requests from Shopware to endpoints exposed by App Server library are automatically verified
using HMAC-SHA256 signatures: it verifies the validity of the `shopware-shop-signature` header.
See the `ShopwareSignatureVerificationFilter` for implementation.

> ⚠️ If you add your own endpoints, you need to protect them yourself. See section *Endpoints exposed by you*

### App Registration Endpoints

* `GET /shopware/api/v1/registration/register`
* `POST /shopware/api/v1/registration/confirm`

App registration and registration confirmation is handled by the App-Server library.
Provide the correct URL to your app backend in the `<registrationUrl>` tag of the `manifest.xml`.
For local development and testing also specify the `<secret>`.
Return the same secret from the `getAppSecret` method of your `ShopwareApp` implementation.

> ⚠️ Never commit credentials into version control!

### App Lifecycle Endpoints

* `POST /shopware/api/v1/lifecycle/updated`
* `POST /shopware/api/v1/lifecycle/deleted`
* `POST /shopware/api/v1/lifecycle/activated`
* `POST /shopware/api/v1/lifecycle/deactivated`

After successful installation, those endpoints are called from Shopware when the respecting App lifecycle event occurs.
Do not rely on them and implement your app backend to be tolerant.

### Shopware Event Endpoints

* `POST /shopware/api/v1/action`
* `POST /shopware/api/v1/event`

Those endpoints are called when an Action button is clicked
or a Shopware event occurs, that the App registered to in the `manifest.xml`.

### Admin Extension Endpoint

TODO TODO TODO TODO TODO: make optional, make injection of data-token and data-version optional

* `GET /shopware/admin/{admin-extension-folder}/{version}/index.html`

This endpoint exposes the UI of your Shopware App, that will be rendered as i-frame within the Shopware Administration.

Your UI must be build externally. It will then be served statically by the App Backend.

### #TODO

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

The App Server uses a **Liquibase setup** to separate core App Server migrations from your custom migrations:

**App Server Migrations (Automatic):**

The App Server library relies on a database to store its data and runs the Liquibase migration on startup.

- Runs automatically when the application starts
- Creates core tables required by the App Server (`SHOPWARE_SHOP`, etc.)
- Uses `app-server-core` context to avoid conflicts
- Is required for App Server functionality

**User Migrations (Optional):**

If you need your own Database tables, you need to set this property to `true`:

```yaml
app-server:
  database:
    user-migrations: true
```

This disables the automatic App Server Migrations as described before.
The App Server will not create a SpringLiquibase bean so that you can create one in your App Backend,
either by configuration or Java.

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

TODO

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

## Local development and testing

For local development and testing you can use `localhost` with e.g. port `8080` as your domain.
In this case also set the `app-server.map-localhost-ip-to-localhost-domain-name` property to `true`.
Don't forget to set it to `false` for production!
Specify the port with the Spring Boot `server.port` property in your application settings.

For simplicity, you can also choose to use `http`.
In this case also set the `app-server.ssl-only` property to `false`.
Don't forget to set it to `true` for production!

## Glue the App Backend to your Shopware App

### The manifest.xml file

The `manifest.xml` needs to glue the Shopware App to the app backend. This is done by specifying the correct URLs.

Remember that every app has to run on its own unique subdomain, this is why all URLs start with `my-shopware-app`,
followed by your domain and again followed by the respective endpoints exposed by the App Server library
(see Framework Endpoints).

For local development and testing you can use `localhost` as your domain,
see App backend URL for local development and testing.

#### URL for registering new App installations

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

Add the `<secret>` tag for local development and testing only. Before uploading your App artifact in your
Shopware Account, remove this tag.

#### URL for Admin Extension

# #TODO

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
can [send notification to the Administration](https://developer.shopware.com/docs/guides/plugins/apps/app-base-guide.html#app-notification),
initiated by the backend. To allow your app to push such notifications, add these permissions:

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

### Endpoints exposed by you

TODO

The App Server can inject a token into the `index.html` of your Admin Extension
that can be used for requests to your endpoints. To make this work, you need to add the string
`data-token=""` to one of your tags, e.g. the `<body>` starting tag:

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

The `data-token property will then be filled with a generated token valid for your Shopware App and App Backend.

TODO: TOKEN VALIDITY, ...

> ⚠️ If you add your own endpoints you have to check authorization yourself

Send the token with your requests to the App Backend and verify its validity.
The `TokenService` offers methods for this case, you can call them from e.g. a `@PreAuthorize` check:

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

Because your App Backend implementation is a Spring Bean, it will automatically be injected
if you correctly reference it in the method signature.

Do not forget to enable method security in your service: Add the `@EnableMethodSecurity` annotation
to your Spring Boot Main class or Web Security class.

## Database

Configure your database using standard Spring Boot `spring.datasource.*` properties.
You should use Liquibase for

The App Server automatically creates and manages these tables via Liquibase:

- `shopware_shop` - Registered shop information

TODO

## License

This App Server is designed to be open-sourced under MIT license.

## Contributing

TODO

## Support

TODO

## Testing

TODO

# OTHER THINGS TO DESCRIBE:

- `data-version=""` property similar to `data-token=""`