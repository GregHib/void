package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.DYNAMIC_REGION
import rs.dusk.network.rs.codec.game.GameOpcodes.REGION
import rs.dusk.network.rs.codec.game.encode.message.DynamicMapRegionMessage
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class DynamicMapRegionMessageEncoder : GameMessageEncoder<DynamicMapRegionMessage>() {

    override fun encode(builder: PacketWriter, msg: DynamicMapRegionMessage) {
        val (chunkX, chunkY, forceRefresh, mapSize, chunks, xteas, clientIndex, clientTile, playerRegions) = msg
        builder.apply {
            writeOpcode(DYNAMIC_REGION, PacketType.SHORT)
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
            writeShort(chunkY)
            writeByte(mapSize)
            writeByte(forceRefresh, Modifier.SUBTRACT)
            writeByte(3, Modifier.SUBTRACT)// Was at dynamic region? 5 or 3 TODO test
            writeShort(chunkX, order = Endian.LITTLE)
            startBitAccess()
            chunks.forEach { data ->
                writeBits(1, data != null)
                if(data != null) {
                    writeBits(26, data)
                }
            }
            finishBitAccess()
            xteas.forEach {
                it.forEach { key ->
                    writeInt(key)
                }
            }
        }
    }
}