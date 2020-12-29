package rs.dusk.core.network.codec.message

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import rs.dusk.buffer.write.writeSmart
import rs.dusk.core.crypto.IsaacCipher
import rs.dusk.core.network.codec.getCipherOut
import rs.dusk.core.network.codec.packet.access.PacketSize
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.utility.get

abstract class MessageEncoder(
    var opcode: Int = -1,
    val type: Int = PacketSize.FIXED
) {

    private var sizeIndex = 0
    private val sessions = get<Sessions>()

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
    internal fun Player.send(size: Int, flush: Boolean = true, block: ByteBuf.() -> Unit) = sessions.get(this)?.send(size, flush, block)

    /**
     * Applies [block] and send [ByteBuf] with fixed [size]
     */
    internal fun Channel.send(size: Int, flush: Boolean = true, block: ByteBuf.() -> Unit) {
        val packet = packet(size)
        send(packet.apply(block), flush)
    }

    /**
     * Creates [ByteBuf] packet with fixed contents [size]
     */
    private fun Channel.packet(size: Int): ByteBuf {
        val cipher = getCipherOut()
        val buffer = alloc().buffer(headerSize(cipher) + size)
        if (opcode < 0) {
            sizeIndex = 0
            return buffer
        }
        // Write opcode
        if (cipher != null) {
            if (opcode >= 128) {
                buffer.writeByte(((opcode shr 8) + 128) + cipher.nextInt())
                buffer.writeByte(opcode + cipher.nextInt())
            } else {
                buffer.writeByte(opcode + cipher.nextInt())
            }
        } else {
            buffer.writeSmart(opcode)
        }
        // Save index where length is written
        sizeIndex = buffer.writerIndex()

        // Length placeholder
        when (type) {
            PacketSize.BYTE -> buffer.writeByte(0)
            PacketSize.SHORT -> buffer.writeShort(0)
        }
        return buffer
    }

    /**
     * Calculates byte count of a packets header
     */
    private fun headerSize(cipher: IsaacCipher?): Int {
        if (opcode < 0) {
            return 0
        }
        var count = if (cipher != null) {
            if (opcode >= 128) 2 else 1
        } else {
            smart(opcode)
        }

        when (type) {
            PacketSize.BYTE -> count += 1
            PacketSize.SHORT -> count += 2
        }
        return count
    }

    /**
     * Writes packet length and send to [Channel]
     */
    internal fun Channel.send(buffer: ByteBuf, flush: Boolean) {
        if (sizeIndex > 0) {
            val index = buffer.writerIndex()
            // The length of the headless packet
            val size = index - sizeIndex
            // Reset to the header size placeholder
            buffer.writerIndex(sizeIndex)
            // Write the packet length (accounting for placeholder)
            when (type) {
                PacketSize.BYTE -> buffer.writeByte(size - 1)
                PacketSize.SHORT -> buffer.writeShort(size - 2)
            }
            buffer.writerIndex(index)
        }
        if (flush) {
            writeAndFlush(buffer)
        } else {
            write(buffer)
        }
    }
}