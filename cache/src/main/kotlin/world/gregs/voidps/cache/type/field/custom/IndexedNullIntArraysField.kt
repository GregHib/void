package world.gregs.voidps.cache.type.field.custom

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeKey
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.codec.ShortCodec

/**
 *  Used for item stacks
 *  ```
 *  if (stackIds == null) {
 *      stackAmounts = IntArray(10)
 *      stackIds = IntArray(10)
 *  }
 *  stackIds!![opcode - 100] = buffer.readShort()
 *  stackAmounts!![opcode - 100] = buffer.readShort()
 *  ```
 */
class IndexedNullIntArraysField(
    size: Int,
    val firstKey: String,
    val secondKey: String,
    val offset: Int,
    private val arraySize: Int = 10,
    val field: FieldCodec<Short> = ShortCodec,
) : Field {
    val first = arrayOfNulls<ShortArray>(size)
    val second = arrayOfNulls<ShortArray>(size)

    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        if (first[index] == null) {
            first[index] = ShortArray(arraySize)
            second[index] = ShortArray(arraySize)
        }
        first[index]!![opcode - offset] = field.readBinary(reader)
        second[index]!![opcode - offset] = field.readBinary(reader)
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int) {
        val stackIds = first[index]
        val stackAmounts = second[index]
        if (stackIds == null || stackAmounts == null) {
            return
        }
        val id = stackIds[opcode - offset].toInt()
        val amount = stackAmounts[opcode - offset].toInt()
        if (id == 0 && amount == 0) {
            return
        }
        writer.writeByte(opcode)
        writer.writeShort(id)
        writer.writeShort(amount)
    }

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        when (key) {
            firstKey -> first[index] = readArray(reader)
            secondKey -> second[index] = readArray(reader)
        }
    }

    private fun readArray(reader: ConfigReader): ShortArray {
        val array = ShortArray(arraySize)
        var count = 0
        while (reader.nextElement()) {
            array[count++] = reader.int().toShort()
        }
        return array
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String): Boolean {
        when (key) {
            firstKey -> writeArray(writer, key, first[index] ?: return false)
            secondKey -> writeArray(writer, key, second[index] ?: return false)
        }
        return true
    }

    private fun writeArray(writer: ConfigWriter, key: String, array: ShortArray) {
        writer.writeKey(key)
        writer.list(array.size) { writeValue(array[it]) }
        writer.write("\n")
    }

    override fun readDirect(reader: Reader) {
        for (i in first.indices) {
            val size = reader.readUnsignedByte()
            if (size == 0) {
                first[i] = null
                second[i] = null
                continue
            }
            val ids = ShortArray(arraySize)
            reader.readBytes(ids)
            first[i] = ids

            val amounts = ShortArray(arraySize)
            reader.readBytes(amounts)
            second[i] = amounts
        }
    }

    override fun writeDirect(writer: Writer) {
        for (i in first.indices) {
            val data = first[i]
            if (data == null) {
                writer.writeByte(0)
                continue
            }
            writer.writeByte(data.size)
            for (id in data) {
                writer.writeShort(id.toInt())
            }
            for (amount in second[i]!!) {
                writer.writeShort(amount.toInt())
            }
        }
    }

    override fun directSize(): Int = first.size + first.sumOf { it?.size ?: 0 } * 4

    override fun override(other: Field, from: Int, to: Int) {
        other as IndexedNullIntArraysField
        if (other.first[from] == null || other.second[from] == null) {
            return
        }
        first[to] = other.first[from]
        second[to] = other.second[from]
    }

    override fun clear() {
        first.fill(null)
        second.fill(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IndexedNullIntArraysField

        if (!first.contentDeepEquals(other.first)) return false
        if (!second.contentDeepEquals(other.second)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = first.contentDeepHashCode()
        result = 31 * result + second.contentDeepHashCode()
        return result
    }

}