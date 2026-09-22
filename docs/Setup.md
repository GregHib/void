## Server

Clone the main void project to your local computer, open in IntelliJ as a gradle project.

## Download list

* Pre-modified cache files - https://mega.nz/folder/ZMN2AQaZ#4rJgfzbVW0_mWsr1oPLh1A
* File server jar and properties - https://github.com/GregHib/rs2-file-server/releases
* Client jar - https://mega.nz/folder/lZcHDY7R#X0myX8SlXTI0BYYgh9-zpQ

## Game Cache

Extract the `active-cache` files into the `/data/cache/active/` directory.

[Build your own](cache-building)

## File Server

Download `rs2-file-server.jar` and `file-server.properties` into the same folder.

Extract cache to a folder (e.g. `/data/cache/`)

> Make sure `cachePath` in file-server.properties points to the correct relative directory.

Launch `rs2-file-server.jar`

> The file server has no UI so will look like it's not doing anything when you click it. Run with `java -jar rs2-file-server.jar` to see output.

## Client

Launch `void-client.jar`

Login with any username & password.

[Build your own](client-building)

## Tests

If you try running the unit tests and recieve an error similar to `Execution failed for task ''. No tests found`

Go to `File | Settings | Build, Execution, Deployment | Build Tools | Gradle` and change `Run tests using:` from `Gradle (Default)` to `IntelliJ IDEA`