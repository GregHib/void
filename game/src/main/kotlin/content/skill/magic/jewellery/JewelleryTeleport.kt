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
import world.gregs.voidps.type.Tile

fun jewelleryTeleport(player: Player, inventory: String, slot: Int, area: Area): Boolean = itemTeleport(player, inventory, slot, area, "jewellery")

fun Player.teleport(tile: Tile, type: String, force: Boolean = false) = itemTeleport(this, tile, type, force)

fun itemTeleport(player: Player, inventory: String, slot: Int, area: Area, type: String): Boolean {
    if (player.queue.contains(ActionPriority.Normal) || !player.inventories.inventory(inventory).discharge(player, slot)) {
        return false
    }
    return itemTeleport(player, area, type)
}

fun itemTeleport(player: Player, area: Area, type: String, force: Boolean = false): Boolean {
    if (!force && player.queue.contains(ActionPriority.Normal)) {
        return false
    }
    return itemTeleport(player, area.random(player) ?: return false, type, force)
}

fun itemTeleport(player: Player, tile: Tile, type: String, force: Boolean = false): Boolean {
    if (!force && player.queue.contains(ActionPriority.Normal)) {
        return false
    }
    player.closeInterfaces()
    player.queue("teleport_$type", onCancel = null) {
        player.sound("teleport")
        player.gfx("teleport_$type")
        player.animDelay("teleport_$type")
        player.tele(tile)
        val int = player.anim("teleport_land_$type")
        if (int == -1) {
            player.clearAnim()
        } else {
            player.gfx("teleport_land_$type")
        }
    }
    return true
}
