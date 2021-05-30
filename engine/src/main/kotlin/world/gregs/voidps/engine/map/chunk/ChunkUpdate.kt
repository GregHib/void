package world.gregs.voidps.engine.map.chunk

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player

interface ChunkUpdate {
    val size: Int
    suspend fun encode(writer: ByteWriteChannel)

    fun visible(player: Player): Boolean
}