[Wildcards](wildcards) let content scripts supply flexible ID patterns whilst having minimal runtime cost by aggressively caching lookups into a `wildcards.txt` file.


[Wildcards.kt](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/event/Wildcards.kt) stores a map for each [Wildcard.kt](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/event/Wildcard.kt) category containing the wildcard string and it's matching ids.

These are used by [event handlers](event-handlers) to resolve ids at call-time.

```kotlin
fun npcOperate(option: String, npc: String = "*", handler: suspend Player.(PlayerOnNPCInteract) -> Unit) {
    Wildcards.find(npc, Wildcard.Npc) { id ->
        playerNpc.getOrPut("$option:$id") { mutableListOf() }.add(handler)
    }
}
```

In order to reduce performance impact they are cached into a .txt file following the format
```txt
# category=hash
wildcard|id1,id2,id3
```

Where comparing the hash of all the ids at the time the file was saved to the current list allows for detecting if any of the ids in [config-files](config-files) have been updated and a full refresh of that category is needed.

## Background
I've made many iterations on this system and trying to get the balance between flexibility and performance has been [a tricky one](#751). The previous system was a trie which stored parameters as-is and resolved wildcards at call-time rather than startup, it was also complex and verbose to add events. This system is simpler and has no call-time costs but likely impacts startup times more (although that is hard to measure due to how spread-out calls within scripts are).

It however is more practical and easier to use than trying to manually obtain lists of ids, it also has the side-effect of encouraging ids to be named in a predictable manor.
