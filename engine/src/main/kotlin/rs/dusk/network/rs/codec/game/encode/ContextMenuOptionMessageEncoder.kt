package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeShort
import rs.dusk.buffer.write.writeString
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketSize
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 16, 2020
 */
class ContextMenuOptionMessageEncoder : MessageEncoder(PLAYER_OPTION, PacketSize.BYTE) {

    /**
     * Sends a player right click option
     * @param option The option
     * @param slot The index of the option
     * @param top Whether it should be forced to the top?
     * @param cursor Unknown value
     */
    fun encode(
        player: Player,
        option: String?,
        slot: Int,
        top: Boolean,
        cursor: Int = -1
    ) = player.send(4 + string(option)) {
        writeByte(top, Modifier.ADD)
        writeShort(cursor, order = Endian.LITTLE)
        writeString(option)
        writeByte(slot, Modifier.INVERSE)
    }
}