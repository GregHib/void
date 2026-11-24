package content.quest.free.ernest_the_chicken

import content.entity.player.bank.ownsItem
import content.quest.questJournal
import content.quest.questStage
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script

class ErnestTheChicken : Script {
    init {
        questJournalOpen("ernest_the_chicken") {
            val stage = questStage("ernest_the_chicken")
            val lines = mutableListOf<String>()
            if (stage == 0) {
                lines += "<navy>I can start this quest by speaking to <maroon>Veronica<navy> who is"
                lines += "<navy>outside <maroon>Draynor Manor"
                lines += ""
                lines += "<navy>There aren't any requirements for this quest"
            } else {
                lines += "<str>I have spoken to Veronica"
                lines += ""
            }
            if (stage == 1) {
                lines += "<navy>I need to find <maroon>Ernest"
                lines += "<navy>He went into <maroon>Draynor Manor<navy> and hasn't returned"
            } else if (stage == 2) {
                lines += "<str>I've spoken to Dr Oddenstein, and discovered Ernest is a"
                lines += "<str>chicken"
                lines += ""
                lines += "<navy>I need to bring <maroon>Dr Oddenstein<navy> parts for his machine"
                for (part in listOf("oil_can", "pressure_gauge", "rubber_tube")) {
                    lines += "${if (ownsItem(part)) "<str>" else "<maroon>"}1 ${part.toTitleCase()}"
                }
            } else if (stage >= 3) {
                lines += "<str>I have collected all the parts for the machine"
                lines += ""
            }

            if (stage == 3) {
                lines += "<navy>Ernest is human again. I should speak to <maroon>Dr Oddenstein"
                lines += "<navy>to finish helping him."
            } else if (stage >= 4) {
                lines += "<str>Dr Oddenstein thanked me for helping fix his machine"
                lines += "<str>We turned Ernest back to normal and he rewarded me"
                lines += "<red>QUEST COMPLETE!"
            }
            questJournal("Ernest the Chicken", lines)
        }
    }
}
