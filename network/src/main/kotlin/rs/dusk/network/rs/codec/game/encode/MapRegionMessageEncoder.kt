package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.REGION
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class MapRegionMessageEncoder : GameMessageEncoder<MapRegionMessage>() {

    override fun encode(builder: PacketWriter, msg: MapRegionMessage) {
        val (chunkX, chunkY, forceRefresh, mapSize, xteas, clientIndex, clientTile, playerRegions) = msg
        builder.apply {
            writeOpcode(REGION, PacketType.SHORT)
            if (playerRegions != null && clientTile != null && clientIndex != null) {
                startBitAccess()
                writeBits(30, clientTile)
                playerRegions.forEachIndexed { index, region ->
                    if (index != clientIndex) {
                        writeBits(18, region)
                    }
                }
                finishBitAccess()
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
    }
}