# Contributing a quest: Vampire Slayer

A quest is ordinary server content built on the existing engine APIs.
Vampire Slayer provides a compact example of dialogue, persistent state,
world objects, a personal boss, a journal and a one-time reward.

## Files to work on

- `game/src/main/kotlin/content/quest/free/vampire_slayer/`: focused scripts for
  Morgan, the garlic drawers, the coffin, Count Draynor and the quest journal.
  Each script registers handlers in `init` and keeps its behavior in private
  member methods. The journal and boss share the tools check through the
  Count's companion object.
- `game/src/main/kotlin/content/area/misthalin/varrock/blue_moon_inn/DrHarlow.kt`:
  owns both quest advice and his ordinary drinking conversation.
- `data/quest/free/vampyre_slayer/`: existing item/NPC definitions plus the
  quest's varbit, garlic drawers and Count combat definition. The data folder
  keeps its existing spelling; the canonical state key is `vampire_slayer`.
- `data/quest/quests.toml`: quest identity, title, requirements and reward
  metadata. Metadata alone does not implement a quest.
- `content/quest/Quest.kt`: add the quest's canonical key to the implementation
  registry when its playable flow is ready.
- `game/src/test/kotlin/content/quest/free/vampire_slayer/VampireSlayerTest.kt`:
  tests exercise normal NPC/object instructions against the prepared cache.

Classes implementing `Script` are discovered by the build's generated script
metadata. Keep `init` limited to handler registration; put behavior in private
member methods and use companion helpers only where the same content shares
logic. Do not construct scripts during gameplay. Follow the guidelines linked
from the repository AGENTS.md.
Run a normal build and deploy the resulting server and data together.

## Design the state machine first

Revision 634 uses varbit 6914, bits 0-1 of varp 178, for this quest. Its journal
client script considers value 3 complete. The implemented named states are:

| State | Wire value | Meaning |
| --- | --- | --- |
| `unstarted` | 0 | Morgan has not accepted your help. |
| `started` | 1 | Find Harlow and bring him a beer. |
| `armed` | 2 | Harlow has explained the stake and hammer; fight the Count. |
| `completed` | 3 | Count defeated; reward already awarded. |

Persist named states in the quest variable. Advance only after the relevant
conversation or action finishes. Check the current state before granting a
reward, and change it before queuing the completion screen. Here the reward
is **4,825 Attack XP and 3 quest points**, with the normal server XP multiplier
applied by `exp`. There is no combat XP lamp in this revision.

## Verify IDs and placements against the target cache

A reference implementation is useful for behavior, but its numeric IDs and
state encoding are not portable. This implementation uses:

| Entity | Revision-634 definition / placement |
| --- | --- |
| Morgan | NPC 755, Draynor Village |
| Count Draynor | NPC 9356 (`count_draynor_normal`) |
| Garlic drawers | Object 46250 at `(3096,3269,1)` |
| Coffin | Object 158 at `(3080,9785,0)` |
| Manor stairs down | Object 47643; existing Draynor teleport definition |
| Cellar stairs up | Object 164; existing teleport definition |
| Stake | Item 1549, carried or equipped |
| Stake hammer | Item 15417, carried or equipped |
| Ordinary hammer | Item 2347, also accepted for staking |
| Garlic | Item 1550 |

In particular, the 2009 Count NPC ID 757 is **Prince Brand** in this cache.
Use both cached definitions and actual map placements; an old definition
existing in the cache does not mean it is still placed in the world. Scope
handlers by placement when an object ID is reused in multiple locations.

## Handle failure paths as part of the quest

Harlow checks inventory capacity before taking beer. Banked/equipped tools
prevent duplicate replacements, and lost tools can be replaced without paying
again. Garlic refuses a full inventory without advancing anything.

The Count is owned by the player who opens the coffin. Normal combat ownership
prevents another player attacking that Count. Reopening the coffin does not
create another boss. Leaving, dying or logging out removes it; it never
respawns as an ordinary world NPC. The lethal-hit check examines the player's
current tools, including equipped items, so losing a tool during combat cannot
bypass the staking requirement. Defeating him consumes the stake and retains
the hammer. Garlic suppresses his regeneration.

Reuse ordinary dialogue and combat APIs instead of making new packet handlers.
Keep generic NPC dialogue available outside the quest. Use the server's
existing quest completion UI and journal helpers.

## Test and review

With a full Java 21 JDK and the prepared/decrypted revision-634 cache at
`data/cache`, run:

```sh
./gradlew :game:test --tests 'content.quest.free.vampire_slayer.*Test'
```

Test acceptance and refusal, missing payment, inventory capacity, lost and
banked equipment, missing combat tools, reward amounts, repeat interactions,
other players and boss cleanup. The world test changes no live accounts and
starts no game listener. Check relevant existing quests as regressions too.
Then do a browser playthrough on an isolated development server to verify
presentation, movement, animations and dialogue pacing before calling the
quest an exact visual recreation.

This implementation provides the playable quest flow with concise dialogue.
It does not recreate the revision-634 coffin-opening cinematic or guarantee
word-for-word historical dialogue. These remain presentation work; the world
tests do not establish visual fidelity.

## Behavioral reference and licensing

The 2009scape implementation was inspected at commit
`384946b48029142904b6c3feb0ee7e69b5f4cf33`, particularly the files under
`Server/src/main/content/region/misthalin/draynor/quest/vampire/` and Morgan / Dr
Harlow dialogue files. Its AGPL-3.0 licensing differs from this server's BSD
license. This contribution is written using Void's existing APIs and does not
copy or mechanically translate those Java files. The reference was used to
check the sequence and edge cases; IDs and wire states come from our cache.

Useful historical checks:

- [Vampire Slayer guide, updated June 2010](https://www.global-rs.com/quests/vampireslayer/)
- [RuneScape quest history](https://runescape.wiki/w/Vampyre_Slayer)
- [2009scape source](https://gitlab.com/2009scape/2009scape)

Keep revision differences and uncertainties explicit in review. A successful
reference quest is not evidence that all its IDs or current wiki rewards
belong to 2011.
