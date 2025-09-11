package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol
import world.gregs.voidps.network.login.protocol.*
import world.gregs.voidps.network.login.protocol.encode.zone.*

fun encodeBatch(messages: Collection<ZoneUpdate>): ByteArray {
    val writeChannel = ByteChannel(true)
    return runBlocking {
        messages.forEach { update ->
            writeChannel.writeByte(update.packetIndex.toByte())
            writeChannel.encode(update)
        }
        val buffer = ByteArray(writeChannel.availableForRead)
        if (buffer.isNotEmpty()) {
            writeChannel.readAvailable(buffer)
        }
        buffer
    }
}

fun Client.sendBatch(messages: ByteArray, zoneOffsetX: Int, zoneOffsetY: Int, zoneLevel: Int) {
    send(Protocol.BATCH_UPDATE_ZONE, messages.size + 3, Client.SHORT) {
        writeByteInverse(zoneOffsetX)
        writeByteSubtract(zoneLevel)
        writeByteSubtract(zoneOffsetY)
        writeBytes(messages)
    }
}

fun Client.send(update: ZoneUpdate) {
    send(update.packetId) {
        encode(update)
    }
}

suspend fun ByteWriteChannel.encode(update: ZoneUpdate) {
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
    writeByte(offset(update.tile))
}

private suspend fun ByteWriteChannel.floorItemRemoval(update: FloorItemRemoval) {
    writeShortAddLittle(update.id)
    writeByteSubtract(offset(update.tile))
}

private suspend fun ByteWriteChannel.floorItemReveal(update: FloorItemReveal) {
    writeShortLittle(update.amount)
    writeByte(offset(update.tile))
    writeShortAdd(update.id)
    writeShortAdd(update.ownerIndex)
}

private suspend fun ByteWriteChannel.floorItemUpdate(update: FloorItemUpdate) {
    writeByte(offset(update.tile))
    writeShort(update.id)
    writeShort(update.stack)
    writeShort(update.combined)
}

private suspend fun ByteWriteChannel.graphicAddition(update: GraphicAddition) {
    writeByte(offset(update.tile))
    writeShort(update.id)
    writeByte(update.height)
    writeShort(update.delay)
    writeByte(update.rotation)
}

private suspend fun ByteWriteChannel.midiAddition(update: MidiAddition) {
    writeByte(offset(update.tile))
    writeShort(update.id)
    writeByte((update.radius shl 4) or update.repeat)
    writeByte(update.delay)
    writeByte(update.volume)
    writeShort(update.speed)
}

private suspend fun ByteWriteChannel.objectAddition(update: ObjectAddition) {
    writeByteSubtract((update.type shl 2) or update.rotation)
    writeShort(update.id)
    writeByteAdd(offset(update.tile))
}

private suspend fun ByteWriteChannel.objectAnimation(update: ObjectAnimation) {
    writeShortLittle(update.id)
    writeByteSubtract(offset(update.tile))
    writeByteInverse((update.type shl 2) or update.rotation)
}

private suspend fun ByteWriteChannel.objectRemoval(update: ObjectRemoval) {
    writeByteAdd((update.type shl 2) or update.rotation)
    writeByte(offset(update.tile))
}

private suspend fun ByteWriteChannel.projectileAddition(update: ProjectileAddition) {
    writeByte(offset(update.tile, 3))
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
    writeByte(offset(update.tile))
    writeShort(update.id)
    writeByte((update.radius shl 4) or update.repeat)
    writeByte(update.delay)
    writeByte(update.volume)
    writeShort(update.speed)
}

private fun offset(tile: Int, bit: Int = 4) = ((tile shr 14 and 0x3fff).rem(8) shl bit) or (tile and 0x3fff).rem(8)
