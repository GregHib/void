package rs.dusk.network.rs.codec.login.encode

import io.netty.channel.Channel
import rs.dusk.core.network.codec.message.MessageEncoder

/**
 * Client game or lobby login response code after a lobby connection request
 */
class LoginResponseEncoder : MessageEncoder() {

    fun encode(
        channel: Channel,
        opcode: Int
    ) = channel.send(1) {
        writeByte(opcode)
    }
}