package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.REGION
import world.gregs.voidps.network.packet.PacketSize

/**
 * @author GregHib <greg@gregs.world>
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
            bitAccess {
                writeBits(30, clientTile)
                playerRegions.forEachIndexed { index, region ->
                    if (index != clientIndex) {
                        writeBits(18, region)
                    }
                }
            }
        }
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