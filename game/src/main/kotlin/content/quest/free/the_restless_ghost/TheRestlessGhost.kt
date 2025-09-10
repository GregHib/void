package content.quest.free.the_restless_ghost

import content.entity.player.bank.ownsItem
import content.entity.player.modal.tab.questJournalOpen
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.event.Script

@Script
class TheRestlessGhost {

    init {
        questJournalOpen("the_restless_ghost") {
            val lines = when (player.quest("the_restless_ghost")) {
                "completed" -> listOf(
                    "<str>I've not started this quest yet.",
                    "<str>I can start this quest by speaking to Father Aereck in the",
                    "<str>church east of Lumbridge Castle in the centre of",
                    "<str>Lumbridge",
                    "<str>I should find Father Urhney, who is an expert on ghosts.",
                    "<str>He lives in a shack in the south of Lumbridge Swamp, near",
                    "<str>the coastline",
                    "<str>I should talk to the ghost in the crypt south of",
                    "<str>Lumbridge church to find out why it is haunting the",
                    "<str>graveyard I must make sure to wear my ghostspeak",
                    "<str>amulet when doing so.",
                    "<str>I should go and search the Mining spot south of Lumbridge",
                    "<str>for the ghost's skull.",
                    "<str>I should take the skull back to the ghost so it can rest in peace.",
                    "",
                    "<red>QUEST COMPLETE!",
                    "",
                )
                "started" -> listOf(
                    "<str>I've not started this quest yet.",
                    "<str>I can start this quest by speaking to Father Aereck in the",
                    "<str>church east of Lumbridge Castle in the centre of",
                    "<str>Lumbridge.",
                    "",
                    "<navy>I should find <maroon>Father Urhney, <navy>who is an expert on <maroon>ghosts.",
                    "<navy>He lives in a <maroon>shack <navy>in the south of <maroon>Lumbridge Swamp, <navy>near",
                    "<navy>the coastline.",
                )
                "ghost" -> {
                    val list = mutableListOf(
                        "<str>I've not started this quest yet.",
                        "<str>I can start this quest by speaking to Father Aereck in the",
                        "<str>church east of Lumbridge Castle in the centre of",
                        "<str>Lumbridge",
                        "<str>I should find Father Urhney, who is an expert on ghosts.",
                        "<str>He lives in a shack in the south of Lumbridge Swamp, near",
                        "<str>the coastline.",
                        "",
                        "<navy>I should talk to the <maroon>ghost <navy>in the crypt south of Lumbridge",
                        "<navy>church to find out why it is haunting the <maroon>graveyard <navy>I must",
                        "<navy>make sure to wear my ghostspeak amulet when doing so.",
                        "",
                    )
                    if (!player.ownsItem("ghostspeak_amulet")) {
                        list.add("<navy>I seem to have loast my <maroon>Amulet of Ghost speak. <navy>I should talk to")
                        list.add("<maroon>Father Urhney <navy>and see if he has a replacement.")
                        list.add("")
                    }
                    list
                }
                "mining_spot", "found_skull" -> {
                    val list = mutableListOf(
                        "<str>I've not started this quest yet.",
                        "<str>I can start this quest by speaking to Father Aereck in the",
                        "<str>church east of Lumbridge Castle in the centre of",
                        "<str>Lumbridge",
                        "<str>I should find Father Urhney, who is an expert on ghosts.",
                        "<str>He lives in a shack in the south of Lumbridge Swamp, near",
                        "<str>the coastline",
                        "<str>I should talk to the ghost in the crypt south of",
                        "<str>Lumbridge church to find out why it is haunting the",
                        "<str>graveyard I must make sure to wear my ghostspeak",
                        "<str>amulet when doing so.",
                        "",
                    )
                    if (!player.ownsItem("muddy_skull")) {
                        list.add("<navy>I should go and search the <maroon>Mining spot <navy>on the coast <maroon>south")
                        list.add("<maroon>of Lumbridge <navy>for the <maroon>ghost's skull.")
                        list.add("")
                    } else {
                        list.add("<navy>I should take the <maroon>skull <navy>back to the <maroon>ghost <navy> in the graveyard")
                        list.add("<navy>south of lumbridge church so it can rest in peace.")
                        list.add("")
                    }
                    list
                }
                else -> listOf(
                    "<navy>I can start this quest by speaking to <maroon>Father Aereck <navy>in the",
                    "<maroon>church <navy>next to <maroon>Lumbridge Castle.",
                )
            }
            player.questJournal("The Restless Ghost", lines)
        }
    }
}
