Both Players and NPCs have a [Variables](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/client/variable/Variables.kt) container for storing variables and information.

By default variables are only stored temporarily and will be lost/reset when the npc dies, player logs out, or the game restarts.

Variable names should use lower camel case. e.g. `"in_combat"` not `"inCombat"`

## Getters

Characters have [extension functions](https://kotlinlang.org/docs/extensions.html) to simplify accessing Variables

The following safe version will return `false` if the value is unset.

```kotlin
val burrow = giantMole["burrow", false]
```

> [!TIP]
> Providing a default is the recommended way of accessing values.

```kotlin
val burrow: Boolean = giantMole["burrow"]
```
> [!WARNING]
> This is the unsafe version and will throw an exception is the value hasn't previously been set and doesn't have a default listed in the [variables](#player-variables) config file.

## Setters
Setting a value is straight-forward, it's important to use descriptive names to make sure values don't conflict with other content.

```kotlin
giantMole["burrow"] = true
```

## Player variables

Variables which a player has often need to be sent to the client to change something or display the value. Client variables are split into one of 4 categories:

* varp - Player Variable
* varbit - Variable Bit (part of a varp)
* varc - Client Variable
* varcstr - Client String Variable

Any variables which need storing but don't have a known id should be stored in `*.vars.toml`

For more details read [Runelites page on the subject](https://github.com/runelite/runelite/wiki/VarPlayers,-VarBits,-and-VarClients)

These variables will need specifying in the relevant `.vars.toml` config file in [./data/](https://github.com/GregHib/void/tree/main/data/). Along with their id and type.

```toml
[cooks_assistant]     # The string name of the variable
id = 29               # The variable id to send to the cleint
persist = true        # Save the value on logout
format = "map"        # The format the value is stored in (might differ from the format sent to the client)
default = "unstarted" # The default value if left unset
values = { unstarted = 0, started = 1, completed = 2 } # Map for converting values before sending to client
```