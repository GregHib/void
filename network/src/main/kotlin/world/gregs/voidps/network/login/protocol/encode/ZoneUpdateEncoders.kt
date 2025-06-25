package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol
import world.gregs.voidps.network.login.protocol.*
import world.gregs.voidps.network.login.protocol.encode.zone.*

fun encodeBatch(messages: Collection<ZoneUpdate>): ByteArray {
    val writeChannel = ByteArrayChannel()
    runBlocking {
        messages.forEach { update ->
            writeChannel.writeByte(update.packetIndex.toByte())
            writeChannel.encode(update)
        }
    }
    return writeChannel.toByteArray()
}

fun Client.sendBatch(messages: ByteArray, zoneOffsetX: Int, zoneOffsetY: Int, zoneLevel: Int) {
    send(Protocol.BATCH_UPDATE_ZONE, messages.size + 3, Client.SHORT) {
        p1Alt2(zoneOffsetX)
        p1Alt1(zoneLevel)
        writeByte(zoneOffsetY)
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
    p1Alt2(offset(update.tile))
    p2Alt2(update.id)
    writeShort(update.amount)
}

private suspend fun ByteWriteChannel.floorItemRemoval(update: FloorItemRemoval) {
    writeShort(update.id)
    writeByte(offset(update.tile))
}

private suspend fun ByteWriteChannel.floorItemReveal(update: FloorItemReveal) {
    p2Alt2(update.ownerIndex)
    p1Alt1(offset(update.tile))
    ip2(update.id)
    writeShort(update.amount)
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
    writeByte(offset(update.tile))
    writeByte((update.type shl 2) or update.rotation)
    p2Alt2(update.id)
}

private suspend fun ByteWriteChannel.objectAnimation(update: ObjectAnimation) {
    p2Alt3(update.id)
    p1Alt3(offset(update.tile))
    writeByte((update.type shl 2) or update.rotation)
}

private suspend fun ByteWriteChannel.objectRemoval(update: ObjectRemoval) {
    p1Alt2(offset(update.tile))
    writeByte((update.type shl 2) or update.rotation)
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