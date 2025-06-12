package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.NPCS
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull

class NPCDecoderFull(val members: Boolean = true) : DefinitionDecoder<NPCDefinitionFull>(NPCS) {

    override fun create(size: Int) = Array(size) { NPCDefinitionFull(it) }

    override fun getFile(id: Int) = id and 0x7f

    override fun getArchive(id: Int) = id ushr 7

    override  fun NPCDefinitionFull.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> {
                val length = buffer.readUnsignedByte()
                modelIds = IntArray(length)
                for (count in 0 until length) {
                    modelIds!![count] = buffer.readUnsignedShort()
                    if (modelIds!![count] == 65535) {
                        modelIds!![count] = -1
                    }
                }
            }
            2 -> name = buffer.readString()
            12 -> size = buffer.readUnsignedByte()
            in 30..34 -> options[opcode - 30] = buffer.readString()
            40 -> readColours(buffer)
            41 -> readTextures(buffer)
            42 -> readColourPalette(buffer)
            60 -> dialogueModels = IntArray(buffer.readUnsignedByte()) { buffer.readUnsignedShort() }
            93 -> drawMinimapDot = false
            95 -> combat = buffer.readShort()
            97 -> scaleXY = buffer.readShort()
            98 -> scaleZ = buffer.readShort()
            99 -> priorityRender = true
            100 -> lightModifier = buffer.readByte()
            101 -> shadowModifier = 5 * buffer.readByte()
            102 -> headIcon = buffer.readShort()
            103 -> rotation = buffer.readShort()
            106, 118 -> readTransforms(buffer, opcode == 118)
            107 -> clickable = false
            109 -> slowWalk = false
            111 -> animateIdle = false
            113 -> {
                primaryShadowColour = buffer.readShort().toShort()
                secondaryShadowColour = buffer.readShort().toShort()
            }
            114 -> {
                primaryShadowModifier = buffer.readByte().toByte()
                secondaryShadowModifier = buffer.readByte().toByte()
            }
            119 -> walkMask = buffer.readByte().toByte()
            121 -> {
                translations = arrayOfNulls(modelIds!!.size)
                val length = buffer.readUnsignedByte()
                for (count in 0 until length) {
                    val index = buffer.readUnsignedByte()
                    translations!![index] = intArrayOf(
                        buffer.readByte(),
                        buffer.readByte(),
                        buffer.readByte()
                    )
                }
            }
            122 -> hitbarSprite = buffer.readShort()
            123 -> height = buffer.readShort()
            125 -> respawnDirection = buffer.readByte().toByte()
            127 -> renderEmote = buffer.readShort()
            128 -> buffer.readUnsignedByte()
            134 -> {
                idleSound = buffer.readShort()
                if (idleSound == 65535) {
                    idleSound = -1
                }
                crawlSound = buffer.readShort()
                if (crawlSound == 65535) {
                    crawlSound = -1
                }
                walkSound = buffer.readShort()
                if (walkSound == 65535) {
                    walkSound = -1
                }
                runSound = buffer.readShort()
                if (runSound == 65535) {
                    runSound = -1
                }
                soundDistance = buffer.readUnsignedByte()
            }
            135 -> {
                primaryCursorOp = buffer.readUnsignedByte()
                primaryCursor = buffer.readShort()
            }
            136 -> {
                secondaryCursorOp = buffer.readUnsignedByte()
                secondaryCursor = buffer.readShort()
            }
            137 -> attackCursor = buffer.readShort()
            138 -> armyIcon = buffer.readShort()
            139 -> spriteId = buffer.readShort()
            140 -> ambientSoundVolume = buffer.readUnsignedByte()
            141 -> visiblePriority = true
            142 -> mapFunction = buffer.readShort()
            143 -> invisiblePriority = true
            in 150..154 -> {
                options[opcode - 150] = buffer.readString()
                if (!members) {
                    options[opcode - 150] = null
                }
            }
            155 -> {
                hue = buffer.readByte().toByte()
                saturation = buffer.readByte().toByte()
                lightness = buffer.readByte().toByte()
                opacity = buffer.readByte().toByte()
            }
            158 -> mainOptionIndex = 1.toByte()
            159 -> mainOptionIndex = 0.toByte()
            160 -> {
                val length = buffer.readUnsignedByte()
                campaigns = IntArray(length) { buffer.readShort() }
            }
            162 -> vorbis = true
            163 -> slayerType = buffer.readUnsignedByte()
            164 -> {
                soundRateMin = buffer.readShort()
                soundRateMax = buffer.readShort()
            }
            165 -> pickSizeShift = buffer.readUnsignedByte()
            168 -> soundRangeMin = buffer.readUnsignedByte()
            249 -> readParameters(buffer)
        }
    }

}