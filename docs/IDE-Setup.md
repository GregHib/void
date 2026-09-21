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
> There are instructions on how to [build your own client](client-building) and [build your own cache](cache-building)!
