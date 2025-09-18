package content.quest.free.gunnars_ground

import content.entity.player.inv.inventoryItem
import content.entity.player.modal.tab.questJournalOpen
import content.quest.letterScroll
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.holdsItem

@Script
class GunnarsGround : Api {

    override fun spawn(player: Player) {
        player.sendVariable("gudrun_after_quest")
        player.sendVariable("dororan_after_quest")
        player.sendVariable("kjell")
        player.sendVariable("dororan")
        player.sendVariable("dororan_after_cutscene")
        player.sendVariable("gudrun")
        player.sendVariable("gudrun_after_cutscene")
        player.sendVariable("dororan_ruby_bracelet")
        player.sendVariable("dororan_dragonstone_necklace")
        player.sendVariable("dororan_onyx_amulet")
    }

    init {
        inventoryItem("Read", "gunnars_ground", "inventory") {
            player.letterScroll(
                "Gunnar's Ground",
                listOf(
                    "",
                    "Our people dwelt on mountains steeped in lore,",
                    "A mighty tribe as harsh as any beast",
                    "Who then, in face of madness swept to war,",
                    "The warlord Gunnar leading to the east.",
                    "",
                    "This legacy of honour still lives on",
                    "In Gunnar's bloodline, fierce to this day.",
                    "We sing the tales of battles long since won",
                    "And from his righteous purpose never stray.",
                    "",
                    "But long is gone the author of that threat",
                    "And even rolling boulders come to rest,",
                    "For Gunnar's ground is rich and fruitful yet",
                    "And Gunnar's blood with beauty blessed.",
                    "",
                    "Now let these freemen from this conflict cease",
                    "And let this be the time of Gunthor's peace.",
                ),
            )
        }

        questJournalOpen("gunnars_ground") {
            val lines = when (player.quest("gunnars_ground")) {
                "completed" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<str>Dororan wants me to take the engraved ring to Gudrun",
                    "<str>in the barbarian village",
                    "<str>He asked that I not reveal that he is a dwarf just yet.",
                    "<str>Gudrun says that her father, Gunthor, would never allow her to",
                    "<str>have a relationship with an outsider. She wondered if I could",
                    "<str>convince him otherwise. He can be found at the longhouse in the",
                    "<str>barbarian village.",
                    "<str>Gunthor was not accommodating. He told me to ask Gudrun",
                    "<str>in the barbarian village what someone called Gunnar would think.",
                    "<str>Gudrun suggested I see if Dororan, outside the",
                    "<str>barbarian village, can think of anything to help convince Gunthor.",
                    "<str>Dororan, outside the barbarian village, is going to try to write",
                    "<str>a poem to convince Gunthor. I should check how he's getting on.",
                    "<str>I need to take the poem to Gudrun in the barbarian village",
                    "<str>and ask her to read it to her father.",
                    "<str>I should talk to Gudrun or Dororan in the",
                    "<str>barbarian village about how the recital went.",
                    "<str>Dororan has moved into a large house, east of the",
                    "<str>barbarian village and across the river. The house is north",
                    "<str>of the road and has roses outside",
                    "",
                    "<red>QUEST COMPLETE!",
                )
                "gunnars_ground" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<str>Dororan wants me to take the engraved ring to Gudrun",
                    "<str>in the barbarian village",
                    "<str>He asked that I not reveal that he is a dwarf just yet.",
                    "<str>Gudrun says that her father, Gunthor, would never allow her to",
                    "<str>have a relationship with an outsider. She wondered if I could",
                    "<str>convince him otherwise. He can be found at the longhouse in the",
                    "<str>barbarian village.",
                    "<str>Gunthor was not accommodating. He told me to ask Gudrun",
                    "<str>in the barbarian village what someone called Gunnar would think.",
                    "<str>Gudrun suggested I see if Dororan, outside the",
                    "<str>barbarian village, can think of anything to help convince Gunthor.",
                    "<str>Dororan, outside the barbarian village, is going to try to write",
                    "<str>a poem to convince Gunthor. I should check how he's getting on.",
                    "<str>I need to take the poem to Gudrun in the barbarian village",
                    "<str>and ask her to read it to her father.",
                    "<navy>I should talk to <maroon>Gudrun <navy>or <maroon>Dororan <navy>in the",
                    "<maroon>barbarian village <navy>about how the recital went.",
                )
                "recital" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<str>Dororan wants me to take the engraved ring to Gudrun",
                    "<str>in the barbarian village",
                    "<str>He asked that I not reveal that he is a dwarf just yet.",
                    "<str>Gudrun says that her father, Gunthor, would never allow her to",
                    "<str>have a relationship with an outsider. She wondered if I could",
                    "<str>convince him otherwise. He can be found at the longhouse in the",
                    "<str>barbarian village.",
                    "<str>Gunthor was not accommodating. He told me to ask Gudrun",
                    "<str>in the barbarian village what someone called Gunnar would think.",
                    "<str>Gudrun suggested I see if Dororan, outside the",
                    "<str>barbarian village, can think of anything to help convince Gunthor.",
                    "<str>Dororan, outside the barbarian village, is going to try to write",
                    "<str>a poem to convince Gunthor. I should check how he's getting on.",
                    "<str>I need to take the poem to Gudrun in the barbarian village",
                    "<str>and ask her to read it to her father.",
                    "<navy>I should talk to <maroon>Gudrun <navy>in the <maroon>barbarian village",
                    "<navy>once I'm ready for her to read the poem to her father.",
                )
                "poem" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<str>Dororan wants me to take the engraved ring to Gudrun",
                    "<str>in the barbarian village",
                    "<str>He asked that I not reveal that he is a dwarf just yet.",
                    "<str>Gudrun says that her father, Gunthor, would never allow her to",
                    "<str>have a relationship with an outsider. She wondered if I could",
                    "<str>convince him otherwise. He can be found at the longhouse in the",
                    "<str>barbarian village.",
                    "<str>Gunthor was not accommodating. He told me to ask Gudrun",
                    "<str>in the barbarian village what someone called Gunnar would think.",
                    "<str>Gudrun suggested I see if Dororan, outside the",
                    "<str>barbarian village, can think of anything to help convince Gunthor.",
                    "<str>Dororan, outside the barbarian village, is going to try to write",
                    "<str>a poem to convince Gunthor. I should check how he's getting on.",
                    "<navy>I need to take the poem to <maroon>Gudrun <navy>in the <maroon>barbarian village",
                    "<navy>and ask her to read it to her father.",
                )
                "write_poem", "more_poem", "one_more_poem", "poem_done" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<str>Dororan wants me to take the engraved ring to Gudrun",
                    "<str>in the barbarian village",
                    "<str>He asked that I not reveal that he is a dwarf just yet.",
                    "<str>Gudrun says that her father, Gunthor, would never allow her to",
                    "<str>have a relationship with an outsider. She wondered if I could",
                    "<str>convince him otherwise. He can be found at the longhouse in the",
                    "<str>barbarian village.",
                    "<str>Gunthor was not accommodating. He told me to ask Gudrun",
                    "<str>in the barbarian village what someone called Gunnar would think.",
                    "<str>Gudrun suggested I see if Dororan, outside the",
                    "<str>barbarian village, can think of anything to help convince Gunthor.",
                    "<maroon>Dororan, <navy>outside the <maroon>barbarian village, <navy>is going to try to write",
                    "<navy>a poem to convince Gunthor. I should check how he's getting on.",
                    "",
                )
                "tell_dororan" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<str>Dororan wants me to take the engraved ring to Gudrun",
                    "<str>in the barbarian village",
                    "<str>He asked that I not reveal that he is a dwarf just yet.",
                    "<str>Gudrun says that her father, Gunthor, would never allow her to",
                    "<str>have a relationship with an outsider. She wondered if I could",
                    "<str>convince him otherwise. He can be found at the longhouse in the",
                    "<str>barbarian village.",
                    "<str>Gunthor was not accommodating. He told me to ask Gudrun",
                    "<str>in the barbarian village what someone called Gunnar would think.",
                    "<navy>Gudrun suggested I see if <maroon>Dororan, <navy>outside the",
                    "<maroon>barbarian village, <navy>can think of anything to help convince Gunthor.",
                    "",
                )
                "tell_gudrun" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<str>Dororan wants me to take the engraved ring to Gudrun",
                    "<str>in the barbarian village",
                    "<str>He asked that I not reveal that he is a dwarf just yet.",
                    "<str>Gudrun says that her father, Gunthor, would never allow her to",
                    "<str>have a relationship with an outsider. She wondered if I could",
                    "<str>convince him otherwise. He can be found at the longhouse in the",
                    "<str>barbarian village.",
                    "<navy>Gunthor was not accommodating. He told me to ask <maroon>Gudrun",
                    "<navy>in the <maroon>barbarian village <navy>what someone called Gunnar would think.",
                    "",
                )
                "meet_chieftain" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<str>Dororan wants me to take the engraved ring to Gudrun",
                    "<str>in the barbarian village",
                    "<str>He asked that I not reveal that he is a dwarf just yet.",
                    "<navy>Gudrun says that her father, <maroon>Gunthor, <navy>would never allow her to",
                    "<navy>have a relationship with an outsider. She wondered if I could",
                    "<navy>convince him otherwise. He can be found at the <maroon>longhouse <navy>in the",
                    "<maroon>barbarian village.",
                    "",
                )
                "show_gudrun" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<str>I need to show the engraved ring to Dororan,",
                    "<str>outside the barbarian village.",
                    "<navy>Dororan wants me to take the <maroon>engraved ring <navy>to <maroon>Gudrun",
                    "<navy>in the <maroon>barbarian village",
                    "<navy>He asked that I not reveal that he is a dwarf just yet.",
                    "",
                )
                "engraved_ring" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<str>Dororan wants me to engrave the words ",
                    "<str>'Gudrun the Fair, Gudrun the Fiery' onto the gold ring with a chisel.",
                    "<navy>I need to show the <maroon>engraved ring <navy>to <maroon>Dororan, <navy>outside",
                    "<navy>the <maroon>barbarian village.",
                )
                "engrave" -> listOf(
                    "<str>I met an unhappy dwarf named Dororan just outside the",
                    "<str>barbarian village.",
                    "<str>Dororan, outside the barbarian village, wants me to bring him",
                    "<str>a gold ring he Specifically wants a ring from Jeffery in Edgeville.",
                    "<navy>Dororan wants me to engrave the words ",
                    "<navy>'Gudrun the Fair, Gudrun the Fiery' onto the <maroon>gold ring <navy>with a <maroon>chisel.",
                )
                "love_poem", "jeffery_ring" -> {
                    val list = mutableListOf(
                        "<str>I met an unhappy dwarf named Dororan just outside the",
                        "<str>barbarian village.",
                        "<maroon>Dororan <navy>outside the <maroon>barbarian village, <navy>wants me to bring him a",
                        "<maroon>Gold ring <navy>he Specifically wants a ring from <maroon>Jeffery <navy>in <maroon>Edgeville.",
                        "<navy>Items I need:",
                    )
                    if (player.holdsItem("ring_from_jeffery")) {
                        list.add("<str>Ring from Jeffery")
                    } else {
                        list.add("<maroon>Ring from Jeffery")
                    }
                    list
                }
                "started" -> listOf(
                    "<navy>I met an unhappy dwarf named <maroon>Dororan <navy>just outside the",
                    "<maroon>barbarian village.",
                )
                else -> listOf(
                    "<navy>I can start this quest by talking to <maroon>Dororan, <navy>the dwarf",
                    "<navy>just outside the <maroon>barbarian village.",
                )
            }
            player.questJournal("Gunnar's Ground", lines)
        }
    }
}
