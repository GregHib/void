package world.gregs.voidps.world.activity.skill.firemaking

import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

itemOnItem({ fromItem.id.endsWith("firelighter") && toItem.id == "logs" }) { player: Player ->
    player.inventory.transaction {
        remove(fromItem.id)
        val colour = fromItem.id.removeSuffix("_firelighter")
        replace(toItem.id, "${colour}_logs")
    }
}