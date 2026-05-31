package content.quest.free.blood_pact

import content.entity.player.bank.bank
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.carriesItem

class BloodPact : Script{

    init {
        questJournalOpen("blood_pact") {
            val lines = when (quest("blood_pact")) {
                "completed" -> listOf(
                    "<str>Xenia, an old adventurer, said she had seen some Zamorakian cultists entering the catacombs beneath Lumbridge Church. She asked me to go with her into the catacombs to deal with them.",
                    "<str>Inside the catacombs, Xenia and I overheard the cultists talking about a blood pact.",
                    "<str>The first cultist shot Xenia, wounding her badly. She will not be able to fight.",
                    //if else
                    "<str>I defeated the first cultist.",
                    //if else
                    "<str>I defeated the second cultist.",
                    //if else
                    "<str>I defeated the third cultist.",
                    "<str>The death of the third cultist completed the ritual. A tomb in the catacombs collapsed, revealing a staircase.",
                    "<str>I untied the prisoner and escaped from the catacombs.",
                    "<str>Xenia thanked me for my help.",
                    "",
                    "<red>QUEST COMPLETE!",
                )
                "started", "watched_cutscene" -> listOf(
                    "<maroon>Xenia<navy>, an old adventurer, said she had seen some Zamorakian ",
                    "<navy>cultists entering the catacombs beneath Lumbridge Church. ",
                    "<navy>She asked me to  go with her into the <maroon>catacombs ",
                    "<navy>to deal with them.",
                    "",
                )
                else -> listOf(
                    "<navy> I can start this quest by speaking to <maroon>Xenia <navy>in the <marron>Lumbridge cemetery.",
                    "",
                )
            }
            questJournal("Blood Pact", lines)
        }
    }
}