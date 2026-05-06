package content.quest.member.tears_of_guthix

import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.timer.epochSeconds
import java.util.concurrent.TimeUnit

class TearsOfGuthix : Script {

    init {
        questJournalOpen("tears_of_guthix") {
            val lines = when (quest("tears_of_guthix")) {
                "completed" -> {
                    val list = mutableListOf(
                        "<str>I met Juna the serpent in a deep chasm beneath the",
                        "<str>Lumbridge Swamp Caves. I made a bowl out of magical stone in",
                        "<str>order to catch the Tears of Guthix.",
                        "",
                        "<red>QUEST COMPLETE!",
                        "",
                        "<navy>Now Juna will let me into the cave to collect the Tears if I <maroon>tell",
                        "<maroon>her stories<navy> of my adventures.",
                    )
                    if (hasClock("tears_of_guthix_cooldown")) {
                        val days = TimeUnit.DAYS.toDays(remaining("tears_of_guthix_cooldown", epochSeconds()).toLong())
                        list.add("<navy>I will be able to collect the Tears of Guthix <maroon>in $days ${"day".plural(days)}<navy>.")
                    } else {
                        list.add("<navy>I have had enough adventures to tell Juna more stories, and a week")
                        list.add("<navy>has passed since I last collected the Tears. I can visit Juna again")
                        list.add("<navy>now.")
                    }
                    list
                }
                "stone_bowl" -> {
                    listOf(
                        "<str>I met Juna the serpent in a deep chasm beneath the",
                        "<str>Lumbridge Swamp Caves.",
                        "<navy>I told her a story and she said she would let me into the Tears of",
                        "<navy>Guthix cave if I brought her a <maroon>bowl<navy> made from the stone in <maroon>the",
                        "<maroon>cave on the South side of the chasm<navy>.",
                    )
                    listOf(
                        "<str>I met Juna the serpent in a deep chasm beneath the",
                        "<str>Lumbridge Swamp Caves.",
                        "<navy>I made a bowl out of <maroon>magical stone<navy> in order to catch", // TODO proper message
                        "<navy>the <maroon>Tears of Guthix<navy>.",
                    )
                }
                else -> listOf(
                    "<navy>I can start this quest by speaking to <maroon>Juna the serpent<navy> who lives",
                    "<navy>deep in the <maroon>Lumbridge Swamp Caves<navy>.",
                )
            }
            questJournal("Tears of Guthix", lines)
        }
    }
}
