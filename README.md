<div align="center">
<h1>Void</h1>
<a href="https://github.com/GregHib/void">
  <img src="https://github.com/user-attachments/assets/93ad1d53-ac07-4e57-82b4-94a3e38e39ad" alt="void">
</a>

[![Release](https://github.com/GregHib/void/actions/workflows/create_release.yml/badge.svg)](https://github.com/GregHib/void/actions/workflows/create_release.yml)
[![Docker](https://img.shields.io/badge/Docker-latest-blue.svg?logo=docker)](https://hub.docker.com/r/greghib/void)
[![Codecov](https://codecov.io/gh/GregHib/void/graph/badge.svg?token=7W6PTSHUTT)](https://codecov.io/gh/GregHib/void)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

<h1>RuneScape Revived</h1>

<p>Rediscover 2011 RuneScape with modern server emulation at your fingertips.</p>

<a href="#features">Features</a> &nbsp;&bull;&nbsp;
<a href="#quick-setup">Quick setup</a> &nbsp;&bull;&nbsp;
<a href="#development">Development</a> &nbsp;&bull;&nbsp;
<a href="https://rune-server.org/runescape-development/rs-503-client-and-server/projects/697260-void-634-a.html" target="_blank">Blog</a> &nbsp;&bull;&nbsp;
<a href="https://greghib.github.io/void/docs">Documentation</a> &nbsp;&bull;&nbsp;
<a href="https://github.com/GregHib/void/issues">Bugs</a> &nbsp;&bull;&nbsp;
<a href="https://greghib.github.io/void-map/">World Map</a>

![void in game picture](https://github.com/user-attachments/assets/fe515ae5-3ae9-4251-a17c-064d7f514579)

</div>

## Features

* **High-performance**: Capable of supporting thousands of concurrent players.
* **Rich content**: Offers a [wide range](https://greghib.github.io/void/docs/content-progress) of emulated content.
* **AI player characters**: Introduced intelligent player bots to enhance immersion.
* **User-friendly**: [Scripts](https://greghib.github.io/void/docs/scripts) let you add content without touching engine internals.
* **Lightweight**: Memory usage under 300MB in low-memory mode.
* **Customisable**: Personalise your gameplay by tweaking settings in the `game.properties` file or modifying the code to suit your preferences.

## Quick setup

1. Make sure you have [java 21 or above installed](https://adoptium.net/temurin/releases/?package=jre). (`java --version` on command line)
2. Extract the [latest void.zip released bundle](https://github.com/GregHib/void/releases) into a directory.
3. Extract the [latest pre-modified game files cache.zip](https://mega.nz/folder/ZMN2AQaZ#4rJgfzbVW0_mWsr1oPLh1A) into `/data/cache/`.
4. Run either the `run-server.bat` on Windows, or `run-server.sh` on Linux.

   You should see `[Main] - Void loaded in ...ms` to show the server is up and running correctly.

   > Ctrl + C to exit and shutdown the server when running from a command line terminal.

5. Run the [latest client.jar](https://github.com/GregHib/void-client/releases) and login with any username and password to create a new account.

## Webclient setup (experimental)

1. Set `web.server.enabled` to `true` in `game.properties`.
2. Run the server as normal [^](#quick-setup).
3. Open `http://localhost:8080/play` in the browser and login with any username or password to create a new account.

## Website api

The webserver can also expose account routes so a website's backend can create and manage accounts on the running server, without a restart or account reload.
They're protected by a single shared key, so they're for trusted server-side callers only, never a static site or browser.

1. Set `web.server.enabled=true` and `web.api.enabled=true` in `game.properties`.
2. Set `web.api.token` to a random secret of at least 32 characters (e.g. `openssl rand -hex 32`). Every request must send it as `Authorization: Bearer <token>`.
3. Block `/api/v1/accounts` on any public reverse proxy so only your backend can reach it.

| Method |                Path                |                 Body                 |                         Success                         |
|--------|------------------------------------|--------------------------------------|---------------------------------------------------------|
| `GET`  | `/api/v1/names/{name}`             |                                      | `{"name","available"}`                                  |
| `GET`  | `/api/v1/accounts/{name}`          |                                      | `{"accountName","displayName","previousName","online"}` |
| `POST` | `/api/v1/accounts`                 | `{"name","password","displayName"?}` | `201` + account                                         |
| `PUT`  | `/api/v1/accounts/{name}/password` | `{"password"}`                       | `204`                                                   |
| `PUT`  | `/api/v1/accounts/{name}/name`     | `{"displayName"}`                    | account                                                 |

When creating, `name` is either a 1-12 character username (which is also the display name) or an email address, in which case `displayName` is required.
`{name}` in an `/accounts` path is always the account name, never a display name. Display names can't match another account's name, and `/names/{name}` checks whether a name is free as either. Changing a password logs the player out if they're online, and staff accounts (any rights) can only be changed in-game.
Errors use the api's `{"error": {"code", "message", "details"?}}` envelope: `400` `bad_request`, `401` `unauthorized` (missing or wrong key), `403` `forbidden` (creation disabled or a staff account), `404` `account_not_found`, `409` `name_taken`/`account_busy`, `422` `validation_failed` (`details` names the `name`, `displayName` or `password` field), `429` `rate_limited` (with `Retry-After`), `503` `unavailable`.

Send the end user's address as `X-Forwarded-For` when creating accounts so `web.api.registration.maxPerIP` limits players rather than the caller:

```bash
curl -H 'Authorization: Bearer <token>' -H 'Content-Type: application/json' -H 'X-Forwarded-For: 203.0.113.9' \
  -d '{"name":"bob@example.com","password":"hunter22","displayName":"Bob"}' http://localhost:8080/api/v1/accounts
```

For more details see the full [Installation Guide](https://greghib.github.io/void/docs/installation-guide).

> [!NOTE]
> For common problems see the [Troubleshooting Guide](https://greghib.github.io/void/docs/Troubleshooting).

## Development

Use [IntelliJ](https://www.jetbrains.com/idea/download/) (which is free to use) to develop with Void.
See [the installation guide](https://www.jetbrains.com/help/idea/installation-guide.html) for more instructions.

Once opened the IDE click the `Clone Repository` button or `File | New | Project from version control... |` if in the full application.

Select `git` and enter the project URL (Found under the `<> Code` button on the [GitHub page](https://github.com/GregHib/void)).
- `git@github.com:GregHib/void.git` if you have [GitHub authentication setup](https://docs.github.com/en/authentication).
- `https://github.com/GregHib/void.git` if you don't have SSH authentication.

> [!NOTE]
> When git is not installed it will display an error and the option to "Download and install", click this and retry the previous step.
> Click "Trust Project" if also asked.

Press "clone" and after the download is complete the project will be opened for you.

Under `Project Structure... | Project` settings set `SDK` to JDK 21+ (download as needed) and let it index.

Run the following command in the terminal to set up Gradle or close and re-open the project to trigger IntelliJ's `Open as Gradle Project` popup.

```bash
./gradlew build -x test
```

Extract the [cache files](https://mega.nz/folder/ZMN2AQaZ#4rJgfzbVW0_mWsr1oPLh1A) into a new directory called `/cache/` inside of the `/data/` directory.

From here you can navigate in the left panel to `/game/src/main/kotlin/` (Or Ctrl/Cmd + N for class search) where you will find [Main.kt](./game/src/main/kotlin/Main.kt) which you should be able to right-click and run.

You can also run in the command line using the gradle wrapper.

```bash
./gradlew run
```

Once the server is up and running; download one of the [prebuilt client.jars](https://github.com/GregHib/void-client/releases) or set up the [void-client repository](https://github.com/GregHib/void-client/) and run to log into the game.

Remember to check out our [Contributing guidelines](./CONTRIBUTING.md) before submitting your first pull request!

Run spotless before commiting to ensure formatting is correct:

```bash
./gradlew spotlessApply
```

> [!TIP]
> There are instructions on how to [build your own client](https://greghib.github.io/void/docs/client-building) and [build your own cache](https://greghib.github.io/void/docs/cache-building)!

## Documentation

For further guides on how to write content please refer to the [Void Wiki](https://greghib.github.io/void/docs/).

## AI Policy

The vast majority of this project was written by hand. AI-assisted contributions are welcome but held to the same bar as any other submission:
keep changes small and focused, ensure they are well tested, and make sure the code follows the style of the surrounding codebase.
Low-effort or bulk-generated PRs will not be accepted.

## Bugs

If you run into any problems or find any bugs, please open a [GitHub Issue](https://github.com/GregHib/void/issues) describing the problem.

## Thanks to

* All contributors
* Kris - [osrs-docs](https://osrs-docs.com/)
* Ebp90
* Jarryd
* Tomm - [RSMod Pathfinder](https://github.com/rsmod/rsmod)
* Graham - [OpenRS2](https://archive.openrs2.org/)

