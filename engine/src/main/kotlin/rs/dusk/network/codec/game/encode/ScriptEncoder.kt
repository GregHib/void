package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.write.writeString
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.SCRIPT
import rs.dusk.network.packet.PacketSize
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class ScriptEncoder : Encoder(SCRIPT, PacketSize.SHORT) {

    /**
     * Sends a client script to run
     * @param id The client script id
     * @param params Additional parameters to run the script with (strings & integers only)
     */
    fun encode(
        player: Player,
        id: Int,
        params: List<Any>
    ) = player.send(getLength(params)) {
        val types = StringBuilder()
        for (param in params) {
            types.append(if (param is String) "s" else "i")
        }
        writeString(types.toString())
        for (param in params.reversed()) {
            if (param is String) {
                writeString(param)
            } else if (param is Int) {
                writeInt(param)
            }
        }
        writeInt(id)
    }

    private fun getLength(params: List<Any>): Int {
        var count = 4
        count += params.size + 1
        count += params.sumBy { if (it is String) string(it) else if (it is Int) 4 else 0 }
        return count
    }
}

fun Player.sendScript(id: Int, vararg params: Any) {
    get<ScriptEncoder>().encode(this, id, params.toList())
}

fun Player.sendScript(id: Int, params: List<Any>) {
    get<ScriptEncoder>().encode(this, id, params.toList())
}