package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.WORLD_MAP_INFO
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.WorldMapInfoDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class WorldMapInfoDecoder : ConfigDecoder<WorldMapInfoDefinition>(WORLD_MAP_INFO) {

    override fun create() = WorldMapInfoDefinition()

    override fun WorldMapInfoDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> anInt1062 = buffer.readShort()
            2 -> anInt1056 = buffer.readShort()
            3 -> name = buffer.readString()
            4 -> anInt1058 = buffer.readMedium()
            5 -> anInt1054 = buffer.readMedium()
            6 -> fontSize = buffer.readUnsignedByte()
            7 -> {
                val setting = buffer.readUnsignedByte()
                if (0x1 and setting == 0) {
                    aBoolean1047 = false
                }
                if (setting and 0x2 == 2) {
                    aBoolean1079 = true
                }
            }
            8 -> aBoolean1063 = buffer.readUnsignedByte() == 1
            9 -> {
                anInt1069 = buffer.readShort()
                if (anInt1069 == 65535) {
                    anInt1069 = -1
                }
                anInt1091 = buffer.readShort()
                if (anInt1091 == 65535) {
                    anInt1091 = -1
                }
                anInt1087 = buffer.readInt()
                anInt1042 = buffer.readInt()
            }
            in 10..14 -> aStringArray1065[opcode + -10] = buffer.readString()
            15 -> {
                val length = buffer.readUnsignedByte()
                anIntArray1049 = IntArray(length * 2)
                repeat(length * 2) { count ->
                    anIntArray1049!![count] = buffer.readUnsignedShort()
                }
                anInt1084 = buffer.readInt()
                val size = buffer.readUnsignedByte()
                anIntArray1066 = IntArray(size)
                repeat(size) { count ->
                    anIntArray1066!![count] = buffer.readInt()
                }
                aByteArray1057 = ByteArray(length)
                repeat(length) { count ->
                    aByteArray1057!![count] = buffer.readByte().toByte()
                }
            }
            16 -> aBoolean1064 = false
            17 -> aString1045 = buffer.readString()
            18 -> anInt1093 = buffer.readShort()
            19 -> anInt1067 = buffer.readShort()
            20 -> {
                anInt1048 = buffer.readShort()
                if (anInt1048 == 65535) {
                    anInt1048 = -1
                }
                anInt1044 = buffer.readShort()
                if (anInt1044 == 65535) {
                    anInt1044 = -1
                }
                anInt1078 = buffer.readInt()
                anInt1072 = buffer.readInt()
            }
            21 -> anInt1081 = buffer.readInt()
            22 -> anInt1077 = buffer.readInt()
            23 -> {
                anInt1074 = buffer.readUnsignedByte()
                anInt1050 = buffer.readUnsignedByte()
                anInt1080 = buffer.readUnsignedByte()
            }
            24 -> {
                anInt1071 = buffer.readUnsignedShort()
                anInt1092 = buffer.readUnsignedShort()
            }
            249 -> readParameters(buffer)
        }
    }

    override fun WorldMapInfoDefinition.changeValues() {
        if (anIntArray1049 != null) {
            var i = 0
            while (anIntArray1049!!.size > i) {
                if (anIntArray1049!![i] >= anInt1068) {
                    if (anInt1089 < anIntArray1049!![i]) {
                        anInt1089 = anIntArray1049!![i]
                    }
                } else {
                    anInt1068 = anIntArray1049!![i]
                }
                if (anInt1051 > anIntArray1049!![i + 1]) {
                    anInt1051 = anIntArray1049!![1 + i]
                } else if (anIntArray1049!![1 + i] > anInt1060) {
                    anInt1060 = anIntArray1049!![i + 1]
                }
                i += 2
            }
        }
    }
}