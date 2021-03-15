package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeSmart
import world.gregs.voidps.buffer.write.writeString
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.CHAT
import world.gregs.voidps.network.PacketSize
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
 * @since July 27, 2020
 */
class ChatEncoder : Encoder(CHAT, PacketSize.BYTE) {

    /**
     * A chat box message to display
     * @param type The message type
     * @param tile The tile the message was sent from
     * @param name Optional display name?
     * @param message The chat message text
     */
    fun encode(
        player: Player,
        type: Int,
        tile: Int,
        message: String,
        name: String?,
        formatted: String?
    ) {
        val mask = getMask(name, formatted)
        player.send(getLength(type, message, name, mask, formatted)) {
            writeSmart(type)
            writeInt(tile)
            writeByte(mask)
            if (name != null) {
                writeString(name)
                if (mask and 0x2 == 0x2) {
                    writeString(formatted)
                }
            }
            writeString(message)
        }
    }

    private fun getMask(name: String?, formatted: String?): Int {
        var mask = 0
        if (name != null) {
            mask = mask or 0x1
            if (formatted != null && name != formatted) {
                mask = mask or 0x2
            }
        }
        return mask
    }

    private fun getLength(type: Int, message: String, name: String?, mask: Int, formatted: String?): Int {
        var length = smart(type) + 5 + string(message)
        if (name != null) {
            length += string(name)
            if (mask and 0x2 == 0x2) {
                length += string(formatted)
            }
        }
        return length
    }
}

fun Player.message(text: String, type: ChatType = ChatType.Game, tile: Int = 0, name: String? = null) {
    get<ChatEncoder>()
        .encode(this, type.id, tile, text, name, name?.toLowerCase()?.replace(" ", "_"))
}