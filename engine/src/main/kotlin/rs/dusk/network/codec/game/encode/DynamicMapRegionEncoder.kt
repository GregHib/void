package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.*
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.DYNAMIC_REGION
import rs.dusk.network.packet.PacketSize

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class DynamicMapRegionEncoder : Encoder(DYNAMIC_REGION, PacketSize.SHORT) {

    fun encode(
        player: Player,
        chunkX: Int,
        chunkY: Int,
        forceRefresh: Boolean,
        mapSize: Int,
        chunks: List<Int?>,
        xteas: Array<IntArray>,
        clientIndex: Int? = null,
        clientTile: Int? = null,
        playerRegions: IntArray? = null
    ) = player.send(getLength(clientTile, playerRegions, clientIndex, chunks, xteas)) {
        if (clientTile != null && playerRegions != null && clientIndex != null) {
            var bitIndex = startBitAccess()
            bitIndex += writeBits(bitIndex, 30, clientTile)
            playerRegions.forEachIndexed { index, region ->
                if (index != clientIndex) {
                    bitIndex += writeBits(bitIndex, 18, region)
                }
            }
            finishBitAccess(bitIndex)
        }
        writeShort(chunkY)
        writeByte(mapSize)
        writeByte(forceRefresh, Modifier.SUBTRACT)
        writeByte(3, Modifier.SUBTRACT)// Was at dynamic region? 5 or 3 TODO test
        writeShort(chunkX, order = Endian.LITTLE)
        var bitIndex = startBitAccess()
        chunks.forEach { data ->
            bitIndex += writeBits(bitIndex, 1, data != null)
            if (data != null) {
                bitIndex += writeBits(bitIndex, 26, data)
            }
        }
        finishBitAccess(bitIndex)
        xteas.forEach {
            it.forEach { key ->
                writeInt(key)
            }
        }
    }

    private fun getLength(clientTile: Int?, playerRegions: IntArray?, clientIndex: Int?, chunks: List<Int?>, xteas: Array<IntArray>): Int {
        var count = 7
        if (clientTile != null && playerRegions != null && clientIndex != null) {
            var bits = 30
            playerRegions.forEachIndexed { index, _ ->
                if (index != clientIndex) {
                    bits += 18
                }
            }
            count += bits(bits)
        }
        count += bits(chunks.sumBy { if (it != null) 27 else 1 })
        count += xteas.sumBy { it.size * 4 }
        return count
    }
}