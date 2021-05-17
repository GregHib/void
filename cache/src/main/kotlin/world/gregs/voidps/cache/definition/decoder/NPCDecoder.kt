package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.NPCS
import world.gregs.voidps.cache.definition.data.NPCDefinition

class NPCDecoder(cache: world.gregs.voidps.cache.Cache, val member: Boolean) : DefinitionDecoder<NPCDefinition>(cache, NPCS) {

    override fun create() = NPCDefinition()

    override fun getFile(id: Int) = id and 0x7f

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
                repeat(length) {
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
                if (!member) {
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