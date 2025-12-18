package world.gregs.voidps.cache.type.field.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeKey
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.TypeField

class ItemStack(
    val idKey: String,
    val amountKey: String,
    val offset: Int,
) : TypeField(listOf(idKey, amountKey)) {
    var ids: IntArray? = null
    var amounts: IntArray? = null

    override fun readBinary(reader: Reader, opcode: Int) {
        if (ids == null) {
            amounts = IntArray(10)
            ids = IntArray(10)
        }
        ids!![opcode - offset] = reader.readShort()
        amounts!![opcode - offset] = reader.readShort()
    }

    override fun writeBinary(writer: Writer, opcode: Int): Boolean {
        val stackIds = ids
        val stackAmounts = amounts
        if (stackIds == null || stackAmounts == null) {
            return false
        }
        val id = stackIds[opcode - offset]
        val amount = stackAmounts[opcode - offset]
        if (id == 0 && amount == 0) {
            return false
        }
        writer.writeByte(opcode)
        writer.writeShort(id)
        writer.writeShort(amount)
        return true
    }

    override fun readConfig(reader: ConfigReader, key: String) {
        when (key) {
            idKey -> ids = readArray(reader)
            amountKey -> amounts = readArray(reader)
        }
    }

    private fun readArray(reader: ConfigReader): IntArray {
        val list = mutableListOf<Int>()
        while (reader.nextElement()) {
            list.add(reader.int())
        }
        return IntArray(10) { list.getOrNull(it) ?: 0 }
    }

    override fun writeConfig(writer: ConfigWriter, key: String) {
        when (key) {
            idKey -> writeArray(writer, key, ids ?: return)
            amountKey -> writeArray(writer, key, amounts ?: return)
        }
    }

    private fun writeArray(writer: ConfigWriter, key: String, array: IntArray) {
        writer.writeKey(key)
        writer.list(array.size) { writeValue(array[it]) }
        writer.write("\n")
    }

    override fun reset() {
        ids = null
        amounts = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemStack

        if (!ids.contentEquals(other.ids)) return false
        if (!amounts.contentEquals(other.amounts)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ids?.contentHashCode() ?: 0
        result = 31 * result + (amounts?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ItemStack(ids=${ids.contentToString()}, amounts=${amounts.contentToString()})"
    }

}