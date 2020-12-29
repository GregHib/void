package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeInt
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ITEM

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceItemMessageEncoder : MessageEncoder(INTERFACE_ITEM) {

    /**
     * Sends an item to display on a interface component
     * @param id The id of the parent interface
     * @param component The index of the component
     * @param item The item id
     * @param amount The number of the item
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int,
        item: Int,
        amount: Int
    ) = player.send(10) {
        writeShort(item, order = Endian.LITTLE)
        writeInt(amount)
        writeInt(id shl 16 or component, Modifier.INVERSE, Endian.MIDDLE)
    }
}