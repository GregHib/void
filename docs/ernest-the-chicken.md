# Ernest the Chicken (revision 634)

This adds the playable quest to the base game server without SDK-specific
handlers. The SDK reads its ordinary varp and uses normal game interactions.

## Flow and reward

Veronica starts the search. Oddenstein explains the transformation and asks
for a pressure gauge, rubber tube and oil can. Repairing the machine restores
Ernest and awards **4 quest points, 3,000 coins, 10 noted eggs and 300 feathers**.
The three non-stackable parts are exchanged atomically for three reward stacks,
so even a full inventory has room. Missing parts and a failed transaction do
not advance the quest. Completion is checked before payment to prevent repeats.

Sources checked for this revision:

- [Archived 2011 quest information](https://www.2011.rs/kb/ernest_the_chicken)
  lists all four rewards.
- [Sal's quest guide](https://runescape.salmoneus.net/quests/ernest-the-chicken.html)
  specifies that the ten eggs are noted.
- [2012 quick guide](https://wiki.darkan.org/Ernest_the_Chicken/Quick_guide)
  documents the six-lever route and contemporary manor layout.

Dialogue is concise original text. The transformation uses the existing NPC
transform mechanism and a temporary actor cleaned up when the conversation
ends. This is not a frame-for-frame reconstruction of the original machine
cinematic. Browser playtesting is still needed for presentation and pacing.

## Revision-specific definitions

Quest varp 32: `unstarted=0`, `started=1`, `parts=2`, `completed=3`.
Lever varp 33: bits 1–6 select the raised forms of levers A–F.
Door varp 668: bits 0–8 select the unlocked forms of placed objects 137–145.
Packed values and individual selectors are synchronized for server validation
and client rendering, and reconstructed from the saved lever mask on login.
The unused `dungeon_gatestone` placeholder previously claimed varbit 1795;
its name and values are preserved as a custom variable so it no longer writes
an Ernest door bit.

Each character retains their own switches and door forms. Cellar transit
resets that character's puzzle. No lever click changes another player's doors.

| Object | Placed ID | Location |
| --- | --- | --- |
| Fountain | 153 | 3087,3334,0 |
| Compost | 152 | 3084,3360,0 |
| Tube-room door | 131; opened 11445 | 3107,3368,0 |
| Candle sconce | 47404 | 3097,3360,0 |
| Secret-room exit lever | 160 | 3096,3357,0 |
| Cellar ladder down | 133 | 3092,3362,0 |
| Cellar ladder up | 32015 | 3117,9754,0 |
| Levers A–F | 146–151 | Cellar |
| Puzzle doors | 137–145 | Cellar |

The engine dispatches operations using the **resolved** child definition.
Handlers therefore register both the placed and transformed lever/door names.
The door mask selects menu availability as well as the visible form; an Open
operation independently checks the switch conditions before passing through.
Personal gates use forced short movement without globally removing collision.
Secret-room passage similarly uses a short forced path, without reconstructing
the original moving-wall animation.

The tube door uses the existing door definition. Its key unlock is tracked per
character; tube pickup checks that unlock and rejects Telekinetic Grab. Oil-can
pickup also rejects Telekinetic Grab and requires being inside the final room.
The existing oil-can spawn remains in its original data file, alongside the
existing tube, poison, fish-food and spade spawns. No unrelated spawn data was
removed or replaced.

Poison and fish food combine in either order. Poisoned food kills the fountain's
piranhas for that character. Searching beforehand inflicts 10 life points;
searching afterwards gives a gauge if there is room and no existing carried or
banked gauge. Lost gauges and keys can be replaced. Compost requires a spade
and supports Search, spade-on-heap and Dig nearby.

## Puzzle provenance

The gate-condition logic is adapted from the MIT-licensed Lost City quest
script in the local rs-sdk reference repository, under
`server/content/scripts/quests/quest_haunted/scripts/quest_haunted.rs2`.
Its copyright and permission notice is retained in `LICENSES/lost-city.txt`.
The handler structure, 634 object IDs, transforms, state encoding and other
quest content use this server's APIs and cache. The 2009scape implementation
was inspected as a behavioral reference, but its Java code was not copied or
mechanically translated.

## Validation and deployment

Run the world tests with Java 21 and the prepared revision-634 cache:

```sh
./gradlew :game:test --tests 'content.quest.free.ernest_the_chicken.*Test'
```

They exercise acceptance/refusal, item prerequisites, inventory capacity,
personal flags, the full lever route, cellar and secret-room transit,
Telekinetic Grab restrictions, completion and noted reward stacks.
Build and deploy server code and its data together. No changes to live account
saves are required.
