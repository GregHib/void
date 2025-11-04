package content.area.misthalin.lumbridge.catacomb

import content.entity.combat.killer
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class LumbridgeCatacombs : Script {

    init {
        objectOperate("Take", "*_demon_statuette") { (target) ->
            val def = target.def(this)
            if (get(def.stringId, "take") != "take") {
                message("You've already taken this statuette.")
                return@objectOperate
            }
            statement("The air grows tense as you approach the statuette. You sense a hostile presence nearby...")

            choice {
                option("Take the statuette.") {
                    if (inventory.add(def.stringId)) {
                        set(def.stringId, "plinth")
                        message("You carefully take the ${def.stringId}")
                    } else {
                        inventoryFull()
                    }
                }
                option("Leave it alone.")
            }
        }

        objectOperate("Take", "diamond_demon_statuette") {
            if (get("diamond_demon_statuette", "take_shield") != "take") {
                return@objectOperate
            }
            if (inventory.add("diamond_demon_statuette")) {
                set("diamond_demon_statuette", "touch")
            }
        }

        npcDeath("dragith_nurn") {
            val killer = killer
            if (killer is Player) {
                killer.message("With Dragith Nurn defeated, the diamond statuette is now within your grasp.")
                killer["diamond_demon_statuette"] = "take"
            }
        }

        destroyed("*_demon_statuette") { item ->
            set(item.id, "take")
        }
    }
}
