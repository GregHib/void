package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.*
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.REGION
import rs.dusk.network.packet.PacketSize

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class MapRegionEncoder : Encoder(REGION, PacketSize.SHORT) {

    fun encode(
        player: Player,
        chunkX: Int,
        chunkY: Int,
        forceRefresh: Boolean,
        mapSize: Int,
        xteas: Array<IntArray>,
        clientIndex: Int? = null,
        clientTile: Int? = null,
        playerRegions: IntArray? = null
    ) = player.send(getLength(clientTile, playerRegions, clientIndex, xteas)) {
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
        writeByte(mapSize, Modifier.INVERSE)
        writeByte(forceRefresh)
        writeShort(chunkX, order = Endian.LITTLE)
        writeShort(chunkY)
        xteas.forEach {
            it.forEach { key ->
                writeInt(key)
            }
        }
    }

    private fun getLength(clientTile: Int?, playerRegions: IntArray?, clientIndex: Int?, xteas: Array<IntArray>): Int {
        var count = 6
        if (clientTile != null && playerRegions != null && clientIndex != null) {
            var bits = 30
            playerRegions.forEachIndexed { index, _ ->
                if (index != clientIndex) {
                    bits += 18
                }
            }
            count += bits(bits)
        }
        count += xteas.sumBy { it.size * 4 }
        return count
    }
}