package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeIntLittle
import world.gregs.voidps.buffer.write.writeShortAdd
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.INTERFACE_COLOUR

/**
 * @author GregHib <greg@gregs.world>
 * @since August 2, 2020
 */
class InterfaceColourEncoder : Encoder(INTERFACE_COLOUR) {

    /**
     * Sends a sprite to a interface component
     * @param id The id of the parent interface
     * @param component The index of the component
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int,
        red: Int,
        green: Int,
        blue: Int
    ) = player.send(6) {
        writeShortAdd((red shl 10) + (green shl 5) + blue)
        writeIntLittle(id shl 16 or component)
    }
}