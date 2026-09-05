# Building the project

This project uses `gradle` as its build system.
It is split into two projects, `server` and `client`, plus a `shared` project for shared classes.

The build process is set up to support two kinds of environments:
- local development
- production deployment
 
The main difference is that production deployments should use a real SSL certificate and might be behind a reverse proxy.

## Running the server
The server requires two files for configuration:
- a keystore file, which contains the SSL certificate used for HTTPS (except for when `USE_HTTPS` is set to `false` in the `.env` file, which is useful for reverse proxies that handle SSL on their own)
- a `.env` file, containing configuration parameters (not strictly necessary as the server provides insecure defaults). The `.env` file should be placed inside the current working directory of the server, which is usually the directory that you execute the `.jar` file from, or the `server` project directory if you use gradle to execute the server.

The server can be run using `./gradlew :server:run`.
It is also possible to create an executable `.jar` with all dependencies included using `./gradlew :server:fatJar`. Run it with `java -jar server/build/libs/server-all.jar` from the repository root (configuration files are read from the current working directory).
The equivalent client commands are `./gradlew :client:fatJar` and `java -jar client/build/libs/client-all.jar`.

### `keystore.jks` configuration
#### Production
When you host the production server, you might be using a reverse proxy that handles SSL for you.
In this case, set `USE_HTTPS` to `false` in the `.env` file. The reverse proxy will handle the SSL certificate, and the server won't need a keystore.
Otherwise, you should use a real SSL certificate, e.g. by Let's Encrypt.

#### Local
For local development, you can use a self-signed SSL certificate.
You can generate a new debug SSL certificate called `keystore.jks` with the password `changeit` using this command:
```sh
keytool -genkeypair -alias local-backend -keyalg RSA -keysize 4096 -validity 365 -keystore keystore.jks -storepass changeit -keypass changeit -dname "CN=localhost, OU=Dev, O=UniTodo, L=Munich, C=DE" -ext "SAN=dns:localhost,ip:127.0.0.1"
```
In this case, remember to include support for trusting all certificates when compiling the client, or otherwise the client will reject all connections to the server if it uses a self-signed certificate.

### `.env` configuration
You should include a few keys in your config:
- `SECRET_KEY`: used for signing and verifying that a session token was created by the server
- `PEPPER`: used during password hashing for more security
- `PORT`: the port to run your server on
- `KEYSTORE_PASSWORD`: the password for your keystore file. If you generated it using the above command, this needs to be set to `changeit`.
- `KEYSTORE_LOCATION`: the location of the keystore file relative to the current working directory of the server.
- `DB_URL`: the location of your sqlite database, e.g. `jdbc:sqlite:database.db`. The database file will be created if it does not exist.
- `TOMBSTONE_TTL`: the time in days that a tombstone (a marker for a deleted item) should be kept in the database before it is permanently deleted. Clients that last synced before the tombstone was created, and sync again after its deletion won't be aware of its existence and require a full sync to reconcile their state with the server.
- `USE_HTTPS`: enables or disables https support. Use this if SSL is taken care of by a reverse proxy.

## Running the client
The client does not require any configuration at runtime.
However, there are a few flags that you can/should set during build time in the `gradle.properties` file:
- `baseUrl`: the base URL of the server. For local development, this should be set to `https://localhost:{PORT}/api` with `{PORT}` replaced by the port you set in the server's `.env` file.
- `trustAllCertificates`: set to `true` if you want the client to trust all SSL certificates, including self-signed ones. This is useful for local development, but should be set to `false` in production.

## JDK version
The project has only been tested with JDK 21. JDK 25 does not seem to be supported by the gradle version being used.
