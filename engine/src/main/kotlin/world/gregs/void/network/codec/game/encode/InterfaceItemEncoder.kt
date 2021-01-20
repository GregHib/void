package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeInt
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_ITEM

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceItemEncoder : Encoder(INTERFACE_ITEM) {

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