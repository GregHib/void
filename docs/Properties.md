Properties are [Game Settings](game-settings) used to configure and customise a world by a server operator in the [`game.properties`](https://github.com/GregHib/void/blob/main/game/src/main/resources/game.properties) file.

## What is a property

Use properties for:
- Server-wide behaviour (xp rates, movement rules)
- Startup-configuration (network & storage settings)
- Paths and external file locations
- Feature toggles

## What isn't a property

Not everything should be a property however.

Avoid properties for:
- Per Item/Npc/Object values - These should be stored in [config files](config-files)

## Accessing Properties

You can access properties in [scripts](scripts) using the `Settings[...]` operator:

```kotlin
val x = Settings["world.home.x", 0]
```

The second argument is the default value if the property is missing or invalid.

You can access booleans, numbers, and strings the same way:

```kotlin
val membersOnly = Settings["world.members", false]
val xpRate = Settings["world.experienceRate", 1.0]
val serverName = Settings["server.name", "Server"]
```

## Example

```kotlin
worldSpawn {
    if (Settings["events.shootingStars.enabled", false]) {
        eventUpdate()
    }
}
```

## Notes
- Properties are global and apply to the whole server.
- Reload properties with the `reload settings` [command](commands).
- `settingsReload {}` [event handler](event-handlers) to listen for reloads.