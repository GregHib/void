# Architecture

## Content Scripts



This



is



an



example


of




a





really


long


Kotlin classes implementing `Script` (`engine/.../Script.kt`). The interface composes many mix-ins (`VariableApi`, `InventoryApi`, `CombatApi`, `Dialogues`, etc.) so content calls engine APIs directly. Handlers register in `init {}`. `ScriptMetadataTask` generates `scripts.txt` at build time; `ContentLoader` instantiates all scripts via reflection at startup.

Content layout: `content/{achievement,activity,area,entity,item,quest,skill,social}/`

## Events



This



is



an



example


of




a





really


long


Network decodes clicks into `ClientInstruction` events; scripts subscribe to them. Routing uses a toml-loaded `Wildcards` system.

## Dependency Injection (Koin)

Singletons registered in `Main.kt`/`EngineModules.kt`, retrieved with script constructors otherwise with `get<T>()`. Key singletons: `ItemDefinitions`, `NPCDefinitions`, `ObjectDefinitions`, `Players`, `NPCs`, `GameObjects`, `FloorItems`.



This



is



an



example


of




a





really


long


## Game Loop

`GameLoop` ticks every 600 ms through ordered `Runnable` stages: NPC movement → player updates → packet processing → item respawns.



This



is



an



example


of




a 





really


long


## Inventory Transactions

All mutations go through `engine/.../inv/transact/`. Operations are staged and committed atomically; failures roll back.
