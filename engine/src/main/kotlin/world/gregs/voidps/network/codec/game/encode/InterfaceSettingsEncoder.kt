package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.INTERFACE_COMPONENT_SETTINGS
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
 * @since July 26, 2020
 */
class InterfaceSettingsEncoder : Encoder(INTERFACE_COMPONENT_SETTINGS) {

    /**
     * Sends settings to a interface's component(s)
     * @param id The id of the parent window
     * @param component The index of the component
     * @param fromSlot The start slot index
     * @param toSlot The end slot index
     * @param settings The settings hash
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int,
        fromSlot: Int,
        toSlot: Int,
        settings: Int
    ) = player.send(12) {
        writeShort(fromSlot, type = Modifier.ADD)
        writeShort(toSlot, order = Endian.LITTLE)
        writeInt(id shl 16 or component)
        writeInt(settings, type = Modifier.INVERSE, order = Endian.MIDDLE)
    }
}

fun Player.sendInterfaceSettings(
    id: Int,
    component: Int,
    fromSlot: Int,
    toSlot: Int,
    settings: Int
) {
    get<InterfaceSettingsEncoder>().encode(this, id, component, fromSlot, toSlot, settings)
}