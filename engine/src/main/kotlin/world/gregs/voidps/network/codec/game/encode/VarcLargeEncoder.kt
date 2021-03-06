package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeIntLittle
import world.gregs.voidps.buffer.write.writeShortLittle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.CLIENT_VARC_LARGE

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
        writeIntLittle(value)
        writeShortLittle(id)
    }
}