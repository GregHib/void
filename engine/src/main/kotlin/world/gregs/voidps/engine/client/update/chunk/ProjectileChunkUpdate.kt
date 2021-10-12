package world.gregs.voidps.engine.client.update.chunk

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.proj.Projectile
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.network.Protocol

fun addProjectile(projectile: Projectile): ChunkUpdate = object : ChunkUpdate {
    override val size = 16

    override fun visible(player: Player): Boolean = projectile.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.PROJECTILE_ADD)
        writeByte(projectile.tile.offset(3))
        writeByte(projectile.direction.x)
        writeByte(projectile.direction.y)
        writeShort(projectile.index)
        writeShort(projectile.id)
        writeByte(projectile.startHeight)
        writeByte(projectile.endHeight)
        writeShort(projectile.delay)
        writeShort(projectile.delay + projectile.flightTime)
        writeByte(projectile.curve)
        writeShort(projectile.offset)
    }
}