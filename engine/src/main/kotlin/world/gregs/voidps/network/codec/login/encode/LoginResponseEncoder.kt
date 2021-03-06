package world.gregs.voidps.network.codec.login.encode

import io.netty.channel.Channel
import world.gregs.voidps.network.codec.Encoder

/**
 * Client game or lobby login response code after a lobby connection request
 */
class LoginResponseEncoder : Encoder() {

    fun encode(
        channel: Channel,
        opcode: Int
    ) = channel.send(1) {
        writeByte(opcode)
    }
}