package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.ProjectileAddition
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol

class ProjectileAdditionEncoder : ChunkEncoder<ProjectileAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: ProjectileAddition) = writer.run {
        writeByte(Protocol.Batch.PROJECTILE_ADD)
        writeByte(update.projectile.tile.offset(3))
        writeByte(update.projectile.direction.x)
        writeByte(update.projectile.direction.y)
        writeShort(update.projectile.index)
        writeShort(update.projectile.id)
        writeByte(update.projectile.startHeight)
        writeByte(update.projectile.endHeight)
        writeShort(update.projectile.delay)
        writeShort(update.projectile.delay + update.projectile.flightTime)
        writeByte(update.projectile.curve)
        writeShort(update.projectile.offset)
    }
}