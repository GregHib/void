package content.quest.free.imp_catcher

import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory

class ImpCatcher : Script {
    init {
        questJournalOpen("imp_catcher") {
            val lines = when (quest("imp_catcher")) {
                "started" -> {
                    val list = mutableListOf(
                        // https://youtu.be/bZlkR2EAN4w?t=16
                        "<str>I have spoken to Wizard Mizgog.",
                        "",
                        "<navy>I need to collect some items by killing <maroon>Imps.",
                    )
                    list.add("${if (inventory.contains("black_bead")) "<str>" else "<maroon>"}1 Black Bead.")
                    list.add("${if (inventory.contains("red_bead")) "<str>" else "<maroon>"}1 Red Bead.")
                    list.add("${if (inventory.contains("white_bead")) "<str>" else "<maroon>"}1 White Bead.")
                    list.add("${if (inventory.contains("yellow_bead")) "<str>" else "<maroon>"}1 Yellow Bead.")
                    list
                }
                "given_beads" -> emptyList<String>() // TODO proper message
                "completed" -> listOf(
                    "<str>I have spoken to Wizard Mizgog.",
                    "",
                    "<str>I have collected all the beads.",
                    "",
                    "<str>Wizard Mizgog thanked me for finding his beads and gave me.",
                    "<str>an Amulet of Accuracy.",
                    "<red>QUEST COMPLETE!",
                )
                // https://youtu.be/HM3xeOjl5Ww?t=9
                else -> listOf(
                    "<navy>I can start this quest by speaking to <maroon>Wizard Mizgog<navy> who is",
                    "<navy>in the <maroon>Wizards' Tower<navy>.",
                    "",
                    "<navy>There aren't any requirements for this quest.",
                )
            }
            questJournal("Imp Catcher", lines)
        }
    }
}
