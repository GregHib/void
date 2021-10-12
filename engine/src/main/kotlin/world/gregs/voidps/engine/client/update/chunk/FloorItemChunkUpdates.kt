package world.gregs.voidps.engine.client.update.chunk

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.network.*

fun addFloorItem(floorItem: FloorItem): ChunkUpdate = object : ChunkUpdate {
    override val size = 5

    override fun visible(player: Player) = floorItem.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_ADD)
        writeShortLittle(floorItem.amount)
        writeShortLittle(floorItem.id)
        writeByte(floorItem.tile.offset())
    }
}

fun removeFloorItem(floorItem: FloorItem): ChunkUpdate = object : ChunkUpdate {
    override val size = 3

    override fun visible(player: Player) = floorItem.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_REMOVE)
        writeShortAddLittle(floorItem.id)
        writeByteSubtract(floorItem.tile.offset())
    }
}

/**
 * @param owner Client index if matches client's local index then item won't be displayed
 */
fun revealFloorItem(floorItem: FloorItem, owner: Int): ChunkUpdate = object : ChunkUpdate {
    override val size = 7

    override fun visible(player: Player) = floorItem.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_REVEAL)
        writeShortLittle(floorItem.amount)
        writeByte(floorItem.tile.offset())
        writeShortAdd(floorItem.id)
        writeShortAdd(owner)
    }
}

/**
 * @param stack Previous item stack size
 * @param combined Updated item stack size
 */
fun updateFloorItem(floorItem: FloorItem, stack: Int, combined: Int): ChunkUpdate = object : ChunkUpdate {
    override val size = 7

    override fun visible(player: Player) = floorItem.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(Protocol.Batch.FLOOR_ITEM_UPDATE)
        writeByte(floorItem.tile.offset())
        writeShort(floorItem.id)
        writeShort(stack)
        writeShort(combined)
    }
}