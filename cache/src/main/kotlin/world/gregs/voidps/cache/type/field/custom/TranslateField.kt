package world.gregs.voidps.cache.type.field.custom

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.AccessibleField
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.NullValueField

class TranslateField(
    size: Int,
    private val modelIds: NullValueField<IntArray>
) : AccessibleField<Array<ByteArray?>?> {
    private val data = arrayOfNulls<Array<ByteArray?>>(size)

    override fun get(index: Int) = data[index]

    override fun set(index: Int, value: Array<ByteArray?>?) {
        data[index] = value
    }

    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        val translations = arrayOfNulls<ByteArray>(modelIds.get(index)?.size ?: return)
        val length = reader.readUnsignedByte()
        for (i in 0 until length) {
            val index = reader.readUnsignedByte()
            translations[index] = byteArrayOf(
                reader.readByte().toByte(),
                reader.readByte().toByte(),
                reader.readByte().toByte(),
            )
        }
        set(index, translations)
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int) {
        val translations = get(index) ?: return
        writer.writeByte(opcode)
        writer.writeByte(translations.size)
        for (i in translations.indices) {
            val translation = translations[i] ?: continue
            writer.writeByte(i)
            writer.writeBytes(translation)
        }
    }

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        val list = mutableListOf<ByteArray>()
        while (reader.nextElement()) {
            val array = ByteArray(3)
            var index = 0
            while (reader.nextElement()) {
                array[index++] = reader.int().toByte()
            }
            list.add(array)
        }
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String) {
        val translations = get(index) ?: return
        writer.writePair(key, translations)
    }

    override fun readDirect(reader: Reader) {
        for (i in 0 until data.size) {
            readPacked(reader, i, 0)
        }
    }

    override fun writeDirect(writer: Writer) {
        for (i in 0 until data.size) {
            writePacked(writer, i, 0)
        }
    }

    override fun directSize(): Int {
        var size = 0
        for (i in 0 until data.size) {
            val translations = get(i) ?: continue
            size += 2
            for (i in translations.indices) {
                translations[i] ?: continue
                size += 4
            }
        }
        return size
    }

    override fun override(other: Field, from: Int, to: Int) {
        other as TranslateField
        if (other.data[from] == null) {
            return
        }
        data[to] = other.data[from]
    }

    override fun clear() {
        data.fill(null)
    }
}