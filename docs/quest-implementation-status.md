# Quest implementation status

## Scope

This inventory compares the quest definitions in `data/quest/quests.toml` with
quest scripts in `game/src/main/kotlin/content`. The repository is configured
for **revision 634**, a 2011 RuneScape revision rather than the current OSRS
client. The quest data file also says that its definitions were dumped from
RS3 and need updating for the current revision, so this is a repository
inventory, not a claim that every entry belongs to modern OSRS.

Quest status is based on a quest script registering the corresponding quest
state/journal. Supporting scripts (NPCs, objects, or rewards) do not count as
a complete quest implementation.

## Summary

| Status | Count |
| --- | ---: |
| Complete quest flow | 30 |
| Missing a complete quest flow | 165 |
| Quest definitions | 195 |

## Complete quest flows

- ✅ Biohazard
- ✅ The Blood Pact
- ✅ Cook's Assistant
- ✅ Creature of Fenkenstrain
- ✅ Death Plateau
- ✅ Demon Slayer
- ✅ Doric's Quest
- ✅ Druidic Ritual
- ✅ Dwarf Cannon
- ✅ A Fairy Tale II - Cure a Queen
- ✅ The Fremennik Trials
- ✅ Gertrude's Cat
- ✅ Ghosts Ahoy
- ✅ Gunnar's Ground
- ✅ Imp Catcher
- ✅ Jungle Potion
- ✅ The Knight's Sword
- ✅ Lost City
- ✅ Monkey Madness
- ✅ Nature Spirit
- ✅ Perils of Ice Mountain
- ✅ Plague City
- ✅ Priest in Peril
- ✅ Prince Ali Rescue
- ✅ The Restless Ghost
- ✅ Rune Mysteries
- ✅ Sheep Shearer (miniquest)
- ✅ Tears of Guthix
- ✅ The Tourist Trap
- ✅ Zogre Flesh Eaters

## Missing quest flows

