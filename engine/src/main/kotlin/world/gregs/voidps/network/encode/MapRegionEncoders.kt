package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*
import world.gregs.voidps.network.GameOpcodes.REGION
import world.gregs.voidps.network.PacketSize.SHORT

fun Client.mapRegion(
    chunkX: Int,
    chunkY: Int,
    forceRefresh: Boolean,
    mapSize: Int,
    xteas: Array<IntArray>,
    clientIndex: Int? = null,
    clientTile: Int? = null,
    playerRegions: IntArray? = null
) = send(REGION, getLength(clientTile, playerRegions, clientIndex, xteas), SHORT) {
    mapInit(clientTile, playerRegions, clientIndex)
    writeByteSubtract(mapSize)
    writeShortAddLittle(chunkY)
    writeShortLittle(chunkX)
    writeByteInverse(forceRefresh)
    xteas.forEach {
        it.forEach { key ->
            writeInt(key)
        }
    }
}

suspend fun ByteWriteChannel.mapInit(clientTile: Int?, playerRegions: IntArray?, clientIndex: Int?) {
    if (clientTile != null && playerRegions != null && clientIndex != null) {
        bitAccess {
            writeBits(30, clientTile)
            playerRegions.forEachIndexed { index, region ->
                if (index != clientIndex) {
                    writeBits(18, region)
                }
            }
        }
    }
}

private fun getLength(clientTile: Int?, playerRegions: IntArray?, clientIndex: Int?, xteas: Array<IntArray>): Int {
    var count = 6
    count += getMapInitLength(clientTile, playerRegions, clientIndex)
    count += xteas.sumBy { it.size * 4 }
    return count
}

fun Client.dynamicMapRegion(
    chunkX: Int,
    chunkY: Int,
    forceRefresh: Boolean,
    mapSize: Int,
    chunks: List<Int?>,
    xteas: Array<IntArray>,
    clientIndex: Int? = null,
    clientTile: Int? = null,
    playerRegions: IntArray? = null
) = send(GameOpcodes.DYNAMIC_REGION, getLength(clientTile, playerRegions, clientIndex, chunks, xteas), PacketSize.SHORT) {
    mapInit(clientTile, playerRegions, clientIndex)
    writeByte(mapSize)
    writeShortAddLittle(chunkY)
    writeByte(forceRefresh)
    writeShortAdd(chunkX)
    writeByteAdd(3)// Was at dynamic region? 5 or 3 TODO test
    bitAccess {
        chunks.forEach { data ->
            writeBit(data != null)
            if (data != null) {
                writeBits(26, data)
            }
        }
    }
    xteas.forEach {
        it.forEach { key ->
            writeInt(key)
        }
    }
}

private fun getLength(clientTile: Int?, playerRegions: IntArray?, clientIndex: Int?, chunks: List<Int?>, xteas: Array<IntArray>): Int {
    var count = 7
    count += getMapInitLength(clientTile, playerRegions, clientIndex)
    count += bits(chunks.sumBy { if (it != null) 27 else 1 })
    count += xteas.sumBy { it.size * 4 }
    return count
}

private fun getMapInitLength(clientTile: Int?, playerRegions: IntArray?, clientIndex: Int?): Int {
    if (clientTile != null && playerRegions != null && clientIndex != null) {
        var bits = 30
        playerRegions.forEachIndexed { index, _ ->
            if (index != clientIndex) {
                bits += 18
            }
        }
        return bits(bits)
    }
    return 0
}