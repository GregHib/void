package world.gregs.voidps.network.client

import io.ktor.utils.io.*

class DummyClient : Client(ByteChannel(false), IsaacCipher(IntArray(4)), null, "") {
    override fun flush() {
    }

    override fun send(opcode: Int, block: suspend ByteWriteChannel.() -> Unit) {
    }

    override fun send(opcode: Int, size: Int, type: Int, block: suspend ByteWriteChannel.() -> Unit) {
    }
}
