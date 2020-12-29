package rs.dusk.core.network.codec.packet.access

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import rs.dusk.buffer.write.BufferWriter
import rs.dusk.core.crypto.IsaacCipher
import rs.dusk.core.network.model.packet.PacketType

/**
 * All functions relative to writing directly to a packet are done by this class
 *
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since February 18, 2020
 */
open class PacketWriter(
    buffer: ByteBuf = Unpooled.buffer(),
    protected open val cipher: IsaacCipher? = null
) : BufferWriter(buffer) {
    private var sizeIndex = 0
    protected var type: PacketType = PacketType.FIXED

    fun writeOpcode(opcode: Int, type: PacketType = PacketType.FIXED) {
        this.type = type
        if (cipher != null) {
            if (opcode >= 128) {
                writeByte(((opcode shr 8) + 128) + cipher!!.nextInt())
                writeByte(opcode + cipher!!.nextInt())
            } else {
                writeByte(opcode + cipher!!.nextInt())
            }
        } else {
            writeSmart(opcode)
        }
        //Write opcode
        //Save index where size is written
        sizeIndex = buffer.writerIndex()
        //Write length placeholder
        when (type) {
            PacketType.BYTE -> writeByte(0)
            PacketType.SHORT -> writeShort(0)
            else -> {
            }
        }
    }

    fun writeSize() {
        if (sizeIndex > 0) {
            val index = buffer.writerIndex()
            //The length of the headless packet
            val size = index - sizeIndex
            //Reset to the header size placeholder
            buffer.writerIndex(sizeIndex)
            //Write the packet length (accounting for placeholder)
            when (type) {
                PacketType.BYTE -> writeByte(size - 1)
                PacketType.SHORT -> writeShort(size - 2)
                else -> {
                }
            }
            buffer.writerIndex(index)
        }
    }

}