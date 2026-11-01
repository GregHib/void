package content.quest.free.ernest_the_chicken

import content.entity.player.bank.bank
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

class ErnestTheChicken : Script {
    init {
        questJournalOpen("ernest_the_chicken") { journal() }
    }

    private fun Player.journal() {
        val lines = when (quest("ernest_the_chicken")) {
            "completed" -> listOf("<str>I found the missing machine parts and helped restore Ernest.", "", "<red>QUEST COMPLETE!")
            "restored" -> listOf("<navy>Ernest is human again. I should speak to Professor", "<navy>Oddenstein to finish helping him.")
            "parts" -> buildList {
                if (get("ernest_parts_returned", false)) {
                    add("<str>I found the three missing machine parts.")
                    add("<navy>I returned them to Professor Oddenstein.")
                    add("<navy>I should speak to him to finish repairing the machine.")
                    return@buildList
                }
                add("<navy>Professor Oddenstein turned Ernest into a chicken.")
                add("<navy>I must find three parts to repair his machine:")
                for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) {
                    add("${if (ownsErnestPart(part)) "<str>" else "<maroon>"}${part.replace('_', ' ')}")
                }
                add("<navy>The parts are hidden around Draynor Manor.")
            }
            "started" -> listOf("<navy>Veronica asked me to find Ernest inside Draynor Manor.", "<navy>I should speak to the professor on the top floor.")
            else -> listOf("<navy>I can start this quest by speaking to Veronica", "<navy>outside the gates of Draynor Manor.")
        }
        questJournal("Ernest the Chicken", lines)
    }

    companion object {
        internal fun Player.ownsErnestPart(id: String): Boolean = inventory.contains(id) || bank.contains(id)
    }
}
