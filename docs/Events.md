All of Void's content is built using Events to transmit information between entities and content in [handlers](event-handlers).

Whilst conceptually it can be considered as a [Publish-Subscriber model](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern) since 2.3.0 Void does not have a concept of an Event class due to performance and overhead reasons. Instead on startup [scripts](scripts) store functions which are then called directly when "emitting" an event.

The simplest example would be the `Spawn` "event".

```kotlin
interface Spawn {
    fun playerSpawn(handler: Player.() -> Unit) {
        playerSpawns.add(handler)
    }
    
    companion object : AutoCloseable {
        private val playerSpawns = mutableListOf<(Player) -> Unit>()
        
        fun player(player: Player) {
            for (handler in playerSpawns) {
                handler(player)
            }
        }
    }
}
```

It registers it's subscription inside a script:

```kotlin
class HelloWorld : Spawn {
    init {
        playerSpawn {
            message("Welcome to Void.")
        }
    }
}
```

And can be emitted by calling: `Spawn.player(myPlayer)`.

Script interfaces are often added to [Script.kt](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/Script.kt#L28) to simplify imports and accessing them inside of scripts, although whilst not required all external events must still be `AutoCloseable` and be added to [Script.kt's `interfaces`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/Script.kt#L43) list.