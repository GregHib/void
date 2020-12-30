package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeShort
import rs.dusk.buffer.write.writeString
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.CLIENT_VARC_STR
import rs.dusk.network.packet.PacketSize
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarcStrEncoder : Encoder(CLIENT_VARC_STR, PacketSize.BYTE) {

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
    get<VarcStrEncoder>().encode(this, id, value)
}