package content.quest.free.ernest_the_chicken

import content.entity.player.bank.ownsItem
import content.quest.questJournal
import content.quest.questStage
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class ErnestTheChicken : Script {
    init {
        questJournalOpen("ernest_the_chicken") { journal() }
    }

    private fun Player.journal() {
        val stage = questStage("ernest_the_chicken")
        val lines = mutableListOf<String>()
        if (stage == 0) {
            lines += "<navy>I can start this quest by speaking to <maroon>Veronica"
            lines += "<navy>outside the gates of <maroon>Draynor Manor<navy>."
        } else {
            lines += "<str>I have spoken to Veronica"
            lines += ""
        }
        if (stage == 1) {
            lines += "<navy>I need to find <maroon>Ernest"
            lines += "<navy>He went into <maroon>Draynor Manor<navy> and hasn't returned"
        } else if (stage >= 2) {
            lines += "<str>I've spoken to Dr Oddenstein, and discovered Ernest is a"
            lines += "<str>chicken"
            lines += ""
            lines += ""
        }

        if (stage == 2) {
            lines += "<navy>I need to bring <maroon>Dr Oddenstein<navy> parts for his machine"
            for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) {
                lines.add("${if (ownsItem(part)) "<str>" else "<maroon>"}1 ${part.toTitleCase()}")
            }
        } else if (stage >= 3) {
            lines += "<str>I have collected all the parts for the machine"
        }

        if (stage == 3) {
            if (get("ernest_parts_returned", false)) {
                lines += "<navy>I returned them to Professor Oddenstein."
                lines += "<navy>I should speak to him to finish repairing the machine."
            } else {
                lines += "<navy>Ernest is human again. I should speak to Professor"
                lines += "<navy>Oddenstein to finish helping him."
            }
        } else if (stage >= 4) {
            lines += "<str>Dr Oddenstein thanked me for helping fix his machine"
            lines += "<str>We turned Ernest back to normal and he rewarded me"
            lines += "<red>QUEST COMPLETE!"
        }
        questJournal("Ernest the Chicken", lines)
    }
}
