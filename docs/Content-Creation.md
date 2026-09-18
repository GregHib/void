Void’s content system is designed to be approachable at every skill level. Whether you just want to tweak a few values or build full-blown mechanics, you can work entirely in simple text-based `.toml` [config files](config-files), lightweight [scripts](scripts), or a mix of both.

This page gives you a quick sense of what you can do and how to get started without being a programmer.

## Data Files

Almost all game data lives in `.toml` files located inside the `/data/` folder. TOML is just a `key = value` format, much like [Game Settings](game-settings).

These files control names, stats, behaviours, prices, spawn locations, etc.

You can modify existing entries or add your own without touching any code.
If you want to give a sword different stats or add a object new teleport, notepad is all you need.

## Scripts

Scripts unlock logic and behaviour: [conversations](dialogues), [interactions](interactions), [timed events](timers), mini-game rules, [complex NPC behaviour](combat), and anything that goes beyond static data.

A basic script can be just a few lines in a .kt file:

```kotlin
class Greeter : Script {
    init {
        npcOperate("Talk-to", "villager") {
            npc<Happy>("Welcome to the village!")
        }
    }
}
```

Scripts are normal Kotlin files inside the `content` module.
You don’t need much programming experience, find other scripts similar to what you want to add and use them as examples.
