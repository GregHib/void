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
            var parameterTypes = ""
            for (count in params.indices.reversed()) {
                parameterTypes += if (params[count] is String) "s" else "i"
            }
            writeString(parameterTypes)
            var index = 0
            for (count in parameterTypes.length - 1 downTo 0) {
                if (parameterTypes[count] == 's') {
                    writeString(params[index++] as String)
                } else {
                    writeInt(params[index++] as Int)
                }
            }
            writeInt(id)
        }
    }
}