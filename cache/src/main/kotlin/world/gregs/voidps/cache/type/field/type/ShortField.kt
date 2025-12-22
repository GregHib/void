package world.gregs.voidps.cache.type.field.type

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.PrimitiveField
import world.gregs.voidps.cache.type.field.codec.ShortCodec
import world.gregs.voidps.cache.type.field.codec.UnsignedShortCodec

class ShortField(
    size: Int,
    override val default: Short,
    override val codec: FieldCodec<Short> = ShortCodec
) : PrimitiveField<Short> {

    val data = ShortArray(size) { default }

    override fun get(index: Int) = data[index]

    override fun set(index: Int, value: Short) {
        data[index] = value
    }

    override fun readDirect(reader: Reader) = reader.readBytes(data)

    override fun writeDirect(writer: Writer) = writer.writeBytes(data)

    override fun directSize(): Int = data.size * codec.bytes(default)

    override fun clear() = data.fill(default)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShortField

        val otherValue = other.data
        return data.contentEquals(otherValue)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}

class UShortField(
    size: Int,
    override val default: Int,
    override val codec: FieldCodec<Int> = UnsignedShortCodec
) : PrimitiveField<Int> {

    val data = ShortArray(size) { default.toShort() }

    override fun get(index: Int) = data[index].toInt()

    override fun set(index: Int, value: Int) {
        data[index] = value.toShort()
    }

    override fun readDirect(reader: Reader) = reader.readBytes(data)

    override fun writeDirect(writer: Writer) = writer.writeBytes(data)

    override fun directSize(): Int = data.size * codec.bytes(default)

    override fun clear() = data.fill(default.toShort())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UShortField

        val otherValue = other.data
        return data.contentEquals(otherValue)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}