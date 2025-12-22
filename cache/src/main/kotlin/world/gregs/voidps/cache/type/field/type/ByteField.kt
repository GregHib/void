package world.gregs.voidps.cache.type.field.type

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.PrimitiveField
import world.gregs.voidps.cache.type.field.codec.ByteCodec
import world.gregs.voidps.cache.type.field.codec.UnsignedByteCodec

class ByteField(
    size: Int,
    override val default: Byte,
    override val codec: FieldCodec<Byte> = ByteCodec
) : PrimitiveField<Byte> {

    val data = ByteArray(size) { default }

    override fun get(index: Int) = data[index]

    override fun set(index: Int, value: Byte) {
        data[index] = value
    }

    override fun readDirect(reader: Reader) = reader.readBytes(data)

    override fun writeDirect(writer: Writer) = writer.writeBytes(data)

    override fun directSize(): Int = data.size

    override fun clear() = data.fill(default)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ByteField

        val otherValue = other.data
        return data.contentEquals(otherValue)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}

class UByteField(
    size: Int,
    override val default: Int,
    override val codec: FieldCodec<Int> = UnsignedByteCodec
) : PrimitiveField<Int> {

    val data = ByteArray(size) { default.toByte() }

    override fun get(index: Int) = data[index].toUByte().toInt()

    override fun set(index: Int, value: Int) {
        data[index] = value.toByte()
    }

    override fun readDirect(reader: Reader) = reader.readBytes(data)

    override fun writeDirect(writer: Writer) = writer.writeBytes(data)

    override fun directSize(): Int = data.size

    override fun clear() = data.fill(default.toByte())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UByteField

        val otherValue = other.data
        return data.contentEquals(otherValue)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}