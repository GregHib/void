package rs.dusk.network.codec.update.encode

import io.netty.channel.Channel
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.service.FileServerResponseCodes

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
            104,
            79328,
            55571,
            46770,
            24563,
            299978,
            44375,
            0,
            4177,
            2822,
            99906,
            617909,
            155187,
            282646,
            330116,
            682557,
            18883,
            19031,
            16187,
            1248,
            6254,
            526,
            119,
            741285,
            821534,
            3671,
            2908
        )
    }
}