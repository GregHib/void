package world.gregs.voidps.engine.client.update.chunk

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.network.Protocol

fun addGraphic(ag: AreaGraphic): ChunkUpdate = object : ChunkUpdate {
    override val size = 7

    override fun visible(player: Player): Boolean = ag.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.GRAPHIC_AREA)
        writeByte(ag.tile.offset())
        writeShort(ag.graphic.id)
        writeByte(ag.graphic.height)
        writeShort(ag.graphic.delay)
        writeByte(ag.graphic.rotation)
    }
}