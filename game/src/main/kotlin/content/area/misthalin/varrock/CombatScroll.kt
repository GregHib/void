package content.area.misthalin.varrock

import content.quest.letterScroll
import world.gregs.voidps.engine.Script

class CombatScroll : Script {

    init {
        objectOperate("Read", "combat_scroll") {
            letterScroll(
                "A Beginner's Guide to Killing Things",
                listOf(
                    "              by Ali Morrisane.",
                    "",
                    "              Step 1: Choose your target.",
                    "              Step 2: Hit it.",
                    "              Step 3: Go to Step 2.",
                ),
            )
        }
    }
}
