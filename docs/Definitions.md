Definitions are collections of data stored in the game Cache, they often store data about a specific [Entity](entities), [Interface](interfaces) or other game engine system.

> [!TIP]
> You can find the full list of definitions in [`./engine/data/definition/`](https://github.com/GregHib/void/tree/main/engine/src/main/kotlin/world/gregs/voidps/engine/data/definition)

Definitions can be accessed from anywhere using the format:

```kotlin
ItemDefinitions.get(itemId)
```

Or in functions using the old injection method:

```kotlin
val itemDefinitions: ItemDefinitions = get()
```


## Definition Extras

Void goes beyond standard definitions and links [TOML](https://toml.io/en/) [configuration files](config-files) to each definition making it easy to define [String Identifiers](string-identifiers) and additional data.


Anything added to an npc beyond the integer id is added to the respective [`definitions.extras`](https://github.com/GregHib/void/blob/main/cache/src/main/kotlin/world/gregs/voidps/cache/definition/Extra.kt) map where they can later be accessed in a similar way to [Variables](character-variables).

```toml
[hans]
id = 0
wander_radius = 4
categories = ["human"]
examine = "Servant of the Duke of Lumbridge."
```

```kotlin
val race = npc.def["race", ""] // Get or default return empty string

val examine: String = npc.def["examine"] // Get unsafe with type

val radius: Int? = npc.def.getOrNull("wander_radius") // Get if exists else return null value
```

> [!NOTE]
> This applies to all definitions, `.objs.toml` -> ObjectDefinition, `.ifaces.toml` -. InterfaceDefinition, `fonts.toml` -> FontDefinition, etc...
