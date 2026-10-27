# Testing

Content tests extend `WorldTest` (`game/src/test/kotlin/WorldTest.kt`): boots full Koin context, loads all definitions, instantiates all scripts, resets world state between tests.

Helpers: `createPlayer()`, `createNPC()`, `createObject()`, `createFloorItem()`, `tick()`, `tickIf()`.

`WorldTest` is expensive (`@TestInstance(PER_CLASS)`). Use it only for integration-level content tests. Engine unit tests use plain JUnit 5.

## Dialogue

Don't write your own dialogue-skipping helpers or loops; use the existing ones in `game/src/test/kotlin/InstructionCalls.kt`:

- `player.skipDialogues()` — continues through every consecutive continuable dialogue (chat, message, object) until a choice or the end is reached.
- `player.dialogueOption(2)` — selects option 2 of a choice menu (`dialogueOption("line2")`).

```kotlin
player.npcOption(npc, "Talk-to")
tick(1) // Dialogues may not open on same tick as interaction
player.skipDialogues()
player.dialogueOption(2)
player.skipDialogues()
```

