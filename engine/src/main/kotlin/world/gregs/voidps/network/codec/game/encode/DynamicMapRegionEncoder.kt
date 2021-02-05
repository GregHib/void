package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.DYNAMIC_REGION
import world.gregs.voidps.network.packet.PacketSize

/**
 * @author GregHib <greg@gregs.world>
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
        writeByte(mapSize)
        writeShort(chunkY, Modifier.ADD, Endian.LITTLE)
        writeByte(forceRefresh)
        writeShort(chunkX, Modifier.ADD)
        writeByte(3, Modifier.ADD)// Was at dynamic region? 5 or 3 TODO test
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