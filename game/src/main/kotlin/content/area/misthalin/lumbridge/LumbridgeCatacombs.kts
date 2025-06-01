package content.area.misthalin.lumbridge

import content.entity.combat.killer
import content.entity.death.npcDeath
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

objectOperate("Take", "*_demon_statuette") {
    if (player[target.id, "shield"] == "touch") {
        player.message("You've already taken this statuette.")
        return@objectOperate
    }
    statement("The air grows tense as you approach the statuette. You sense a hostile presence nearby...")

    choice {
        option("Take the statuette.") {
            if (player.inventory.isFull()) {
                player.inventoryFull()
            } else {
                player.inventory.add(target.id)
                player[target.id] = "take"
                player.message("You carefully take the ${target.id}")
            }
        }
        option("Leave it alone.") {
            // Do nothing
        }
    }
// These should be outside the above block
    objectOperate("Take", "diamond_demon_statuette") {
        player.inventory.add("diamond_demon_statuette")
        player["diamond_demon_statuette"] = "take"
    }
    npcDeath("dragith_nurn") { npc ->
        val killer = npc.killer
        if (killer is Player) {
            killer.message("With Dragith Nurn defeated, the diamond statuette is now within your grasp.")
        }
    }
}