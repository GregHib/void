package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull

object NpcDefinitionCodec : DefinitionCodec<NPCDefinitionFull> {
    override val index: Int = Index.NPCS

    override fun create(size: Int, block: (Int) -> NPCDefinitionFull) = Array(size, block)

    override fun create(index: Int) = NPCDefinitionFull(index)

    override fun data(cache: Cache, index: Int): ByteArray? {
        return cache.data(Index.NPCS, index and 0x7f, index ushr 7)
    }

    override fun encode(writer: Writer, definition: NPCDefinitionFull) {
        if (definition.id == -1) {
            return
        }

        val modelIds = definition.modelIds
        if (modelIds != null) {
            writer.writeByte(1)
            writer.writeByte(modelIds.size)
            for (models in modelIds) {
                writer.writeShort(models)
            }
        }

        val name = definition.name
        if (name != "null") {
            writer.writeByte(2)
            writer.writeString(name)
        }

        if (definition.size != 1) {
            writer.writeByte(12)
            writer.writeByte(definition.size)
        }

        val options = definition.options
        for (index in 0 until 5) {
            val option = options[index] ?: continue
            writer.writeByte(30 + index)
            writer.writeString(option)
        }

        definition.writeColoursTextures(writer)

        definition.writeRecolourPalette(writer)

        val dialogueModels = definition.dialogueModels
        if (dialogueModels != null) {
            writer.writeByte(60)
            writer.writeByte(dialogueModels.size)
            for (id in dialogueModels) {
                writer.writeShort(id)
            }
        }

        if (!definition.drawMinimapDot) {
            writer.writeByte(93)
        }

        if (definition.combat != -1) {
            writer.writeByte(95)
            writer.writeShort(definition.combat)
        }

        if (definition.scaleXY != 128) {
            writer.writeByte(97)
            writer.writeShort(definition.scaleXY)
        }

        if (definition.scaleZ != 128) {
            writer.writeByte(98)
            writer.writeShort(definition.scaleZ)
        }

        if (definition.priorityRender) {
            writer.writeByte(99)
        }

        if (definition.lightModifier != 0) {
            writer.writeByte(100)
            writer.writeByte(definition.lightModifier)
        }

        if (definition.shadowModifier != 0) {
            writer.writeByte(101)
            writer.writeByte(definition.shadowModifier / 5)
        }

        if (definition.headIcon != -1) {
            writer.writeByte(102)
            writer.writeShort(definition.headIcon)
        }

        if (definition.rotation != 32) {
            writer.writeByte(103)
            writer.writeShort(definition.rotation)
        }

        definition.writeTransforms(writer, 106, 118)

        if (!definition.clickable) {
            writer.writeByte(107)
        }

        if (!definition.slowWalk) {
            writer.writeByte(109)
        }

        if (!definition.animateIdle) {
            writer.writeByte(111)
        }

        if (definition.primaryShadowColour != 0.toShort() || definition.secondaryShadowColour != 0.toShort()) {
            writer.writeByte(113)
            writer.writeShort(definition.primaryShadowColour.toInt())
            writer.writeShort(definition.secondaryShadowColour.toInt())
        }

        if (definition.primaryShadowModifier.toInt() != -96 || definition.secondaryShadowModifier.toInt() != -16) {
            writer.writeByte(114)
            writer.writeByte(definition.primaryShadowModifier.toInt())
            writer.writeByte(definition.secondaryShadowModifier.toInt())
        }

        if (definition.walkMode.toInt() != 0) {
            writer.writeByte(119)
            writer.writeByte(definition.walkMode.toInt())
        }

        val translations = definition.translations
        if (translations != null) {
            writer.writeByte(121)
            writer.writeByte(translations.filterNotNull().size)
            for (i in translations.indices) {
                val translation = translations[i] ?: continue
                writer.writeByte(i)
                writer.writeByte(translation[0])
                writer.writeByte(translation[1])
                writer.writeByte(translation[2])
            }
        }

        if (definition.hitbarSprite != -1) {
            writer.writeByte(122)
            writer.writeShort(definition.hitbarSprite)
        }

        if (definition.height != -1) {
            writer.writeByte(123)
            writer.writeShort(definition.height)
        }

        if (definition.respawnDirection.toInt() != 4) {
            writer.writeByte(125)
            writer.writeByte(definition.respawnDirection.toInt())
        }

        if (definition.renderEmote != -1) {
            writer.writeByte(127)
            writer.writeShort(definition.renderEmote)
        }

        if (definition.idleSound != -1 || definition.crawlSound != -1 || definition.walkSound != -1 || definition.runSound != -1 || definition.soundDistance != 0) {
            writer.writeByte(134)
            writer.writeShort(definition.idleSound)
            writer.writeShort(definition.crawlSound)
            writer.writeShort(definition.walkSound)
            writer.writeShort(definition.runSound)
            writer.writeByte(definition.soundDistance)
        }

        if (definition.primaryCursorOp != -1 || definition.primaryCursor != -1) {
            writer.writeByte(135)
            writer.writeByte(definition.primaryCursorOp)
            writer.writeShort(definition.primaryCursor)
        }

        if (definition.secondaryCursorOp != -1 || definition.secondaryCursor != -1) {
            writer.writeByte(136)
            writer.writeByte(definition.secondaryCursorOp)
            writer.writeShort(definition.secondaryCursor)
        }

        if (definition.attackCursor != -1) {
            writer.writeByte(137)
            writer.writeShort(definition.attackCursor)
        }

        if (definition.armyIcon != -1) {
            writer.writeByte(138)
            writer.writeShort(definition.armyIcon)
        }

        if (definition.spriteId != -1) {
            writer.writeByte(139)
            writer.writeShort(definition.spriteId)
        }

        if (definition.ambientSoundVolume != 255) {
            writer.writeByte(140)
            writer.writeByte(definition.ambientSoundVolume)
        }

        if (definition.visiblePriority) {
            writer.writeByte(141)
        }

        if (definition.mapFunction != -1) {
            writer.writeByte(142)
            writer.writeShort(definition.mapFunction)
        }

        if (definition.invisiblePriority) {
            writer.writeByte(143)
        }

        val membersOptions = definition.options
        for (index in 0 until 5) {
            val option = options[index]
            if (option != null) {
                continue
            }
            val membersOption = membersOptions[index] ?: continue
            writer.writeByte(150 + index)
            writer.writeString(membersOption)
        }

        if (definition.hue.toInt() != 0 || definition.saturation.toInt() != 0 || definition.lightness.toInt() != 0 || definition.opacity.toInt() != 0) {
            writer.writeByte(155)
            writer.writeByte(definition.hue.toInt())
            writer.writeByte(definition.saturation.toInt())
            writer.writeByte(definition.lightness.toInt())
            writer.writeByte(definition.opacity.toInt())
        }

        if (definition.mainOptionIndex.toInt() == 1) {
            writer.writeByte(158)
        }

        if (definition.mainOptionIndex.toInt() == 0) {
            writer.writeByte(159)
        }

        val campaigns = definition.campaigns
        if (campaigns != null) {
            writer.writeByte(160)
            writer.writeByte(campaigns.size)
            campaigns.forEach {
                writer.writeShort(it)
            }
        }

        if (definition.vorbis) {
            writer.writeByte(162)
        }

        if (definition.slayerType != -1) {
            writer.writeByte(163)
            writer.writeByte(definition.slayerType)
        }

        if (definition.soundRateMin != 256 || definition.soundRateMax != 256) {
            writer.writeByte(164)
            writer.writeShort(definition.soundRateMin)
            writer.writeShort(definition.soundRateMax)
        }

        if (definition.pickSizeShift != 0) {
            writer.writeByte(165)
            writer.writeByte(definition.pickSizeShift)
        }

        if (definition.soundRangeMin != 0) {
            writer.writeByte(165)
            writer.writeByte(definition.soundRangeMin)
        }

        definition.writeParameters(writer)
        writer.writeByte(0)
    }

