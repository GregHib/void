package org.redrune.network.packet.access

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.redrune.network.packet.DataType
import org.redrune.network.packet.Endian
import org.redrune.network.packet.Modifier
import org.redrune.network.packet.PacketType
import org.redrune.tools.crypto.cipher.IsaacCipher

/**
 * All functions relative to writing directly to a packet are done by this class
 *
 * @author Greg Hibb
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
open class PacketWriter(
    protected var opcode: Int? = null,
    protected var type: PacketType = PacketType.FIXED,
    protected open val buffer: ByteBuf = Unpooled.buffer(),
    protected open val cipher: IsaacCipher? = null
) {
    private var sizeIndex = 0
    private var bitIndex = 0
    private var mode = AccessMode.BYTE

    fun writeOpcode(opcode: Int, type: PacketType) {
        this.type = type
        this.opcode = opcode
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

    /**
     * Writes a byte to [buffer].
     * @param value [Int]
     */
    fun writeByte(value: Int, type: Modifier = Modifier.NONE): PacketWriter {
        write(DataType.BYTE, value, type)
        return this
    }

    /**
     * Writes a [Short] to [buffer].
     * @param value [Int]
     */
    fun writeShort(value: Int, type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): PacketWriter {
        write(DataType.SHORT, value, type, order)
        return this
    }

    /**
     * Writes a Medium [Int] to [buffer]
     * @param value [Int]
     */
    fun writeMedium(value: Int): PacketWriter {
        write(DataType.MEDIUM, value)
        return this
    }

    /**
     * Writes a [Int] to [buffer].
     * @param value [Int]
     */
    fun writeInt(value: Int, type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): PacketWriter {
        write(DataType.INT, value, type, order)
        return this
    }

    /**
     * Writes a [Long] to [buffer].
     * @param value [Long]
     */
    fun writeLong(value: Long): PacketWriter {
        write(DataType.LONG, value)
        return this
    }

    /**
     * Writes a boolean as a byte to [buffer].
     * @param value [Boolean]
     */
    fun writeByte(value: Boolean, type: Modifier = Modifier.NONE): PacketWriter {
        return writeByte(if (value) 1 else 0, type)
    }

    fun writeSmart(value: Int): PacketWriter {
        if (value >= 128) {
            writeShort(value + 32768)
        } else {
            writeByte(value)
        }
        return this
    }

    fun writeString(value: String?): PacketWriter {
        if (value != null) {
            writeBytes(value.toByteArray())
        }
        writeByte(0)
        return this
    }

    fun writeGJString(value: String?): PacketWriter {
        writeByte(0)
        if (value != null) {
            writeBytes(value.toByteArray())
        }
        writeByte(0)
        return this
    }

    fun writeBytes(value: ByteArray): PacketWriter {
        buffer.writeBytes(value)
        return this
    }

    fun writeBytes(value: ByteBuf): PacketWriter {
        buffer.writeBytes(value)
        return this
    }

    fun writeBytes(data: ByteArray, offset: Int, length: Int): PacketWriter {
        buffer.writeBytes(data, offset, length)
        return this
    }

    fun writeBytes(data: ByteBuf, offset: Int, length: Int): PacketWriter {
        buffer.writeBytes(data, offset, length)
        return this
    }


    fun startBitAccess(): PacketWriter {
        /*if (mode == AccessMode.BIT) {
            throw IllegalStateException("Already in bit access mode")
        }*/
        mode = AccessMode.BIT
        bitIndex = buffer.writerIndex() * 8
        return this
    }

    fun finishBitAccess(): PacketWriter {
        /*if (mode == AccessMode.BYTE) {
            throw IllegalStateException("Already in byte access mode")
        }*/
        mode = AccessMode.BYTE
        buffer.writerIndex((bitIndex + 7) / 8)
        return this
    }

    fun writeBits(bitCount: Int, value: Boolean): PacketWriter {
        return writeBits(bitCount, if (value) 1 else 0)
    }

    fun writeBits(bitCount: Int, value: Int): PacketWriter {
//        checkBitAccess()
        var numBits = bitCount

        var bytePos = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        bitIndex += numBits

        var requiredSpace = bytePos - buffer.writerIndex() + 1
        requiredSpace += (numBits + 7) / 8
        buffer.ensureWritable(requiredSpace)

        while (numBits > bitOffset) {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and BIT_MASKS[bitOffset].inv()
            tmp = tmp or (value shr numBits - bitOffset and BIT_MASKS[bitOffset])
            buffer.setByte(bytePos++, tmp)
            numBits -= bitOffset
            bitOffset = 8
        }
        if (numBits == bitOffset) {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and BIT_MASKS[bitOffset].inv()
            tmp = tmp or (value and BIT_MASKS[bitOffset])
            buffer.setByte(bytePos, tmp)
        } else {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and (BIT_MASKS[numBits] shl bitOffset - numBits).inv()
            tmp = tmp or (value and BIT_MASKS[numBits] shl bitOffset - numBits)
            buffer.setByte(bytePos, tmp)
        }
        return this
    }

    fun skip(position: Int): PacketWriter {
        for (i in 0 until position) {
            writeByte(0)
        }
        return this
    }

    fun position(): Int {
        return buffer.writerIndex()
    }

    fun write(type: DataType, value: Number, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG) {
//        checkByteAccess()
        val longValue = value.toLong()
        when (order) {
            Endian.BIG, Endian.LITTLE -> {
                val range = if (order == Endian.LITTLE) 0 until type.length else type.length - 1 downTo 0
                for (i in range) {
                    if (i == 0 && modifier != Modifier.NONE) {
                        when (modifier) {
                            Modifier.ADD -> buffer.writeByte((longValue + 128).toByte().toInt())
                            Modifier.INVERSE -> buffer.writeByte((-longValue).toByte().toInt())
                            Modifier.SUBTRACT -> buffer.writeByte((128 - longValue).toByte().toInt())
                            else -> throw IllegalArgumentException("Unknown byte modifier")
                        }
                    } else {
                        buffer.writeByte((longValue shr i * 8).toByte().toInt())
                    }
                }
            }
            Endian.MIDDLE -> {
                if (modifier != Modifier.NONE && modifier != Modifier.INVERSE) {
                    throw IllegalArgumentException("Middle endian doesn't support variable modifier $modifier")
                }

                if (type != DataType.INT) {
                    throw IllegalArgumentException("Middle endian can only be used with an integer")
                }

                val range = listOf(8, 0, 24, 16)
                //Reverse range if inverse modifier
                for (i in if (modifier == Modifier.NONE) range else range.reversed()) {
                    buffer.writeByte((longValue shr i).toByte().toInt())
                }
            }
        }
    }

    /**
     * The packet write mode
     */
    private enum class AccessMode {
        BYTE,
        BIT;
    }

    /**
     * Checks the write mode is byte
     */
    private fun checkByteAccess() {
        if (mode != AccessMode.BYTE) {
            throw IllegalStateException("Can't write bytes while in bit access mode")
        }
    }

    /**
     * Checks the write mode is bit
     */
    private fun checkBitAccess() {
        if (mode != AccessMode.BIT) {
            throw IllegalStateException("Can't write bits while in byte access mode")
        }
    }

    companion object {
        /**
         * Bit masks for [writeBits]
         */
        private val BIT_MASKS = IntArray(32)

        init {
            for (i in BIT_MASKS.indices)
                BIT_MASKS[i] = (1 shl i) - 1
        }
    }


}