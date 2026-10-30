package content.area.asgarnia.port_sarim

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class DrunkenSailor : Script {

    init {
        // https://runescape.wiki/w/Transcript:Drunken_sailor
        npcOperate("Talk-to", "drunken_sailor_normal_2") {
            statement("You shake the snoring sailor by the shoulder. It doesn't look like he's going to wake up any time soon.")
        }

        npcOperate("Search", "drunken_sailor_normal_2") {
            if (ownsItem("sailors_hat")) {
                statement("You search the sailor's pockets but find nothing.")
                return@npcOperate
            }
            if (!inventory.add("sailors_hat")) {
                statement("You find the sailor's hat in one of his pockets. You don't have room in your inventory for it.")
                return@npcOperate
            }
            statement("You find the sailor's hat in one of his pockets. You put it in your inventory.")
        }
    }
}
