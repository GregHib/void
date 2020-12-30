package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.write.writeString
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.INTERFACE_TEXT
import rs.dusk.network.packet.PacketSize

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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