package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeShort
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
        writeByte(type, Modifier.INVERSE)
        writeShort(id, Modifier.ADD)
    }
}