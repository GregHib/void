package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.client.Client.Companion.bits
import world.gregs.voidps.network.login.Protocol
import world.gregs.voidps.network.login.Protocol.REGION
import world.gregs.voidps.network.login.protocol.*

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
    writeByteInverse(mapSize)
    writeByte(forceRefresh)
    writeShortAddLittle(zoneX)
    writeShort(zoneY)

    val allXtea = arrayOf(
        intArrayOf(1224902573, 373045525, 245718019, -2036093111),
        intArrayOf(-1901318074, -274793744, -1396129887, -1094522509),
        intArrayOf(453065651, -810686472, 1799060963, -1328396492),
        intArrayOf(1792091116, -1744526276, 56731698, 1034638680),
        intArrayOf(-1292180307, -1643453819, 700005461, 517833095),
        intArrayOf(307992456, -1799562678, -1780758542, -1910219722),
        intArrayOf(85559718, 230193585, -748681235, -1002325471),
        intArrayOf(1747320030, -1049932456, 903314780, 1020897411),
        intArrayOf(-1772232253, 838095486, -1167300216, -1478070642)
    )

    allXtea.forEach {
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

enum class DynamicRegionLoadType(val index: Int) {
    // not sure if zero is even a valid input to the packet glancing at the
    // client logic, it shouldn't really perform different than 1 as they both
    // set the loading type to the same value
    STANDARD(1),                   //No difference from non-dynamic logic essentially
    STANDARD_NO_ENTITY_RESET(2),   //Same as above except this one won't reset local entities on load (Matrix and most other bases default to this)
    LARGE(3),                      //Ignores render-distance restrictions on zone updates (current Void default)
    LARGE_NO_UPDATEZONE_RESET(4)   //Ignores render-distance restrictions on zone updates and doesn't reset objs, locs, etc on load
}

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
    loadType: DynamicRegionLoadType = DynamicRegionLoadType.LARGE
) = send(Protocol.DYNAMIC_REGION, getLength(clientTile, playerRegions, clientIndex, zones, xteas), SHORT) {
    mapInit(clientTile, playerRegions, clientIndex)
    writeShort(zoneY)
    writeByte(mapSize)
    writeByteSubtract(if (forceRefresh) 1 else 0)
    writeByteSubtract(loadType.index)
    writeShortAddLittle(zoneX)

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