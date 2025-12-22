package world.gregs.voidps.cache.type.field.custom

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.codec.ShortCodec
import world.gregs.voidps.cache.type.field.codec.UnsignedByteCodec

/**
 * Used for colours
 *
 * ```
 * val size = buffer.readUnsignedByte()
 * originalColours = ShortArray(size)
 * modifiedColours = ShortArray(size)
 * for (count in 0 until size) {
 *     originalColours!![count] = buffer.readShort()
 *     modifiedColours!![count] = buffer.readShort()
 * }
 * ```
 */
class ShortArraysField(
    size: Int,
    firstKey: String,
    secondKey: String,
    val fieldCodec: FieldCodec<Short> = ShortCodec,
    sizeCodec: FieldCodec<Int> = UnsignedByteCodec,
) : NullArraysField<ShortArray>(size, firstKey, secondKey, sizeCodec) {

    val first = Array<ShortArray?>(size) { null }
    val second = Array<ShortArray?>(size) { null }

    override fun getFirst(index: Int) = first[index]

    override fun getSecond(index: Int) = second[index]

    override fun setFirst(index: Int, value: ShortArray?) {
        first[index] = value
    }

    override fun setSecond(index: Int, value: ShortArray?) {
        second[index] = value
    }

    override fun size(value: ShortArray?) = value?.size ?: 0

    override fun read(reader: Reader, size: Int): ShortArray {
        val array = ShortArray(size)
        reader.readBytes(array)
        return array
    }

    override fun write(writer: Writer, value: ShortArray) = writer.writeBytes(value)

    override fun read(reader: ConfigReader): ShortArray? {
        if (reader.peek == '"') {
            val value = reader.string()
            require(value == "null") { "Expected null ShortArray, found: $value" }
            return null
        }
        val list = ObjectArrayList<Short>(4)
        while (reader.nextElement()) {
            list.add(fieldCodec.readConfig(reader))
        }
        return ShortArray(list.size) { list[it] }
    }

    override fun write(writer: ConfigWriter, value: ShortArray?) {
        if (value == null) {
            writer.writeValue("null")
            return
        }
        writer.list(value.size) { fieldCodec.writeConfig(writer, value[it]) }
    }

    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        val size = sizeCodec.readBinary(reader)
        if (size == 0) {
            setFirst(index, null)
            setSecond(index, null)
            return
        }
        setFirst(index, read(reader, size))
        setSecond(index, read(reader, size))
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int): Boolean {
        val first = getFirst(index)
        val second = getSecond(index)
        if (first == null || second == null) {
            sizeCodec.writeBinary(writer, 0)
            return true
        }
        sizeCodec.writeBinary(writer, size(first))
        write(writer, first)
        write(writer, second)
        return true
    }

    override fun override(other: Field, from: Int, to: Int) {
        other as ShortArraysField
        if (other.first[from] == null || other.second[from] == null) {
            return
        }
        first[to] = other.first[from]
        second[to] = other.second[from]
    }

    override fun directSize() = (sizeCodec.bytes(0) * first.size) + (first.sumOf { it?.size ?: 0 } * fieldCodec.bytes(0) * 2)

    override fun clear() {
        first.fill(null)
        second.fill(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShortArraysField
        return first.contentDeepEquals(other.first) && second.contentDeepEquals(other.second)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + first.contentDeepHashCode()
        result = 31 * result + second.contentDeepHashCode()
        return result
    }

    override fun toString(): String = "ShortArraysField(first=${first.contentToString()}, second=${second.contentToString()})"
}