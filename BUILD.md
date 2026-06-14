# Building the project

This project uses `gradle` as its build system.
It is split into two projects, `server` and `client`, plus a `shared` project for shared classes.

## Running the server
The server requires two files for configuration:
- a keystore file, which contains the SSL certificate used for HTTPS
- a `.env` file, containing configuration parameters (not strictly necessary as the server provides insecure defaults). The `.env` file should be placed inside the current working directory of the server, which is usually the directory that you execute the `.jar` file from, or the `server` project directory if you use gradle to execute the server.

The server can be run using `./gradlew :server:run`.
It is also possible to create an executable `.jar` with all dependencies included using `./gradlew :server:jar`

### `keystore.jks` configuration
You can generate a new debug SSL certificate called `keystore.jks` with the password `changeit` using this command:
```sh
keytool -genkeypair -alias local-backend -keyalg RSA -keysize 4096 -validity 365 -keystore keystore.jks -storepass changeit -keypass changeit -dname "CN=localhost, OU=Dev, O=UniTodo, L=Munich, C=DE" -ext "SAN=dns:localhost,ip:127.0.0.1"
```

Otherwise, use a real SSL certificate, e.g. by Let's Encrypt.
### `.env` configuration
You should include a few keys in your config:
- `SECRET_KEY`: used for signing and verifying that a session token was created by the server
- `PEPPER`: used during password hashing for more security
- `PORT`: the port to run your server on
- `KEYSTORE_PASSWORD`: the password for your keystore file. If you generated it using the above command, this needs to be set to `changeit`.
- `KEYSTORE_LOCATION`: the location of the keystore file relative to the current working directory of the server.
- `DB_URL`: the location of your sqlite database, e.g. `jdbc:sqlite:database.db`. The database file will be created if it does not exist.
- `TOMBSTONE_TTL`: the time in days that a tombstone (a marker for a deleted item) should be kept in the database before it is permanently deleted. Clients whose last sync was before the tombstone was created, and sync again after it was deleted won't be aware of its existence and will require a full sync to reconcile their state with the server.

## Running the client
The client does not require any configuration whatsoever.
It can be run using `./gradlew :client:run`.
However, by default, the client only trusts valid SSL certificates.
If you are hosting the server locally without a valid SSL certificate, like the one generated above, the client will refuse to connect.
In this case, you must compile the dev-version of the client specially using `./gradlew :client:runDev`.
This will force the client to trust all certificates, including your self-generated one.

## JDK version
The project has only been tested with JDK 21. JDK 25 does not seem to be supported by the gradle version being used.