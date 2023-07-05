package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Client.Companion.SHORT
import world.gregs.voidps.network.Client.Companion.bits
import world.gregs.voidps.network.Protocol.REGION

fun Client.mapRegion(
    zoneX: Int,
    zoneY: Int,
    forceRefresh: Boolean,
    mapSize: Int,
    xteas: Array<IntArray>,
    clientIndex: Int? = null,
    clientTile: Int? = null,
    playerRegions: IntArray? = null
) = send(REGION, getLength(clientTile, playerRegions, clientIndex, xteas), SHORT) {
    mapInit(clientTile, playerRegions, clientIndex)
    writeByteSubtract(mapSize)
    writeShortAddLittle(zoneY)
    writeShortLittle(zoneX)
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
    count += xteas.sumOf { it.size * 4 }
    return count
}

/**
 * @param unknownMode (0 = only shows 1 region, 1 = ?, 2 = ?, 3 = ?, 4 = ?)
 */
fun Client.dynamicMapRegion(
    zoneX: Int,
    zoneY: Int,
    forceRefresh: Boolean,
    mapSize: Int,
    zones: List<Int?>,
    xteas: Array<IntArray>,
    clientIndex: Int? = null,
    clientTile: Int? = null,
    playerRegions: IntArray? = null,
    unknownMode: Int = 3
) = send(Protocol.DYNAMIC_REGION, getLength(clientTile, playerRegions, clientIndex, zones, xteas), SHORT) {
    mapInit(clientTile, playerRegions, clientIndex)
    writeByte(mapSize)
    writeShortAddLittle(zoneY)
    writeByte(forceRefresh)
    writeShortAdd(zoneX)
    writeByteAdd(unknownMode)
    bitAccess {
        zones.forEach { data ->
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

private fun getLength(clientTile: Int?, playerRegions: IntArray?, clientIndex: Int?, zones: List<Int?>, xteas: Array<IntArray>): Int {
    var count = 7
    count += getMapInitLength(clientTile, playerRegions, clientIndex)
    count += bits(zones.sumOf(::notNull))
    count += xteas.sumOf { it.size * 4 }
    return count
}

private fun notNull(it: Int?): Int {
    return if (it != null) 27 else 1
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