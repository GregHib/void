package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.animate
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.type.Area
import world.gregs.voidps.world.interact.entity.sound.playSound

fun jewelleryTeleport(player: Player, inventory: String, slot: Int, area: Area): Boolean {
    return itemTeleport(player, inventory, slot, area, "jewellery")
}

fun itemTeleport(player: Player, inventory: String, slot: Int, area: Area, type: String): Boolean {
    if (player.queue.contains(ActionPriority.Normal) || !player.inventories.inventory(inventory).discharge(player, slot)) {
        return false
    }
    player.closeInterfaces()
    player.queue("teleport_$type", onCancel = null) {
        player.playSound("teleport")
        player.setGraphic("teleport_$type")
        player.animate("teleport_$type")
        player.tele(area.random(player)!!)
        val int = player.setAnimation("teleport_land_$type")
        if (int == -1) {
            player.clearAnimation()
        } else {
            player.setGraphic("teleport_land_$type")
        }
    }
    return true
}