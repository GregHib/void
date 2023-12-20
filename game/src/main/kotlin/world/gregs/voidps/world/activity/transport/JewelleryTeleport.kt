package world.gregs.voidps.world.activity.transport

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.type.Area
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.interact.entity.sound.playSound

fun jewelleryTeleport(player: Player, inventory: String, slot: Int, area: Area) {
    if (player.queue.contains(ActionPriority.Normal) || !Degrade.discharge(player, inventory, slot)) {
        return
    }
    player.closeInterfaces()
    player.queue("jewellery_teleport", onCancel = null) {
        player.playSound("teleport")
        player.setGraphic("teleport_jewellery")
        player.start("movement_delay", 2)
        player.playAnimation("teleport_jewellery", canInterrupt = false)
        player.tele(area.random(player)!!)
        player.clearAnimation()
    }
}