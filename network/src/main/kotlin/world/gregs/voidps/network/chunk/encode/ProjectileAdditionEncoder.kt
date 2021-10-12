package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.ProjectileAddition

class ProjectileAdditionEncoder : ChunkEncoder<ProjectileAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: ProjectileAddition) = writer.run {
        writeByte(Protocol.Batch.PROJECTILE_ADD)
        writeByte(update.tileOffset)
        writeByte(update.directionX)
        writeByte(update.directionY)
        writeShort(update.index)
        writeShort(update.id)
        writeByte(update.startHeight)
        writeByte(update.endHeight)
        writeShort(update.delay)
        writeShort(update.delay + update.flightTime)
        writeByte(update.curve)
        writeShort(update.offset)
    }
}