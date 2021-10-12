package world.gregs.voidps.engine.client.update.chunk.update

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.map.chunk.ChunkUpdate

/**
 * @param stack Previous item stack size
 * @param combined Updated item stack size
 */
data class FloorItemUpdate(val floorItem: FloorItem, val stack: Int, val combined: Int) : ChunkUpdate(7) {
    override fun visible(player: Player) = floorItem.visible(player)
}