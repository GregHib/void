package world.gregs.voidps.engine.client.update.chunk.update

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.map.chunk.ChunkUpdate

data class FloorItemRemoval(val floorItem: FloorItem) : ChunkUpdate(3) {
    override fun visible(player: Player) = floorItem.visible(player)
}