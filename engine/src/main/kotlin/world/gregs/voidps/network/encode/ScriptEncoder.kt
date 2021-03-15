package world.gregs.voidps.network.encode

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Client.Companion.SHORT
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.GameOpcodes.SCRIPT
import world.gregs.voidps.network.writeString

/**
 * Sends a client script to run
 * @param id The client script id
 * @param params Additional parameters to run the script with (strings & integers only)
 */
fun Player.sendScript(
    id: Int,
    vararg params: Any
) = sendScript(id, params.toList())

fun Player.sendScript(
    id: Int,
    params: List<Any>
) {
    client?.send(SCRIPT, getLength(params), SHORT) {
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
}

private fun getLength(params: List<Any>): Int {
    var count = 4
    count += params.size + 1
    count += params.sumBy { if (it is String) string(it) else if (it is Int) 4 else 0 }
    return count
}