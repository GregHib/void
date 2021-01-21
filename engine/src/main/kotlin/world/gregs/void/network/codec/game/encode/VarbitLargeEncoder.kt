package world.gregs.void.network.codec.game.encode

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.CLIENT_VARBIT_LARGE

/**
 * @author GregHib <greg@gregs.world>
 * @since July 04, 2020
 */
class VarbitLargeEncoder : Encoder(CLIENT_VARBIT_LARGE) {

    /**
     * A variable bit; also known as "ConfigFile", known in the client as "clientvarpbit"
     * @param id The file id
     * @param value The value to pass to the config file
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(6) {
        writeInt(value)
        writeShort(id)
    }
}