package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.network.*
import world.gregs.voidps.utility.get

fun addObject(gameObject: GameObject): ChunkUpdate = object : ChunkUpdate {
    override val size = 4

    override fun visible(player: Player) = gameObject.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.OBJECT_ADD)
        writeByteSubtract((gameObject.type shl 2) or  gameObject.rotation)
        writeShort( gameObject.id)
        writeByteAdd( gameObject.tile.offset())
    }
}

fun GameObject.animate(id: Int) = get<ChunkBatches>().update(tile.chunk, object : ChunkUpdate {
    override val size = 4

    override fun visible(player: Player) = true

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.OBJECT_ANIMATION_SPECIFIC)
        writeShortLittle(id)
        writeByteSubtract(tile.offset())
        writeByteInverse((type shl 2) or rotation)
    }
})

fun removeObject(gameObject: GameObject): ChunkUpdate = object : ChunkUpdate {
    override val size = 2

    override fun visible(player: Player) = gameObject.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.OBJECT_REMOVE)
        writeByteAdd((gameObject.type shl 2) or gameObject.rotation)
        writeByte(gameObject.tile.offset())
    }
}

/**
 * Show animation of an object for a single client
 * @param tile 30 bit location hash
 * @param animation Animation id
 * @param type Object type
 * @param rotation Object rotation
 */
fun Client.animateObject(
    tile: Int,
    animation: Int,
    type: Int,
    rotation: Int
) = send(Protocol.OBJECT_ANIMATION) {
    writeShortAddLittle(animation)
    writeByteAdd((type shl 2) or rotation)
    writeIntInverseMiddle(tile)
}

/**
 * Preloads a object model
 */
fun Client.preloadObject(
    id: Int,
    modelType: Int
) = send(Protocol.OBJECT_PRE_FETCH) {
    writeShort(id)
    writeByte(modelType)
}