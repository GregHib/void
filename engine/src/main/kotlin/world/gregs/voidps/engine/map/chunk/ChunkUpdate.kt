package world.gregs.voidps.engine.map.chunk

import world.gregs.voidps.engine.entity.character.player.Player

abstract class ChunkUpdate(val size: Int) {

    abstract fun visible(player: Player): Boolean

}