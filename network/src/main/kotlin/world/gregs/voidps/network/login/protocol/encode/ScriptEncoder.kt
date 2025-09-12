package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.writeInt
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.login.Protocol.SCRIPT
import world.gregs.voidps.network.login.protocol.writeByte
import world.gregs.voidps.network.login.protocol.writeText

/**
 * Sends a client script to run
 * @param id The client script id
 * @param params Additional parameters to run the script with (strings & integers only)
 */
fun Client.sendScript(
    id: Int,
    params: List<Any?>,
) = send(SCRIPT, getLength(params), SHORT) {
    val types = StringBuilder()
    for (param in params) {
        types.append(if (param == null || param is String) "s" else "i")
    }
    writeText(types.toString())
    for (param in params.reversed()) {
        when (param) {
            null -> writeByte(0)
            is String -> writeText(param)
            is Int -> writeInt(param)
        }
    }
    writeInt(id)
}

private fun getLength(params: List<Any?>): Int {
    var count = 4
    count += params.size + 1
    count += params.sumOf {
        when (it) {
            null -> 1
            is String -> string(it)
            is Int -> 4
            else -> 0
        }
    }
    return count
}
