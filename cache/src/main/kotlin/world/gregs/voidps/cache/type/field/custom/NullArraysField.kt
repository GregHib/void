package world.gregs.voidps.cache.type.field.custom

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.FieldCodec

abstract class NullArraysField<T>(
    val size: Int,
    val firstKey: String,
    val secondKey: String,
    val sizeCodec: FieldCodec<Int>,
) : Field {

    abstract fun getFirst(index: Int): T?

    abstract fun getSecond(index: Int): T?

    abstract fun setFirst(index: Int, value: T?)

    abstract fun setSecond(index: Int, value: T?)

    abstract fun size(value: T?): Int

    abstract fun read(reader: Reader, size: Int): T

    abstract fun write(writer: Writer, value: T)

    abstract fun read(reader: ConfigReader): T?

    abstract fun write(writer: ConfigWriter, value: T?)

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

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        when (key) {
            firstKey -> setFirst(index, read(reader))
            secondKey -> setSecond(index, read(reader))
        }
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String): Boolean {
        when (key) {
            firstKey -> write(writer, getFirst(index))
            secondKey -> write(writer, getSecond(index))
        }
        return true
    }

    override fun readDirect(reader: Reader) {
        for (i in 0 until size) {
            readPacked(reader, i, 0)
        }
    }

    override fun writeDirect(writer: Writer) {
        for (i in 0 until size) {
            writePacked(writer, i, 0)
        }
    }

    override fun hashCode(): Int {
        var result = 0
        for (i in 0 until size) {
            result = 31 * result + (getFirst(i)?.hashCode() ?: 0)
            result = 31 * result + (getSecond(i)?.hashCode() ?: 0)
        }
        return result
    }

    override fun toString(): String = "first=${(0 until size).joinToString { getFirst(it).toString() }}, second=${(0 until size).joinToString { getSecond(it).toString() }}"
}