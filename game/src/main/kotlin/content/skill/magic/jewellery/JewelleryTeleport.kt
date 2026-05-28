package content.skill.magic.jewellery

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

fun jewelleryTeleport(player: Player, inventory: String, slot: Int, area: Area): Boolean = itemTeleport(player, inventory, slot, area, "jewellery")

fun Player.teleport(tile: Tile, type: String, force: Boolean = false) = Teleport.teleport(this, tile, type, force = force)

fun itemTeleport(player: Player, inventory: String, slot: Int, area: Area, type: String): Boolean {
    if (player.queue.contains(ActionPriority.Strong) || !player.inventories.inventory(inventory).discharge(player, slot)) {
        return false
    }
    val tile = area.random(player) ?: return false
    return Teleport.teleport(player, tile, type, force = true)
}
