package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.write.writeString
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_TEXT
import world.gregs.void.network.packet.PacketSize

/**
 * @author GregHib <greg@gregs.world>
 * @since August 2, 2020
 */
class InterfaceTextEncoder : Encoder(INTERFACE_TEXT, PacketSize.SHORT) {

    /**
     * Update the text of a interface component
     * @param id The id of the parent interface
     * @param component The index of the component
     * @param text The text to send
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int,
        text: String
    ) = player.send(4 + string(text)) {
        writeInt(id shl 16 or component)
        writeString(text)
    }
}