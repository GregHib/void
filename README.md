<div align="center">
<h1>Void</h1>
<a href="https://github.com/GregHib/void">
  <img src="https://i.imgur.com/X0OdMTf.png" alt="void">
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
<a href="https://github.com/GregHib/void/wiki">Documentation</a> &nbsp;&bull;&nbsp;
<a href="https://github.com/GregHib/void/issues">Bugs</a> &nbsp;&bull;&nbsp;
<a href="https://greghib.github.io/void-map/">World Map</a>

![void in game picture](https://i.imgur.com/OZ317on.png)

</div>

## Features

* **High-performance**: Capable of supporting thousands of concurrent players.
* **Rich content**: Offers a wide range of emulated content.
* **AI player characters**: Introduced intelligent player bots to enhance immersion.
* **User-friendly**: Simplifies content creation with [kotlin scripting](https://github.com/GregHib/void/wiki/scripts) eliminating the need for complex integration.
* **Lightweight**: Memory usage under 300MB in low-memory mode.
* **Customisable**: Personalise your gameplay by tweaking settings in the `game.properties` file or modifying the code to suit your preferences.

## Quick setup

1. Make sure you have [java 19 or above installed](https://adoptium.net/temurin/releases/?package=jre). (`java --version` on command line)
2. Extract the [latest void.zip released bundle](https://github.com/GregHib/void/releases) into a directory.
3. Extract the [latest pre-modified game files cache.zip](https://mega.nz/folder/ZMN2AQaZ#4rJgfzbVW0_mWsr1oPLh1A) into `/void/data/cache/`.
4. Run either the `run-server.bat` on Windows, or `run-server.sh` on Linux.

   You should see `[Main] - Void loaded in 2349ms` to show the server is up and running correctly.

   > Ctrl + C to exit and shutdown the server when running from a command line terminal.

5. Run the [latest client.jar](https://github.com/GregHib/void-client/releases) and login with any username and password to create a new account.

> [!NOTE]
> For common problems see the [Troubleshooting Guide](https://github.com/GregHib/void/wiki/Troubleshooting).

## Development

It is recommended to use IntelliJ IDEA to develop with Void.
The community edition can be downloaded for free from the [jetbrains website.](https://www.jetbrains.com/idea/download/)
See [the installation guide](https://www.jetbrains.com/help/idea/installation-guide.html) for more instructions.

Once opened the IDE click the `Clone Repository` button or `File | New | Project from version control... |` if in the full application.

Selecting `git` version control and entering the void project URL Found under the `<> Code` button on the [GitHub page](https://github.com/GregHib/void).
- `git@github.com:GregHib/void.git` if you have [GitHub authentication setup](https://docs.github.com/en/authentication).
- `https://github.com/GregHib/void.git` if you don't have SSH authentication.

> [!NOTE]
> When git is not installed it will display an error and the option to "Download and install", click this and retry the previous step.
> Click "Trust Project" if also asked.

Press "clone" and after the download is complete the project will be opened for you.

Under `Project Structure... | Project` settings set `SDK` to JDk 19+ (download as needed) and let it index.

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

Don't forget to check out our [Contributing guidelines](./CONTRIBUTING.md) before submitting your first pull request!

Formatting is done with spotless but should align with IntelliJ's default formatting. To be certain run:

```bash
./gradlew spotlessApply
```

> [!TIP]
> There are instructions on how to [build your own client](https://github.com/GregHib/void/wiki/client-building) and [build your own cache](https://github.com/GregHib/void/wiki/cache-building)!

## Documentation

For further guides on how to write content please refer to the [Void Wiki](https://github.com/GregHib/void/wiki/).

## Bugs

If you run into any problems or find any bugs please report them by creating a [New Issue on our Issues Page](https://github.com/GregHib/void/issues) describing the problem, so it can be fixed.

## Thanks to

* All contributors
* Kris - [osrs-docs](https://osrs-docs.com/)
* Ebp90
* Jarryd
* Tomm - [RSMod Pathfinder](https://github.com/rsmod/rsmod)
* Graham - [OpenRS2](https://archive.openrs2.org/)

[![image](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_square.svg)](https://jb.gg/OpenSourceSupport)
