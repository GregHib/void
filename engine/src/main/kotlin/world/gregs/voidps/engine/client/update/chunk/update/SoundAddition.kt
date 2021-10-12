package world.gregs.voidps.engine.client.update.chunk.update

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.map.chunk.ChunkUpdate

data class SoundAddition(val areaSound: AreaSound) : ChunkUpdate(8) {
    override fun visible(player: Player): Boolean = areaSound.visible(player)
}