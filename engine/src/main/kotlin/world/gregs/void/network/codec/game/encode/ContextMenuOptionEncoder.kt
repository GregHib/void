package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.buffer.write.writeString
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.PLAYER_OPTION
import world.gregs.void.network.packet.PacketSize

/**
 * @author GregHib <greg@gregs.world>
 * @since August 16, 2020
 */
class ContextMenuOptionEncoder : Encoder(PLAYER_OPTION, PacketSize.BYTE) {

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