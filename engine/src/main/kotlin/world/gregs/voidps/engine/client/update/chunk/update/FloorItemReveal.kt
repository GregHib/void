package world.gregs.voidps.engine.client.update.chunk.update

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.map.chunk.ChunkUpdate

/**
 * @param owner Client index if matches client's local index then item won't be displayed
 */
data class FloorItemReveal(val floorItem: FloorItem, val owner: Int) : ChunkUpdate(7) {
    override fun visible(player: Player) = floorItem.visible(player)
}