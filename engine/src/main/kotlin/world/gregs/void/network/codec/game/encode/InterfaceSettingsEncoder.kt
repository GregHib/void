package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeInt
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_COMPONENT_SETTINGS
import world.gregs.void.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
        writeShort(fromSlot, order = Endian.LITTLE)
        writeInt(id shl 16 or component, Modifier.INVERSE, Endian.MIDDLE)
        writeShort(toSlot, Modifier.ADD)
        writeInt(settings, order = Endian.LITTLE)
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