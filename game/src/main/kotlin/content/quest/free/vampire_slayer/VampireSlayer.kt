package content.quest.free.vampire_slayer

import content.quest.free.vampire_slayer.CountDraynor.Companion.hasVampireTools
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class VampireSlayer : Script {
    init {
        questJournalOpen("vampire_slayer") { journal() }
    }

    private fun Player.journal() {
        val lines = when (quest("vampire_slayer")) {
            "completed" -> listOf("<str>I defeated Count Draynor and saved Draynor Village.", "", "<red>QUEST COMPLETE!")
            "armed" -> buildList {
                add("<navy>I must defeat <maroon>Count Draynor<navy> in the cellar of <maroon>Draynor Manor<navy>.")
                add("<navy>I need a <maroon>stake<navy> and <maroon>hammer<navy> to finish him off.")
                if (!hasVampireTools()) {
                    add("<navy>Dr Harlow can replace my missing stake or stake hammer.")
                }
                add("<navy>Garlic from Morgan's upstairs drawers will weaken him.")
            }
            "started" -> listOf("<navy>Morgan asked me to defeat the vampyre in <maroon>Draynor Manor<navy>.", "<navy>I should speak to <maroon>Dr Harlow<navy> in the <maroon>Blue Moon Inn<navy> in Varrock.", "<navy>He may be more helpful after a <maroon>beer<navy>.")
            else -> listOf("<navy>I can start this quest by speaking to <maroon>Morgan<navy> in", "<navy>his house in <maroon>Draynor Village<navy>.", "<navy>I will need to defeat a level 28 enemy.")
        }
        questJournal("Vampire Slayer", lines)
    }
}
