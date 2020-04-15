package rs.dusk.cache.definition.decoder

import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices.NPCS
import rs.dusk.cache.definition.data.NPCDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class NPCDecoder(val member: Boolean) : DefinitionDecoder<NPCDefinition>(NPCS) {

    override fun create() = NPCDefinition()

    override fun getFile(id: Int) = id and 0x7ff

    override fun getArchive(id: Int) = id ushr 7

    override fun NPCDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> {
                val length = buffer.readUnsignedByte()
                modelIds = IntArray(length)
                repeat(length) { count ->
                    modelIds!![count] = buffer.readShort()
                    if (modelIds!![count] == 65535) {
                        modelIds!![count] = -1
                    }
                }
            }
            2 -> name = buffer.readString()
            12 -> size = buffer.readUnsignedByte()
            in 30..34 -> options[-30 + opcode] = buffer.readString()
            40 -> readColours(buffer)
            41 -> readTextures(buffer)
            42 -> {
                val length = buffer.readUnsignedByte()
                recolourPalette = ByteArray(length)
                repeat(length) { count ->
                    recolourPalette!![count] = buffer.readByte().toByte()
                }
            }
            60 -> {
                val length = buffer.readUnsignedByte()
                dialogueModels = IntArray(length)
                repeat(length) { count ->
                    dialogueModels!![count] = buffer.readShort()
                }
            }
            93 -> drawMinimapDot = false
            95 -> combat = buffer.readShort()
            97 -> scaleXY = buffer.readShort()
            98 -> scaleZ = buffer.readShort()
            99 -> priorityRender = true
            100 -> lightModifier = buffer.readByte()
            101 -> shadowModifier = 5 * buffer.readByte()
            102 -> headIcon = buffer.readShort()
            103 -> rotation = buffer.readShort()
            106, 118 -> {
                varbit = buffer.readShort()
                if (varbit == 65535) {
                    varbit = -1
                }
                varp = buffer.readShort()
                if (varp == 65535) {
                    varp = -1
                }
                var last = -1
                if (opcode == 118) {
                    last = buffer.readShort()
                    if (last == 65535) {
                        last = -1
                    }
                }
                val count = buffer.readUnsignedByte()
                morphs = IntArray(count + 2)
                for (index in 0..count) {
                    morphs!![index] = buffer.readShort()
                    if (morphs!![index] == 65535) {
                        morphs!![index] = -1
                    }
                }
                morphs!![count + 1] = last
            }
            107 -> clickable = false
            109 -> slowWalk = false
            111 -> animateIdle = false
            113 -> {
                aShort2863 = buffer.readShort().toShort()
                aShort2871 = buffer.readShort().toShort()
            }
            114 -> {
                aByte2877 = buffer.readByte().toByte()
                aByte2868 = buffer.readByte().toByte()
            }
            119 -> walkMask = buffer.readByte().toByte()
            121 -> {
                translations = arrayOfNulls(modelIds!!.size)
                val length = buffer.readUnsignedByte()
                repeat(length) {
                    val index = buffer.readUnsignedByte()
                    translations!![index] = intArrayOf(
                        buffer.readByte(),
                        buffer.readByte(),
                        buffer.readByte()
                    )
                }
            }
            122 -> anInt2878 = buffer.readShort()
            123 -> height = buffer.readShort()
            125 -> respawnDirection = buffer.readByte().toByte()
            127 -> renderEmote = buffer.readShort()
            128 -> buffer.readUnsignedByte()
            134 -> {
                anInt2812 = buffer.readShort()
                if (anInt2812 == 65535) {
                    anInt2812 = -1
                }
                anInt2833 = buffer.readShort()
                if (anInt2833 == 65535) {
                    anInt2833 = -1
                }
                anInt2809 = buffer.readShort()
                if (anInt2809 == 65535) {
                    anInt2809 = -1
                }
                anInt2810 = buffer.readShort()
                if (anInt2810 == 65535) {
                    anInt2810 = -1
                }
                anInt2864 = buffer.readUnsignedByte()
            }
            135 -> {
                anInt2815 = buffer.readUnsignedByte()
                anInt2859 = buffer.readShort()
            }
            136 -> {
                anInt2856 = buffer.readUnsignedByte()
                anInt2886 = buffer.readShort()
            }
            137 -> attackCursor = buffer.readShort()
            138 -> armyIcon = buffer.readShort()
            139 -> spriteId = buffer.readShort()
            140 -> anInt2828 = buffer.readUnsignedByte()
            141 -> aBoolean2843 = true
            142 -> mapFunction = buffer.readShort()
            143 -> aBoolean2825 = true
            in 150..154 -> {
                options[opcode - 150] = buffer.readString()
                if (!member) {
                    options[opcode - 150] = null
                }
            }
            155 -> {
                aByte2836 = buffer.readByte().toByte()
                aByte2853 = buffer.readByte().toByte()
                aByte2857 = buffer.readByte().toByte()
                aByte2839 = buffer.readByte().toByte()
            }
            158 -> mainOptionIndex = 1.toByte()
            159 -> mainOptionIndex = 0.toByte()
            160 -> {
                val length = buffer.readUnsignedByte()
                campaigns = IntArray(length)
                repeat(length) { count ->
                    campaigns!![count] = buffer.readShort()
                }
            }
            162 -> aBoolean2883 = true
            163 -> anInt2803 = buffer.readUnsignedByte()
            164 -> {
                anInt2844 = buffer.readShort()
                anInt2852 = buffer.readShort()
            }
            165 -> anInt2831 = buffer.readUnsignedByte()
            168 -> anInt2862 = buffer.readUnsignedByte()
            249 -> readParameters(buffer)
        }
    }

}