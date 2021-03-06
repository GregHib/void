package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeByteAdd
import world.gregs.voidps.buffer.write.writeByteSubtract
import world.gregs.voidps.buffer.write.writeShortAddLittle
import world.gregs.voidps.buffer.write.writeString
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.PLAYER_OPTION
import world.gregs.voidps.network.packet.PacketSize

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
        writeShortAddLittle(cursor)
        writeString(option)
        writeByteSubtract(slot)
        writeByteAdd(top)
    }
}