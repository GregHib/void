package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Configs.QUESTS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.QuestDefinition

class QuestDecoder(cache: world.gregs.voidps.cache.Cache) : ConfigDecoder<QuestDefinition>(cache, QUESTS) {

    override fun create() = QuestDefinition()

    override fun QuestDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> aString2211 = buffer.readPrefixedString()
            2 -> aString2202 = buffer.readPrefixedString()
            3 -> {
                val length = buffer.readUnsignedByte()
                anIntArrayArray2208 = Array(length) { IntArray(3) }
                repeat(length) { count ->
                    anIntArrayArray2208!![count][0] = buffer.readShort()
                    anIntArrayArray2208!![count][1] = buffer.readInt()
                    anIntArrayArray2208!![count][2] = buffer.readInt()
                }
            }
            4 -> {
                val length = buffer.readUnsignedByte()
                anIntArrayArray2193 = Array(length) { IntArray(3) }
                repeat(length) { count ->
                    anIntArrayArray2193!![count][0] = buffer.readShort()
                    anIntArrayArray2193!![count][1] = buffer.readInt()
                    anIntArrayArray2193!![count][2] = buffer.readInt()
                }
            }
            5 -> buffer.readShort()
            6 -> buffer.readUnsignedByte()
            7 -> buffer.readUnsignedByte()
            9 -> buffer.readUnsignedByte()
            10 -> {
                val length = buffer.readUnsignedByte()
                anIntArray2209 = IntArray(length)
                repeat(length) { count ->
                    anIntArray2209!![count] = buffer.readInt()
                }
            }
            12 -> buffer.readInt()
            13 -> {
                val length = buffer.readUnsignedByte()
                anIntArray2207 = IntArray(length)
                repeat(length) { count ->
                    anIntArray2207!![count] = buffer.readShort()
                }
            }
            14 -> {
                val length = buffer.readUnsignedByte()
                anIntArrayArray2210 = Array(length) { IntArray(2) }
                repeat(length) { count ->
                    anIntArrayArray2210!![count][0] = buffer.readUnsignedByte()
                    anIntArrayArray2210!![count][1] = buffer.readUnsignedByte()
                }
            }
            15 -> buffer.readShort()
            17 -> anInt2188 = buffer.readShort()
            18 -> {
                val length = buffer.readUnsignedByte()
                aStringArray2201 = arrayOfNulls(length)
                anIntArray2200 = IntArray(length)
                anIntArray2199 = IntArray(length)
                anIntArray2191 = IntArray(length)
                repeat(length) { count ->
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
                repeat(length) { count ->
                    anIntArray2204!![count] = buffer.readInt()
                    anIntArray2195!![count] = buffer.readInt()
                    anIntArray2190!![count] = buffer.readInt()
                    aStringArray2198!![count] = buffer.readString()
                }
            }
            249 -> readParameters(buffer)
        }
    }

    override fun QuestDefinition.changeValues() {
        if (aString2202 == null) {
            aString2202 = aString2211
        }
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