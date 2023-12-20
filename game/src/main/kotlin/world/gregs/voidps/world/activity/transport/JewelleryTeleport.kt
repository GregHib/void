package world.gregs.voidps.world.activity.transport

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.type.Area
import world.gregs.voidps.world.interact.entity.sound.playSound

fun teleport(player: Player, name: String, area: Area) {
    if (player.queue.contains(ActionPriority.Normal)) {
        return
    }
    player.closeInterfaces()
    player.queue("jewellery_teleport", onCancel = null) {
        player.playSound("teleport")
        player.setGraphic("teleport_$name")
        player.start("movement_delay", 2)
        player.playAnimation("teleport_$name", canInterrupt = false)
        player.tele(area.random(player)!!)
        pause(1)
        player.playSound("teleport_land")
        player.setGraphic("teleport_land_$name")
        player.playAnimation("teleport_land_$name", canInterrupt = false)
    }
}