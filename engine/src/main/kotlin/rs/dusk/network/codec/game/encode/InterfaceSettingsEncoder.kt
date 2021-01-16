package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeInt
import rs.dusk.buffer.write.writeShort
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.INTERFACE_COMPONENT_SETTINGS
import rs.dusk.utility.get

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