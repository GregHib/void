# Testing

Content tests extend `WorldTest` (`game/src/test/kotlin/WorldTest.kt`): boots full Koin context, loads all definitions, instantiates all scripts, resets world state between tests.

Helpers: `createPlayer()`, `createNPC()`, `createObject()`, `createFloorItem()`, `tick()`, `tickIf()`.

`WorldTest` is expensive (`@TestInstance(PER_CLASS)`). Use it only for integration-level content tests. Engine unit tests use plain JUnit 5.
