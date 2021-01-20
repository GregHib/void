package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.*
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.REGION
import world.gregs.void.network.packet.PacketSize

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