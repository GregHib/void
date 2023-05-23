package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.network.*
import world.gregs.voidps.network.encode.chunk.*

fun encodeBatch(messages: Collection<ChunkUpdate>): ByteArray {
    val writeChannel = ByteArrayChannel()
    runBlocking {
        messages.forEach { update ->
            writeChannel.writeByte(update.packetIndex.toByte())
            writeChannel.encode(update)
        }
    }
    return writeChannel.toByteArray()
}

fun Client.sendBatch(messages: Collection<ChunkUpdate>, chunkOffsetX: Int, chunkOffsetY: Int, chunkPlane: Int) {
    send(Protocol.BATCH_UPDATE_CHUNK, messages.sumOf { it.size + 1 } + 3, Client.SHORT) {
        writeByteInverse(chunkOffsetX)
        writeByteSubtract(chunkPlane)
        writeByteSubtract(chunkOffsetY)
        messages.forEach { update ->
            writeByte(update.packetIndex)
            encode(update)
        }
    }
}

fun Client.sendBatch(messages: ByteArray, chunkOffsetX: Int, chunkOffsetY: Int, chunkPlane: Int) {
    send(Protocol.BATCH_UPDATE_CHUNK, messages.size + 3, Client.SHORT) {
        writeByteInverse(chunkOffsetX)
        writeByteSubtract(chunkPlane)
        writeByteSubtract(chunkOffsetY)
        writeBytes(messages)
    }
}

fun Client.send(update: ChunkUpdate) {
    send(update.packetId) {
        encode(update)
    }
}

suspend fun ByteWriteChannel.encode(update: ChunkUpdate) {
    when (update) {
        is FloorItemAddition -> floorItemAddition(update)
        is FloorItemRemoval -> floorItemRemoval(update) // update
        is FloorItemReveal -> floorItemReveal(update) // update
        is FloorItemUpdate -> floorItemUpdate(update) // update
        is GraphicAddition -> graphicAddition(update)
        is MidiAddition -> midiAddition(update)
        is ObjectAddition -> objectAddition(update)
        is ObjectAnimation -> objectAnimation(update) // update
        is ObjectRemoval -> objectRemoval(update) // update if removing a temp obj (not original)
        is ProjectileAddition -> projectileAddition(update)
        is SoundAddition -> soundAddition(update)
    }
}

private suspend fun ByteWriteChannel.floorItemAddition(update: FloorItemAddition) {
    writeShortLittle(update.amount)
    writeShortLittle(update.id)
    writeByte(update.tileOffset)
}

private suspend fun ByteWriteChannel.floorItemRemoval(update: FloorItemRemoval) {
    writeShortAddLittle(update.id)
    writeByteSubtract(update.tileOffset)
}

private suspend fun ByteWriteChannel.floorItemReveal(update: FloorItemReveal) {
    writeShortLittle(update.amount)
    writeByte(update.tileOffset)
    writeShortAdd(update.id)
    writeShortAdd(update.ownerIndex)
}

private suspend fun ByteWriteChannel.floorItemUpdate(update: FloorItemUpdate) {
    writeByte(update.tileOffset)
    writeShort(update.id)
    writeShort(update.stack)
    writeShort(update.combined)
}

private suspend fun ByteWriteChannel.graphicAddition(update: GraphicAddition) {
    writeByte(update.tileOffset)
    writeShort(update.id)
    writeByte(update.height)
    writeShort(update.delay)
    writeByte(update.rotation)
}

private suspend fun ByteWriteChannel.midiAddition(update: MidiAddition) {
    writeByte(update.tileOffset)
    writeShort(update.id)
    writeByte((update.radius shl 4) or update.repeat)
    writeByte(update.delay)
    writeByte(update.volume)
    writeShort(update.speed)
}

private suspend fun ByteWriteChannel.objectAddition(update: ObjectAddition) {
    writeByteSubtract((update.type shl 2) or update.rotation)
    writeShort(update.id)
    writeByteAdd(update.tileOffset)
}

private suspend fun ByteWriteChannel.objectAnimation(update: ObjectAnimation) {
    writeShortLittle(update.id)
    writeByteSubtract(update.tileOffset)
    writeByteInverse((update.type shl 2) or update.rotation)
}

private suspend fun ByteWriteChannel.objectRemoval(update: ObjectRemoval) {
    writeByteAdd((update.type shl 2) or update.rotation)
    writeByte(update.tileOffset)
}

private suspend fun ByteWriteChannel.projectileAddition(update: ProjectileAddition) {
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

private suspend fun ByteWriteChannel.soundAddition(update: SoundAddition) {
    writeByte(update.tileOffset)
    writeShort(update.id)
    writeByte((update.radius shl 4) or update.repeat)
    writeByte(update.delay)
    writeByte(update.volume)
    writeShort(update.speed)
}
