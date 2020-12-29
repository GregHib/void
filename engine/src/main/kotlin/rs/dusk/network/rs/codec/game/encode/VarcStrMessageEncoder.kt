package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeShort
import rs.dusk.buffer.write.writeString
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketSize
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARC_STR
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarcStrMessageEncoder : MessageEncoder(CLIENT_VARC_STR, PacketSize.BYTE) {

    /**
     * Client variable; also known as "GlobalString"
     * @param id The config id
     * @param value The value to pass to the config
     */
    fun encode(
        player: Player,
        id: Int,
        value: String
    ) = player.send(2 + string(value)) {
        writeShort(id, Modifier.ADD, Endian.LITTLE)
        writeString(value)
    }
}

fun Player.sendVarcStr(id: Int, value: String) {
    get<VarcStrMessageEncoder>().encode(this, id, value)
}