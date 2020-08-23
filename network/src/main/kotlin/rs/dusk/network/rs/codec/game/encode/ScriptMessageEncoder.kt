package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.SCRIPT
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class ScriptMessageEncoder : GameMessageEncoder<ScriptMessage>() {

    override fun encode(builder: PacketWriter, msg: ScriptMessage) {
        val (id, params) = msg
        builder.apply {
            writeOpcode(SCRIPT, PacketType.SHORT)
            val types = StringBuilder()
            for (param in params) {
                types.append(if (param is String) "s" else "i")
            }
            writeString(types.reverse().toString())
            for (param in params) {
                if (param is String) {
                    writeString(param)
                } else if (param is Int) {
                    writeInt(param)
                }
            }
            writeInt(id)
        }
    }
}