<div style="text-align: center;">
<h1>Void</h1>
<a href="https://github.com/GregHib/void">
  <img src="https://i.imgur.com/N8RhzRY.png" alt="void">
</a>

<p>Void is a lightweight, high-performance back-end game server emulating the 2011 MMORPG RuneScape (revision 634). It serves as a modern and user-friendly base for others to built-off of.</p>

<a href="#features">Features</a> &nbsp;&bull;&nbsp;
<a href="#installation">Installation</a> &nbsp;&bull;&nbsp;
<a href="https://rune-server.org/runescape-development/rs-503-client-and-server/projects/697260-void-634-a.html">Blog</a> &nbsp;&bull;&nbsp;
<a href="#documentation">Documentation</a> &nbsp;&bull;&nbsp;
<a href="#bugs">Bugs</a>
</div>

## Features

* Capable of handling thousands of concurrent players
* Wide range of emulated content
* AI driven player bots
* Developer friendly content creation
* Low memory usage

![void in game picture](https://i.imgur.com/OZ317on.png)

## Installation

### Prerequisites

* Installed Java Development Kit (JDK) 19 or above. https://adoptium.net/en-GB/
* Downloaded the latest `client.jar` from the [client release page](https://github.com/GregHib/void-client/releases).
* Downloaded pre-modified cache game files from [cache artifacts](https://mega.nz/folder/ZMN2AQaZ#4rJgfzbVW0_mWsr1oPLh1A).

> [!TIP]
> There are instructions on how to [build your own client](https://github.com/GregHib/void/wiki/client-building) and [build your own cache](https://github.com/GregHib/void/wiki/cache-building)!

### Download game code

> [!NOTE]
> Jar builds, game bundles and docker images are coming soon!

#### Git

Clone the repository using the GitHub url.

```bash
git clone git@github.com:GregHib/void.git
cd void
```

#### Manual download

Download from GitHub and extract the `void-main.zip`

![github code button menu](https://i.imgur.com/98TDsxX.png)

### Extract cache

Create a new folder `cache` inside the `/data/` directory and extract the cache files inside of it.
The files location should look like this: `/void/data/cache/main_file_cache.dat2`

### Play

To quickly launch the server to log in and play; open a terminal in the `void` directory and run the following command:

```bash
./gradlew run
```

> [!TIP]
> Ctrl + C to exit and shutdown the server when running from a command line terminal.

#### Common issues

To resolve the error

```bash
> Kotlin could not find the required JDK tools in the Java installation. Make sure Kotlin compilation is running on a JDK, not JRE.
```

Open the `gradle.properties` file, remove the hash from the first line and replace the directory with the location of
your JDK installation.

```properties
org.gradle.java.home=C:/Users/Greg/.jdks/openjdk-19.0.1/
```

### Development

It is recommended to use IntelliJ IDEA to develop with Void.
The community edition can be downloaded for free from the [jetbrains website.](https://www.jetbrains.com/idea/download/)
See [the installation guide](https://www.jetbrains.com/help/idea/installation-guide.html) for more instructions.

Once inside the IDE, you can create a new project by going to `File | New | Project from version control... |`

Selecting `git` version control and entering the void project URL `git@github.com:GregHib/void.git` found under the `<> Code` button on the [GitHub page](https://github.com/GregHib/void).

Press clone and after a little while the project will be opened for you, the JDK indexed and gradle setup.

From here you can navigate in the left panel to `/game/src/main/kotlin/world/gregs/voidps/` where you will find [Main.kt](./game/src/main/kotlin/world/gregs/voidps/Main.kt) which you should be able to right-click and run.

> [!NOTE]
> See the [Troubleshooting Guide](https://github.com/GregHib/void/wiki/Troubleshooting) for further problems

## Documentation

For further guides on how to write content please refer to the [Void Wiki](https://github.com/GregHib/void/wiki/).

## Bugs

If you run into any problems or find any bugs please report them by creating a [New Issue on our Issues Page](https://github.com/GregHib/void/issues) describing the problem, so it can be fixed.