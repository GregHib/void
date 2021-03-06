package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeShortAddLittle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.CLIENT_VARC
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
 * @since July 04, 2020
 */
class VarcEncoder : Encoder(CLIENT_VARC) {

    /**
     * Client variable; also known as "ConfigGlobal"
     * @param id The config id
     * @param value The value to pass to the config
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(3) {
        writeByte(value)
        writeShortAddLittle(id)
    }
}

fun Player.sendVarc(id: Int, value: Int) {
    if(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        get<VarcEncoder>().encode(this, id, value)
    } else {
        get<VarcLargeEncoder>().encode(this, id, value)
    }
}