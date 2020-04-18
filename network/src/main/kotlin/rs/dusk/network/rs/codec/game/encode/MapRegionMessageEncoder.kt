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
        val (chunkX, chunkY, forceRefresh, mapSize, xteas, positions, location) = msg
        builder.apply {
            writeOpcode(REGION, PacketType.SHORT)

            if (positions != null && location != null) {
                startBitAccess()
                //Send current player position
                writeBits(30, location)

                //Update player locations
                positions.forEach { hash ->
                    writeBits(18, hash)
                }

                //Iterate up to max number of players
                //Positions doesn't include self & not zero indexed so +2
                for (i in positions.size + 2 until PLAYERS_LIMIT) {
                    writeBits(18, 0)
                }

                finishBitAccess()
            }
            writeByte(mapSize, Modifier.INVERSE)//Map type
            writeByte(forceRefresh)//Force next map load refresh
            writeShort(chunkX, order = Endian.LITTLE)
            writeShort(chunkY)
            //Needed as it is used to calculate number of regions
            xteas.forEach {
                it.forEach { key ->
                    writeInt(key)
                }
            }
        }
    }

    companion object {
        const val PLAYERS_LIMIT = 2048
    }
}