    override fun decode(reader: Reader, definition: NPCDefinitionFull) = with(definition) {
        while (true) {
            when (val opcode = reader.readUnsignedByte()) {
                0 -> break
                1 -> {
                    val length = reader.readUnsignedByte()
                    modelIds = IntArray(length)
                    for (count in 0 until length) {
                        modelIds!![count] = reader.readUnsignedShort()
                        if (modelIds!![count] == 65535) {
                            modelIds!![count] = -1
                        }
                    }
                }
                2 -> name = reader.readString()
                12 -> size = reader.readUnsignedByte()
                in 30..34 -> options[opcode - 30] = reader.readString()
                40 -> readColours(reader)
                41 -> readTextures(reader)
                42 -> readColourPalette(reader)
                60 -> dialogueModels = IntArray(reader.readUnsignedByte()) { reader.readUnsignedShort() }
                93 -> drawMinimapDot = false
                95 -> combat = reader.readShort()
                97 -> scaleXY = reader.readShort()
                98 -> scaleZ = reader.readShort()
                99 -> priorityRender = true
                100 -> lightModifier = reader.readByte()
                101 -> shadowModifier = 5 * reader.readByte()
                102 -> headIcon = reader.readShort()
                103 -> rotation = reader.readShort()
                106, 118 -> readTransforms(reader, opcode == 118)
                107 -> clickable = false
                109 -> slowWalk = false
                111 -> animateIdle = false
                113 -> {
                    primaryShadowColour = reader.readShort().toShort()
                    secondaryShadowColour = reader.readShort().toShort()
                }
                114 -> {
                    primaryShadowModifier = reader.readByte().toByte()
                    secondaryShadowModifier = reader.readByte().toByte()
                }
                119 -> walkMode = reader.readByte().toByte()
                121 -> {
                    translations = arrayOfNulls(modelIds!!.size)
                    val length = reader.readUnsignedByte()
                    for (count in 0 until length) {
                        val index = reader.readUnsignedByte()
                        translations!![index] = intArrayOf(
                            reader.readByte(),
                            reader.readByte(),
                            reader.readByte(),
                        )
                    }
                }
                122 -> hitbarSprite = reader.readShort()
                123 -> height = reader.readShort()
                125 -> respawnDirection = reader.readByte().toByte()
                127 -> renderEmote = reader.readShort()
                128 -> reader.readUnsignedByte()
                134 -> {
                    idleSound = reader.readShort()
                    if (idleSound == 65535) {
                        idleSound = -1
                    }
                    crawlSound = reader.readShort()
                    if (crawlSound == 65535) {
                        crawlSound = -1
                    }
                    walkSound = reader.readShort()
                    if (walkSound == 65535) {
                        walkSound = -1
                    }
                    runSound = reader.readShort()
                    if (runSound == 65535) {
                        runSound = -1
                    }
                    soundDistance = reader.readUnsignedByte()
                }
                135 -> {
                    primaryCursorOp = reader.readUnsignedByte()
                    primaryCursor = reader.readShort()
                }
                136 -> {
                    secondaryCursorOp = reader.readUnsignedByte()
                    secondaryCursor = reader.readShort()
                }
                137 -> attackCursor = reader.readShort()
                138 -> armyIcon = reader.readShort()
                139 -> spriteId = reader.readShort()
                140 -> ambientSoundVolume = reader.readUnsignedByte()
                141 -> visiblePriority = true
                142 -> mapFunction = reader.readShort()
                143 -> invisiblePriority = true
                in 150..154 -> {
                    options[opcode - 150] = reader.readString()
                }
                155 -> {
                    hue = reader.readByte().toByte()
                    saturation = reader.readByte().toByte()
                    lightness = reader.readByte().toByte()
                    opacity = reader.readByte().toByte()
                }
                158 -> mainOptionIndex = 1.toByte()
                159 -> mainOptionIndex = 0.toByte()
                160 -> {
                    val length = reader.readUnsignedByte()
                    campaigns = IntArray(length) { reader.readShort() }
                }
                162 -> vorbis = true
                163 -> slayerType = reader.readUnsignedByte()
                164 -> {
                    soundRateMin = reader.readShort()
                    soundRateMax = reader.readShort()
                }
                165 -> pickSizeShift = reader.readUnsignedByte()
                168 -> soundRangeMin = reader.readUnsignedByte()
                249 -> readParameters(reader)
            }
        }
    }
}