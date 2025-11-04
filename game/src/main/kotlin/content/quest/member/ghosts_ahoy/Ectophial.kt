package content.quest.member.ghosts_ahoy

import content.skill.magic.spell.Teleport.Companion.teleport
import content.skill.magic.spell.teleportLand
import content.skill.magic.spell.teleportTakeOff
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactItemOn
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.type.Tile

class Ectophial : Script {

    val objects: GameObjects by inject()

    init {
        itemOption("Empty", "ectophial") {
            gfx("empty_ectophial")
            animDelay("empty_ectophial")
            delay(2)
            teleport(this, "ectophial_teleport", "ectophial")
        }

        itemOnObjectOperate("ectophial_empty", "ectofuntus") {
            if (inventory.replace(it.slot, it.item.id, "ectophial")) {
                anim("take")
                message("You refill the ectophial from the Ectofuntus.")
            }
        }

        teleportTakeOff("ectophial") {
            player.anim("empty_ectophial")
            player.gfx("empty_ectophial")
            player.message("You empty the ectoplasm onto the ground around your feet...", ChatType.Filter)
        }

        teleportLand("ectophial") {
            player.message("... and the world changes around you.", ChatType.Filter)
            val ectofuntus = objects[Tile(3658, 3518), "ectofuntus"] ?: return@teleportLand
            val slot = player.inventory.indexOf("ectophial")
            player.interactItemOn(ectofuntus, "inventory", "inventory", Item("empty_ectophial"), slot)
        }
    }
}
