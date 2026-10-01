Scripts are the backbone of content in Void they allow you to easily attach code to events that occur within the game engine.
A script is any Kotlin class within the `game` module that implements the `Script` interface.

Anything registered in `init` is called when the server starts.

## Example

```kotlin
class Cows : Script {
    init {
        npcSpawn("cow") {
            softTimers.start("eat_grass")
        }
    }
}
```

> [!NOTE]
> See [Event Handlers](event-handlers) to learn about `npcSpawn` and other ways to write content.

## Creating a script

In IntelliJ right click the directory where you'd like to place to create a kotlin file (Shortcut: Alt + Insert)

![Screenshot 2024-02-21 204031](https://github.com/GregHib/void/assets/5911414/a15c451c-0071-4c22-b650-aa886f6ceb45)

Enter the class name and select the `Class` option

<img width="343" height="276" alt="image" src="https://github.com/user-attachments/assets/83dbb4ef-dccc-4353-a16a-23fcc4f7cab6" />

A class will be created:

<img width="750" height="226" alt="image" src="https://github.com/user-attachments/assets/f117eacb-80f9-41c6-8f0f-5c2ca68e2b6f" />

Now you can extend the `Script` interface and add an init function

<img width="436" height="247" alt="image" src="https://github.com/user-attachments/assets/4dd407e1-516c-4cf2-b33f-adc6f9a03214" />


Now you can write your content using event handlers // TODO

That's it! Scripts are discovered automatically next time your run the server.

**Next up**: [adding event handlers](event-handlers)

### Notes
- Keep scripts focused to one [entity](entity) or one behaviour if an entity has a lot of options
- Make sure scripts are located in the `content.` package (unless otherwise specified in [properties](properties))
- Scripts are organised by location first, by type second


## Troubleshooting

It's possible script detection fails and a deleted or moved script can't be found. If this occurs you'll see a startup error like this:

```txt
ERROR [ContentLoader] Failed to load script: content.area.misthalin.lumbridge.farm.Cow
java.lang.ClassNotFoundException: content.area.misthalin.lumbridge.farm.Cow
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:525)
	at java.base/java.lang.Class.forName0(Native Method)
	at java.base/java.lang.Class.forName(Class.java:413)
	at java.base/java.lang.Class.forName(Class.java:404)
	at ContentLoader.loadScript(ContentLoader.kt:54)
	at ContentLoader.load(ContentLoader.kt:26)
	at Main.preload(Main.kt:102)
	at Main.main(Main.kt:56)
ERROR [ContentLoader] If the file exists make sure the scripts package is correct.
ERROR [ContentLoader] If the file has been deleted try running 'gradle cleanScriptMetadata'.
ERROR [ContentLoader] Otherwise make sure the return type is written explicitly.
```

You can fix this by running
```gradle
gradle collectSourcePaths
```

```bash
./gradlew collectSourcePaths
```

Or manually deleting the `scripts.txt` file in `game/src/main/resources/`