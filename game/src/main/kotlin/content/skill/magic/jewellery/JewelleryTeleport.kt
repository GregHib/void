package content.skill.magic.jewellery

import content.entity.sound.sound
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Area

fun jewelleryTeleport(player: Player, inventory: String, slot: Int, area: Area): Boolean = itemTeleport(player, inventory, slot, area, "jewellery")

fun itemTeleport(player: Player, inventory: String, slot: Int, area: Area, type: String): Boolean {
    if (player.queue.contains(ActionPriority.Normal) || !player.inventories.inventory(inventory).discharge(player, slot)) {
        return false
    }
    player.closeInterfaces()
    player.queue("teleport_$type", onCancel = null) {
        player.sound("teleport")
        player.gfx("teleport_$type")
        player.animDelay("teleport_$type")
        player.tele(area.random(player)!!)
        val int = player.anim("teleport_land_$type")
        if (int == -1) {
            player.clearAnim()
        } else {
            player.gfx("teleport_land_$type")
        }
    }
    return true
}
