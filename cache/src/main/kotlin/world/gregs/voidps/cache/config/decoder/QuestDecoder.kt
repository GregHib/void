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
            1 -> aString2211 = buffer.readPrefixedString()
            2 -> aString2202 = buffer.readPrefixedString()
            3 -> anIntArrayArray2208 = readArray(buffer)
            4 -> anIntArrayArray2193 = readArray(buffer)
            5 -> buffer.readShort()
            6 -> buffer.readUnsignedByte()
            7 -> buffer.readUnsignedByte()
            9 -> buffer.readUnsignedByte()
            10 -> {
                val length = buffer.readUnsignedByte()
                anIntArray2209 = IntArray(length) { buffer.readInt() }
            }
            12 -> buffer.readInt()
            13 -> {
                val length = buffer.readUnsignedByte()
                anIntArray2207 = IntArray(length) { buffer.readShort() }
            }
            14 -> {
                val length = buffer.readUnsignedByte()
                anIntArrayArray2210 = Array(length) { IntArray(2) { buffer.readUnsignedByte() } }
            }
            15 -> buffer.readShort()
            17 -> anInt2188 = buffer.readShort()
            18 -> {
                val length = buffer.readUnsignedByte()
                aStringArray2201 = arrayOfNulls(length)
                anIntArray2200 = IntArray(length)
                anIntArray2199 = IntArray(length)
                anIntArray2191 = IntArray(length)
                for (count in 0 until length) {
                    anIntArray2200!![count] = buffer.readInt()
                    anIntArray2191!![count] = buffer.readInt()
                    anIntArray2199!![count] = buffer.readInt()
                    aStringArray2201!![count] = buffer.readString()
                }
            }
            19 -> {
                val length = buffer.readUnsignedByte()
                anIntArray2204 = IntArray(length)
                aStringArray2198 = arrayOfNulls(length)
                anIntArray2195 = IntArray(length)
                anIntArray2190 = IntArray(length)
                for (count in 0 until length) {
                    anIntArray2204!![count] = buffer.readInt()
                    anIntArray2195!![count] = buffer.readInt()
                    anIntArray2190!![count] = buffer.readInt()
                    aStringArray2198!![count] = buffer.readString()
                }
            }
            249 -> readParameters(buffer, parameters)
        }
    }

    override fun changeValues(definitions: Array<QuestDefinition>, definition: QuestDefinition) {
        if (definition.aString2202 == null) {
            definition.aString2202 = definition.aString2211
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