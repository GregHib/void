package world.gregs.voidps.network.codec.update.encode

import io.netty.channel.Channel
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.service.FileServerResponseCodes

class UpdateVersionEncoder : Encoder() {

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
            1330,
            79328,
            55571,
            46770,
            24563,
            299978,
            44375,
            0,
            4180,
            4327,
            136078,
            698736,
            191477,
            304748,
            414751,
            777769,
            18647,
            21950,
            16533,
            1248,
            9641,
            825,
            119,
            764210,
            1080257,
            3992,
            3013
        )
    }
}