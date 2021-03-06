package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeByteInverse
import world.gregs.voidps.buffer.write.writeShortAdd
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.INTERFACE_WINDOW

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
class InterfaceUpdateEncoder : Encoder(INTERFACE_WINDOW) {

    fun encode(
        player: Player,
        id: Int,
        type: Int
    ) = player.send(3) {
        writeByteInverse(type)
        writeShortAdd(id)
    }
}