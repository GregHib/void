package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.buffer.write.writeString
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.CLIENT_VARC_STR
import world.gregs.voidps.network.packet.PacketSize
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
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