package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.INTERFACE_ITEM

/**
 * @author GregHib <greg@gregs.world>
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
        writeInt(id shl 16 or component, Modifier.INVERSE, Endian.MIDDLE)
        writeInt(amount)
    }
}