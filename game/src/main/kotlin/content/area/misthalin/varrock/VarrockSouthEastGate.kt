package content.area.misthalin.varrock

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit

class VarrockSouthEastGate : Script {

    init {
        npcOperate("Talk-to", "guard_biohazard") {
            npc<Neutral>("Please don't disturb me, I've got to keep an eye out for suspicious individuals.")
        }

        objectOperate("Open", "guidor_gate_left_closed,guidor_gate_right_closed") { (target) ->
            if (tile.x >= target.tile.x || questStage("biohazard") !in SEARCHED) {
                enterDoor(target)
                return@objectOperate
            }
            search()
            enterDoor(target)
        }
    }

    private suspend fun Player.search() {
        npc<Neutral>("guard_biohazard", "Halt. I need to conduct a search on you. There have been reports of someone bringing a virus into Varrock.")
        message("The guard searches you.")
        delay(1)
        for (vial in VIALS) {
            val amount = inventory.count(vial)
            if (amount <= 0) {
                continue
            }
            inventory.removeToLimit(vial, amount)
            message("He takes the vial of ${vial.replace('_', ' ')} from you.")
            delay(1)
        }
        npc<Neutral>("guard_biohazard", "You may now pass.")
    }

    private companion object {
        val SEARCHED = listOf(10, 12)
        val VIALS = listOf("ethenea", "liquid_honey", "sulphuric_broline")
    }
}
