package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Config.QUESTS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.QuestDefinition
import world.gregs.voidps.cache.definition.Parameters

class QuestDecoder(
    private val parameters: Parameters = Parameters.EMPTY
) : ConfigDecoder<QuestDefinition>(QUESTS) {

    override fun create(size: Int) = Array(size) { QuestDefinition(it) }

    override fun QuestDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> name = buffer.readPrefixedString()
            2 -> listName = buffer.readPrefixedString()
            3 -> varps = readArray(buffer)
            4 -> varbits = readArray(buffer)
            5 -> subQuest = buffer.readShort()
            6 -> buffer.readUnsignedByte()
            7 -> difficulty = buffer.readUnsignedByte()
            8 -> members = true
            9 -> questPoints = buffer.readUnsignedByte()
            10 -> pathStart = IntArray(buffer.readUnsignedByte()) { buffer.readInt() }
            12 -> otherPathStart = buffer.readInt()
            13 -> questRequirements = IntArray(buffer.readUnsignedByte()) { buffer.readShort() }
            14 -> skillRequirements = Array(buffer.readUnsignedByte()) { IntArray(2) { buffer.readUnsignedByte() } }
            15 -> buffer.readShort()
            17 -> itemSprite = buffer.readShort()
            249 -> readParameters(buffer, parameters)
        }
    }

    override fun changeValues(definitions: Array<QuestDefinition>, definition: QuestDefinition) {
        if (definition.listName == null) {
            definition.listName = definition.name
        }
    }

    private fun readArray(buffer: Reader) : Array<IntArray> {
        val length = buffer.readUnsignedByte()
        val array = Array(length) { IntArray(3) }
        for (count in 0 until length) {
            array[count][0] = buffer.readShort()
            array[count][1] = buffer.readInt()
            array[count][2] = buffer.readInt()
        }
        return array
    }

    companion object {
        private fun Reader.readPrefixedString(): String {
            val head = readByte()
            check(head == 0) { "Bad version number in gjstr2" }
            val sb = StringBuilder()
            var b: Int
            while (readableBytes() > 0) {
                b = readByte()
                if (b == 0) {
                    break
                }
                sb.append(byteToChar(b.toByte()))
            }
            return sb.toString()
        }
    }
}