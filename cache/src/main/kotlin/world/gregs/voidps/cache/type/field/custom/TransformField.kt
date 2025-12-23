package world.gregs.voidps.cache.type.field.custom

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.Field

class TransformField(
    size: Int,
    val largerOpcode: Int,
) : Field {

    private val transforms = arrayOfNulls<IntArray>(size)
    private val varbit = IntArray(size) { -1 }
    private val varp = IntArray(size) { -1 }

    fun getVarbit(index: Int) = varbit[index]

    fun getVarp(index: Int) = varbit[index]

    fun getTransforms(index: Int) = transforms[index]

    fun setVarbit(index: Int, value: Int) {
        varbit[index] = value
    }

    fun setVarp(index: Int, value: Int) {
        varp[index] = value
    }

    fun setTransforms(index: Int, value: IntArray?) {
        transforms[index] = value
    }

    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        setVarbit(index, reader.readShort())
        setVarp(index, reader.readShort())
        var last = -1
        if (opcode == largerOpcode) {
            last = reader.readUnsignedByte()
        }
        val length = reader.readUnsignedByte()
        val array = IntArray(length + 2) { reader.readUnsignedShort() }
        array[length + 1] = last
        setTransforms(index, array)
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int) {
        val transforms = getTransforms(index)
        val varbit = getVarbit(index)
        val varp = getVarp(index)
        if (transforms == null) {
            return
        }
        val last = transforms.last()
        val extended = last != -1 && opcode == largerOpcode
        writer.writeByte(if (extended) largerOpcode else opcode)
        writer.writeShort(varbit)
        writer.writeShort(varp)
        if (extended) {
            writer.writeShort(last)
        }
        writer.writeByte(transforms.size - 2)
        for (i in 0 until transforms.size - 1) {
            writer.writeShort(transforms[i])
        }
    }

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        when (key) {
            "varbit" -> setVarbit(index, reader.int())
            "varp" -> setVarp(index, reader.int())
            "transforms" -> {
                val list = mutableListOf<Int>()
                while (reader.nextElement()) {
                    list.add(reader.int())
                }
                setTransforms(index, list.toIntArray())
            }
        }
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String) {
        when (key) {
            "varbit" -> if (getVarbit(index) != -1) {
                writer.writePair(key, getVarbit(index))
            }
            "varp" -> if (getVarp(index) != -1) {
                writer.writePair(key, getVarp(index))
            }
            "transforms" -> {
                val transforms = getTransforms(index) ?: return
                writer.writePair(key, transforms.toList())
            }
        }
    }

    override fun readDirect(reader: Reader) {
        for (i in 0 until varbit.size) {
            readPacked(reader, i, 0)
        }
    }

    override fun writeDirect(writer: Writer) {
        for (i in 0 until varbit.size) {
            writePacked(writer, i, 0)
        }
    }

    override fun directSize(): Int = varbit.size * 5 + transforms.sumOf {
        if (it == null) 0 else if (it.last() != -1) it.size * 2 else (it.size - 1) * 2
    }

    override fun override(other: Field, from: Int, to: Int) {
        other as TransformField
        if (other.transforms[from] == null || other.varbit[from] == -1 && other.varp[from] == -1) {
            return
        }
        transforms[to] = other.transforms[from]
        varbit[to] = other.varbit[from]
        varp[to] = other.varp[from]
    }

    override fun clear() {
        transforms.fill(null)
        varbit.fill(-1)
        varp.fill(-1)
    }
}