package world.gregs.voidps.network.codec.login.encode

import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Encoder

/**
 * Client game or lobby login response code after a lobby connection request
 */
class LoginResponseEncoder : Encoder() {

    fun encode(
        session: ClientSession,
        opcode: Int
    ) = session.send(1) {
        writeByte(opcode)
    }
}