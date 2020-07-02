package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.SOUND_AREA
import rs.dusk.network.rs.codec.game.encode.message.SoundAreaMessage

/**
 * Incomplete
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class SoundAreaMessageEncoder : GameMessageEncoder<SoundAreaMessage>() {

    override fun encode(builder: PacketWriter, msg: SoundAreaMessage) {
        val (tile, id, type, rotation, three, four, five) = msg
        builder.apply {
            println(msg)
            writeOpcode(SOUND_AREA)
            writeByte(tile)
            writeShort(id)
            writeByte( (type shl 4) and rotation)
            writeByte(three)
            writeByte(four)
            writeShort(five)
        }
    }
}