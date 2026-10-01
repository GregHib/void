By default void saves players to `/data/saves/*.toml` however when running in a production environment you might want to store in a database rather than toml files, void support [PostgreSQL](#postgresql) out of the box, and [other SQL databases](#driver-support) with a little modification.

## Postgresql

Storage using Postgresql is simple, first you must build the server with the database module by passing `-PincludeDb = true` in your gradle build command, or adding `includeDb = true` in the gradle.properties file before building your server or jar file.

Then replace the `storage.type=files` line in `game.properties` with `storage.type=database` and fill out the following details about your database:

```properties
storage.type=database
storage.database.username=postgres # The database username
storage.database.password=password # The database password
storage.database.driver=org.postgresql.Driver # The database driver - see JDBC
storage.database.jdbcUrl=jdbc:postgresql://localhost:5432/game?reWriteBatchedInserts=true
stoage.database.poolSize=4 # Number of connections to use
```

### JDBC
Java database connectivity (JDBC) is a specification for connecting to multiple types of SQL databases.
By default Void only comes bundled with the Postgresql Driver but can support all major database providers.

## Driver Support
> [!CAUTION]
> MySql is no longer supported due to lack of ARRAY type support

Follow these steps to add support for your desired database, for this example we will add support for MySQL:

1. In `/engine/build.gradle.kts` add the driver for the database of your choosing

```gradle.kts
implementation("com.mysql:mysql-connector-j:8.3.0")
```

2. Build your server .jar on the command line by running

```
./gradlew assembleBundleDist
```

Which will create `/game/build/distributions/void-dev.zip` for you to use.


3. In `game.properties` set the driver class name and jdbc url (as well as login credentials)

```properties
storage.database.driver=com.mysql.cj.jdbc.Driver
storage.database.jdbcUrl=jdbc:mysql://localhost:3306/game
```

4. For some non-concurrent databases

> [!TIP]
> This information can be found by googling "mysql jdbc driver maven" and "mysql jdbc driver class name"

Now when you run your game it will connect to the database specified in the JDBC url, load and store player data there.