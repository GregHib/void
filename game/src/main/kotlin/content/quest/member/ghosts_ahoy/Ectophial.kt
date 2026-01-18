package content.quest.member.ghosts_ahoy

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactItemOn
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.type.Tile

class Ectophial(val objects: GameObjects) : Script {

    init {
        itemOption("Empty", "ectophial") {
            gfx("empty_ectophial")
            animDelay("empty_ectophial")
            delay(2)
            Teleport.teleport(this, "ectophial_teleport", "ectophial")
        }

        itemOnObjectOperate("ectophial_empty", "ectofuntus") {
            if (inventory.replace(it.slot, it.item.id, "ectophial")) {
                anim("take")
                message("You refill the ectophial from the Ectofuntus.")
            }
        }

        teleportTakeOff("ectophial") {
            anim("empty_ectophial")
            gfx("empty_ectophial")
            message("You empty the ectoplasm onto the ground around your feet...", ChatType.Filter)
            return@teleportTakeOff true
        }

        teleportLand("ectophial") {
            message("... and the world changes around you.", ChatType.Filter)
            val ectofuntus = objects.findOrNull(Tile(3658, 3518), "ectofuntus") ?: return@teleportLand
            val slot = inventory.indexOf("ectophial")
            interactItemOn(ectofuntus, "inventory", "inventory", Item("empty_ectophial"), slot)
        }
    }
}
