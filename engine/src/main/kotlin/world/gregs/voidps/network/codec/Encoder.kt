package world.gregs.voidps.network.codec

import io.ktor.utils.io.*
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.buffer.write.writeSmart
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.crypto.IsaacCipher
import world.gregs.voidps.network.packet.PacketSize
import world.gregs.voidps.utility.inject

abstract class Encoder(
    var opcode: Int = -1,
    val type: Int = PacketSize.FIXED
) {

    private val sessions: Sessions by inject()

    /**
     * Calculates number of bytes used for a smart with [value]
     */
    internal fun smart(value: Int) = if (value >= 128) 2 else 1

    /**
     * Calculates number of bytes for [string]
     */
    internal fun string(value: String?) = (value?.length ?: 0) + 1

    /**
     * Calculates number of bytes for [bitCount]
     */
    internal fun bits(bitCount: Int): Int {
        return (bitCount + 7) / 8
    }

    /**
     * Applies [block] and send [ByteBuf] with fixed [size]
     */
    internal fun Player.send(size: Int, flush: Boolean = true, block: suspend ByteWriteChannel.() -> Unit) = sessions.get(this)?.send(size, flush, block)

    /**
     * Applies [block] and send [ByteBuf] with fixed [size]
     */
    internal fun ClientSession.send(size: Int, flush: Boolean = true, block: suspend ByteWriteChannel.() -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {

            write.header(size, cipherOut)
            block.invoke(write)
//            if(flush) {
//                write.flush()
//            }
        }
        runBlocking {
        }
    }

    /**
     * Creates [ByteBuf] packet with fixed contents [size]
     */
    private suspend fun ByteWriteChannel.header(size: Int, cipher: IsaacCipher?) {
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