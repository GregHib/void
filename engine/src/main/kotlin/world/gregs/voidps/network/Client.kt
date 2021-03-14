package world.gregs.voidps.network

import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.buffer.write.writeSmart

data class Client(
    val write: ByteWriteChannel,
    val cipherIn: IsaacCipher,
    val cipherOut: IsaacCipher?
) {

    private var connected = true

    fun disconnect() {
        write.close()
        connected = false
    }

    fun flush() {

    }

    fun send(opcode: Int, size: Int, type: Int = PacketSize.FIXED, block: suspend ByteWriteChannel.() -> Unit) {
        if (!connected) {
            return
        }
        runBlocking(Dispatchers.IO) {
            write.header(opcode, type, size, cipherOut)
            block.invoke(write)
        }
    }

    private suspend fun ByteWriteChannel.header(opcode: Int, type: Int, size: Int, cipher: IsaacCipher?) {
        if (opcode < 0) {
            return
        }
        // Write opcode
        if (cipher != null) {
            if (opcode >= 128) {
                writeByte(((opcode shr 8) + 128) + cipher.nextInt())
                writeByte(opcode + cipher.nextInt())
            } else {
                writeByte(opcode + cipher.nextInt())
            }
        } else {
            writeSmart(opcode)
        }
        // Length
        when (type) {
            PacketSize.BYTE -> writeByte(size)
            PacketSize.SHORT -> writeShort(size)
        }
    }
}

fun smart(value: Int) = if (value >= 128) 2 else 1

fun string(value: String?) = (value?.length ?: 0) + 1

fun bits(bitCount: Int): Int {
    return (bitCount + 7) / 8
}