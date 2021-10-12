package world.gregs.voidps.engine.client.update.chunk.update

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.map.chunk.ChunkUpdate

data class GraphicAddition(val areaGraphic: AreaGraphic) : ChunkUpdate(7) {
    override fun visible(player: Player): Boolean = areaGraphic.visible(player)
}