- ⬜ All Fired Up
- ⬜ Animal Magnetism
- ⬜ Another Slice of H.A.M.
- ⬜ As a First Resort
- ⬜ Back to my Roots
- ⬜ Bar Crawl (miniquest)
- ⬜ Between a Rock...
- ⬜ Big Chompy Bird Hunting
- ⬜ Black Knights' Fortress
- ⬜ Blood Runs Deep
- ⬜ Buyers and Cellars
- ⬜ Cabin Fever
- ⬜ Catapult Construction
- ⬜ The Chosen Commander
- ⬜ Clock Tower
- ⬜ Cold War
- ⬜ Contact!
- ⬜ The Curse of Arrav
- ⬜ The Curse of Zaros (miniquest)
- ⬜ The Darkness of Hallowvale
- ⬜ Dealing with Scabaras
- ⬜ Death to the Dorgeshuun
- ⬜ Defender of Varrock
- ⬜ Desert Slayer Dungeon (miniquest)
- ⬜ Desert Treasure
- ⬜ Devious Minds
- ⬜ The Dig Site
- ⬜ Do No Evil
- ⬜ Dragon Slayer
- ⬜ Dream Mentor
- ⬜ Eadgar's Ruse
- ⬜ Eagles' Peak
- ⬜ Elemental Workshop I
- ⬜ Elemental Workshop II
- ⬜ Elemental Workshop III
- ⬜ Enakhra's Lament
- ⬜ Enlightened Journey
- ⬜ Enter the Abyss (miniquest)
- ⬜ Ernest the Chicken
- ⬜ The Eyes of Glouphrie
- ⬜ A Fairy Tale I - Growing Pains
- ⬜ A Fairy Tale III - Battle at Ork's Rift
- ⬜ Family Crest
- ⬜ The Feud
- ⬜ Fight Arena
- ⬜ Fishing Contest
- ⬜ Forgettable Tale of a Drunken Dwarf
- ⬜ Forgiveness of a Chaos Dwarf
- ⬜ The Fremennik Isles
- ⬜ From Tiny Acorns (miniquest)
- ⬜ Fur 'n Seek
- ⬜ Garden of Tranquillity
- ⬜ The General's Shadow (miniquest)
- ⬜ The Giant Dwarf
- ⬜ Glorious Memories
- ⬜ Goblin Diplomacy
- ⬜ The Golem
- ⬜ The Grand Tree
- ⬜ The Great Brain Robbery
- ⬜ Grim Tales
- ⬜ A Guild of Our Own (miniquest)
- ⬜ The Hand in the Sand
- ⬜ Haunted Mine
- ⬜ Hazeel Cult
- ⬜ Heroes' Quest
- ⬜ Holy Grail
- ⬜ Hopespear's Will (miniquest)
- ⬜ Horror from the Deep
- ⬜ Hunt for Red Raktuber
- ⬜ The Hunt for Surok (miniquest)
- ⬜ Icthlarin's Little Helper
- ⬜ In Aid of the Myreque
- ⬜ In Pyre Need
- ⬜ In Search of the Myreque
- ⬜ Kennith's Concerns
- ⬜ King of the Dwarves
- ⬜ King's Ransom
- ⬜ Lair of Tarn Razorlor (miniquest)
- ⬜ Land of the Goblins
- ⬜ Legacy of Seergaze
- ⬜ Legends' Quest
- ⬜ Lost Her Marbles (miniquest)
- ⬜ The Lost Tribe
- ⬜ Love Story
- ⬜ Lunar Diplomacy
- ⬜ Making History
- ⬜ Meeting History
- ⬜ Merlin's Crystal
- ⬜ Missing My Mummy
- ⬜ Monk's Friend
- ⬜ Mountain Daughter
- ⬜ Mourning's End Part I
- ⬜ Mourning's End Part II
- ⬜ Murder Mystery
- ⬜ My Arm's Big Adventure
- ⬜ Myths of the White Lands
- ⬜ Nomad's Requiem
- ⬜ Observatory Quest
- ⬜ Olaf's Quest
- ⬜ One Foot in the Grave (miniquest)
- ⬜ One Small Favour
- ⬜ The Path of Glouphrie
- ⬜ Pirate's Treasure
- ⬜ Purple Cat (miniquest)
- ⬜ Quiet Before the Swarm
- ⬜ Rag and Bone Man
- ⬜ Rat Catchers
- ⬜ Recipe for Disaster
- ⬜ Recipe for Disaster: Another Cook's Quest
- ⬜ Recipe for Disaster: Defeating the Culinaromancer
- ⬜ Recipe for Disaster: Freeing Evil Dave
- ⬜ Recipe for Disaster: Freeing King Awowogei
- ⬜ Recipe for Disaster: Freeing Pirate Pete
- ⬜ Recipe for Disaster: Freeing Sir Amik Varze
- ⬜ Recipe for Disaster: Freeing Skrach Uglogwee
- ⬜ Recipe for Disaster: Freeing the Goblin Generals
- ⬜ Recipe for Disaster: Freeing the Lumbridge Sage
- ⬜ Recipe for Disaster: Freeing the Mountain Dwarf
- ⬜ Recruitment Drive
- ⬜ Regicide
- ⬜ Rocking Out
- ⬜ Roving Elves
- ⬜ Royal Trouble
- ⬜ Rum Deal
- ⬜ Rune Mechanics
- ⬜ Scorpion Catcher
- ⬜ Sea Slug
- ⬜ Shades of Mort'ton
- ⬜ Shadow of the Storm
- ⬜ Sheep Herder
- ⬜ Shield of Arrav
- ⬜ Shilo Village
- ⬜ The Slug Menace
- ⬜ Smoking Kills
- ⬜ A Soul's Bane
- ⬜ Spirit of Summer
- ⬜ Spirits of the Elid
- ⬜ Summer's End
- ⬜ Swan Song
- ⬜ Swept Away
- ⬜ Tai Bwo Wannai Trio
- ⬜ A Tail of Two Cats
- ⬜ The Tale of the Muspah
- ⬜ The Temple at Senntisten
- ⬜ Temple of Ikov
- ⬜ Throne of Miscellania
- ⬜ TokTz-Ket-Dill
- ⬜ Tower of Life
- ⬜ Tree Gnome Village
- ⬜ Tribal Totem
- ⬜ Troll Romance
- ⬜ Troll Stronghold
- ⬜ Underground Pass
- ⬜ Vampire Slayer
- ⬜ A Void Dance
- ⬜ The Void Stares Back
- ⬜ Wanted!
- ⬜ Watchtower
- ⬜ Waterfall Quest
- ⬜ What Lies Below
- ⬜ While Guthix Sleeps
- ⬜ Witch's House
- ⬜ Witch's Potion (miniquest)
- ⬜ Within the Light
- ⬜ Wolf Whistle

## Suggested next implementation

**All Fired Up** is a clear next target: it has a quest definition and
requirements in `data/quest/quests.toml`, but no quest-flow script. Implement
its start dialogue, beacon sequence, requirements, completion state, rewards,
and quest journal before marking it complete.

