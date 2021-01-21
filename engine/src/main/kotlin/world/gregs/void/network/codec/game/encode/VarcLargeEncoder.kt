package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.CLIENT_VARC_LARGE

/**
 * @author GregHib <greg@gregs.world>
 * @since July 04, 2020
 */
class VarcLargeEncoder : Encoder(CLIENT_VARC_LARGE) {

    /**
     * Client variable; also known as "ConfigGlobal"
     * @param id The config id
     * @param value The value to pass to the config
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(6) {
        writeShort(id, order = Endian.LITTLE)
        writeInt(value)
    }
}