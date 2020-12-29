package rs.dusk.network.rs.codec.update.encode

import io.netty.channel.Channel
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.network.rs.codec.service.FileServerResponseCodes

class UpdateVersionMessageEncoder : MessageEncoder() {

    fun encode(
        channel: Channel,
        response: Int
    ) = channel.send(getLength(response)) {
        writeByte(response)
        if (response == FileServerResponseCodes.JS5_RESPONSE_OK) {
            GRAB_SERVER_KEYS.forEach {
                writeInt(it)
            }
        }
    }

    private fun getLength(response: Int): Int {
        return if (response == FileServerResponseCodes.JS5_RESPONSE_OK) 1 + GRAB_SERVER_KEYS.size * 4 else 1
    }

    companion object {

        private val GRAB_SERVER_KEYS = intArrayOf(
            1362,
            77448,
            44880,
            39771,
            24563,
            363672,
            44375,
            0,
            1614,
            0,
            5340,
            142976,
            741080,
            188204,
            358294,
            416732,
            828327,
            19517,
            22963,
            16769,
            1244,
            11976,
            10,
            15,
            119,
            817677,
            1624243
        )
    }
}