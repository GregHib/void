package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.*
import world.gregs.voidps.cache.type.Type

data class NPCDefinitionFull(
    override var id: Int = -1,
    var modelIds: IntArray? = null,
    var name: String = "null",
    var size: Int = 1,
    var options: Array<String?> = arrayOf(null, null, null, null, null, "Examine"),
    override var originalColours: ShortArray? = null,
    override var modifiedColours: ShortArray? = null,
    override var originalTextureColours: ShortArray? = null,
    override var modifiedTextureColours: ShortArray? = null,
    override var recolourPalette: ByteArray? = null,
    var dialogueModels: IntArray? = null,
    var drawMinimapDot: Boolean = true,
    var combat: Int = -1,
    var scaleXY: Int = 128,
    var scaleZ: Int = 128,
    var priorityRender: Boolean = false,
    var lightModifier: Int = 0,
    var shadowModifier: Int = 0,
    var headIcon: Int = -1,
    var rotation: Int = 32,
    override var varbit: Int = -1,
    override var varp: Int = -1,
    override var transforms: IntArray? = null,
    var clickable: Boolean = true,
    var slowWalk: Boolean = true,
    var animateIdle: Boolean = true,
    var primaryShadowColour: Short = 0,
    var secondaryShadowColour: Short = 0,
    var primaryShadowModifier: Byte = -96,
    var secondaryShadowModifier: Byte = -16,
    var walkMode: Byte = 0,
    var translations: Array<IntArray?>? = null,
    var hitbarSprite: Int = -1,
    var height: Int = -1,
    var respawnDirection: Byte = 4,
    var renderEmote: Int = -1,
    var idleSound: Int = -1,
    var crawlSound: Int = -1,
    var walkSound: Int = -1,
    var runSound: Int = -1,
    var soundDistance: Int = 0,
    var primaryCursorOp: Int = -1,
    var primaryCursor: Int = -1,
    var secondaryCursorOp: Int = -1,
    var secondaryCursor: Int = -1,
    var attackCursor: Int = -1,
    var armyIcon: Int = -1,
    var spriteId: Int = -1,
    var ambientSoundVolume: Int = 255,
    var visiblePriority: Boolean = false,
    var mapFunction: Int = -1,
    var invisiblePriority: Boolean = false,
    var hue: Byte = 0,
    var saturation: Byte = 0,
    var lightness: Byte = 0,
    var opacity: Byte = 0,
    var mainOptionIndex: Byte = -1,
    var campaigns: IntArray? = null,
    var vorbis: Boolean = false,
    var slayerType: Int = -1,
    var soundRateMin: Int = 256,
    var soundRateMax: Int = 256,
    var pickSizeShift: Int = 0,
    var soundRangeMin: Int = 0,
    override var params: Map<Int, Any>? = null,
    override var stringId: String = "", // TODO remove
    override var extras: Map<String, Any>? = null,
) : Definition,
    Transforms,
    Recolourable,
    ColourPalette,
    Parameterized,
    Extra, Type {

    override fun decode(reader: Reader) {
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
                in 150..154 -> options[opcode - 150] = reader.readString()
                155 -> {
                    hue = reader.readByte().toByte()
                    saturation = reader.readByte().toByte()
                    lightness = reader.readByte().toByte()
                    opacity = reader.readByte().toByte()
                }
                158 -> mainOptionIndex = 1.toByte()
                159 -> mainOptionIndex = 0.toByte()
                160 -> campaigns = IntArray(reader.readUnsignedByte()) { reader.readShort() }
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

    override fun encode(writer: Writer) {
        if (id == -1) {
            return
        }
        val modelIds = modelIds
        if (modelIds != null) {
            writer.writeByte(1)
            writer.writeByte(modelIds.size)
            for (models in modelIds) {
                writer.writeShort(models)
            }
        }
        if (name != "null") {
            writer.writeByte(2)
            writer.writeString(name)
        }
        if (size != 1) {
            writer.writeByte(12)
            writer.writeByte(size)
        }
        for (index in 0 until 5) {
            val option = options[index] ?: continue
            writer.writeByte(30 + index)
            writer.writeString(option)
        }
        writeColoursTextures(writer)
        writeRecolourPalette(writer)
        val dialogueModels = dialogueModels
        if (dialogueModels != null) {
            writer.writeByte(60)
            writer.writeByte(dialogueModels.size)
            for (id in dialogueModels) {
                writer.writeShort(id)
            }
        }
        if (!drawMinimapDot) {
            writer.writeByte(93)
        }
        if (combat != -1) {
            writer.writeByte(95)
            writer.writeShort(combat)
        }
        if (scaleXY != 128) {
            writer.writeByte(97)
            writer.writeShort(scaleXY)
        }
        if (scaleZ != 128) {
            writer.writeByte(98)
            writer.writeShort(scaleZ)
        }
        if (priorityRender) {
            writer.writeByte(99)
        }
        if (lightModifier != 0) {
            writer.writeByte(100)
            writer.writeByte(lightModifier)
        }
        if (shadowModifier != 0) {
            writer.writeByte(101)
            writer.writeByte(shadowModifier / 5)
        }
        if (headIcon != -1) {
            writer.writeByte(102)
            writer.writeShort(headIcon)
        }
        if (rotation != 32) {
            writer.writeByte(103)
            writer.writeShort(rotation)
        }
        writeTransforms(writer, 106, 118)
        if (!clickable) {
            writer.writeByte(107)
        }
        if (!slowWalk) {
            writer.writeByte(109)
        }
        if (!animateIdle) {
            writer.writeByte(111)
        }
        if (primaryShadowColour != 0.toShort() || secondaryShadowColour != 0.toShort()) {
            writer.writeByte(113)
            writer.writeShort(primaryShadowColour.toInt())
            writer.writeShort(secondaryShadowColour.toInt())
        }
        if (primaryShadowModifier.toInt() != -96 || secondaryShadowModifier.toInt() != -16) {
            writer.writeByte(114)
            writer.writeByte(primaryShadowModifier.toInt())
            writer.writeByte(secondaryShadowModifier.toInt())
        }
        if (walkMode.toInt() != 0) {
            writer.writeByte(119)
            writer.writeByte(walkMode.toInt())
        }
        val translations = translations
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
        if (hitbarSprite != -1) {
            writer.writeByte(122)
            writer.writeShort(hitbarSprite)
        }
        if (height != -1) {
            writer.writeByte(123)
            writer.writeShort(height)
        }
        if (respawnDirection.toInt() != 4) {
            writer.writeByte(125)
            writer.writeByte(respawnDirection.toInt())
        }
        if (renderEmote != -1) {
            writer.writeByte(127)
            writer.writeShort(renderEmote)
        }
        if (idleSound != -1 || crawlSound != -1 || walkSound != -1 || runSound != -1 || soundDistance != 0) {
            writer.writeByte(134)
            writer.writeShort(idleSound)
            writer.writeShort(crawlSound)
            writer.writeShort(walkSound)
            writer.writeShort(runSound)
            writer.writeByte(soundDistance)
        }
        if (primaryCursorOp != -1 || primaryCursor != -1) {
            writer.writeByte(135)
            writer.writeByte(primaryCursorOp)
            writer.writeShort(primaryCursor)
        }
        if (secondaryCursorOp != -1 || secondaryCursor != -1) {
            writer.writeByte(136)
            writer.writeByte(secondaryCursorOp)
            writer.writeShort(secondaryCursor)
        }
        if (attackCursor != -1) {
            writer.writeByte(137)
            writer.writeShort(attackCursor)
        }
        if (armyIcon != -1) {
            writer.writeByte(138)
            writer.writeShort(armyIcon)
        }
        if (spriteId != -1) {
            writer.writeByte(139)
            writer.writeShort(spriteId)
        }
        if (ambientSoundVolume != 255) {
            writer.writeByte(140)
            writer.writeByte(ambientSoundVolume)
        }
        if (visiblePriority) {
            writer.writeByte(141)
        }
        if (mapFunction != -1) {
            writer.writeByte(142)
            writer.writeShort(mapFunction)
        }
        if (invisiblePriority) {
            writer.writeByte(143)
        }
        // FIXME member options
//        for (index in 0 until 5) {
//            val option = options[index] ?: continue
//            writer.writeByte(150 + index)
//            writer.writeString(option)
//        }
        if (hue.toInt() != 0 || saturation.toInt() != 0 || lightness.toInt() != 0 || opacity.toInt() != 0) {
            writer.writeByte(155)
            writer.writeByte(hue.toInt())
            writer.writeByte(saturation.toInt())
            writer.writeByte(lightness.toInt())
            writer.writeByte(opacity.toInt())
        }
        if (mainOptionIndex.toInt() == 1) {
            writer.writeByte(158)
        }
        if (mainOptionIndex.toInt() == 0) {
            writer.writeByte(159)
        }
        val campaigns = campaigns
        if (campaigns != null) {
            writer.writeByte(160)
            writer.writeByte(campaigns.size)
            for (it in campaigns) {
                writer.writeShort(it)
            }
        }
        if (vorbis) {
            writer.writeByte(162)
        }
        if (slayerType != -1) {
            writer.writeByte(163)
            writer.writeByte(slayerType)
        }
        if (soundRateMin != 256 || soundRateMax != 256) {
            writer.writeByte(164)
            writer.writeShort(soundRateMin)
            writer.writeShort(soundRateMax)
        }
        if (pickSizeShift != 0) {
            writer.writeByte(165)
            writer.writeByte(pickSizeShift)
        }
        if (soundRangeMin != 0) {
            writer.writeByte(165)
            writer.writeByte(soundRangeMin)
        }
        writeParameters(writer)
        writer.writeByte(0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NPCDefinitionFull

        if (id != other.id) return false
        if (modelIds != null) {
            if (other.modelIds == null) return false
            if (!modelIds.contentEquals(other.modelIds)) return false
        } else if (other.modelIds != null) {
            return false
        }
        if (name != other.name) return false
        if (size != other.size) return false
        if (!options.contentEquals(other.options)) return false
        if (originalColours != null) {
            if (other.originalColours == null) return false
            if (!originalColours.contentEquals(other.originalColours)) return false
        } else if (other.originalColours != null) {
            return false
        }
        if (modifiedColours != null) {
            if (other.modifiedColours == null) return false
            if (!modifiedColours.contentEquals(other.modifiedColours)) return false
        } else if (other.modifiedColours != null) {
            return false
        }
        if (originalTextureColours != null) {
            if (other.originalTextureColours == null) return false
            if (!originalTextureColours.contentEquals(other.originalTextureColours)) return false
        } else if (other.originalTextureColours != null) {
            return false
        }
        if (modifiedTextureColours != null) {
            if (other.modifiedTextureColours == null) return false
            if (!modifiedTextureColours.contentEquals(other.modifiedTextureColours)) return false
        } else if (other.modifiedTextureColours != null) {
            return false
        }
        if (recolourPalette != null) {
            if (other.recolourPalette == null) return false
            if (!recolourPalette.contentEquals(other.recolourPalette)) return false
        } else if (other.recolourPalette != null) {
            return false
        }
        if (dialogueModels != null) {
            if (other.dialogueModels == null) return false
            if (!dialogueModels.contentEquals(other.dialogueModels)) return false
        } else if (other.dialogueModels != null) {
            return false
        }
        if (drawMinimapDot != other.drawMinimapDot) return false
        if (combat != other.combat) return false
        if (scaleXY != other.scaleXY) return false
        if (scaleZ != other.scaleZ) return false
        if (priorityRender != other.priorityRender) return false
        if (lightModifier != other.lightModifier) return false
        if (shadowModifier != other.shadowModifier) return false
        if (headIcon != other.headIcon) return false
        if (rotation != other.rotation) return false
        if (varbit != other.varbit) return false
        if (varp != other.varp) return false
        if (transforms != null) {
            if (other.transforms == null) return false
            if (!transforms.contentEquals(other.transforms)) return false
        } else if (other.transforms != null) {
            return false
        }
        if (clickable != other.clickable) return false
        if (slowWalk != other.slowWalk) return false
        if (animateIdle != other.animateIdle) return false
        if (primaryShadowColour != other.primaryShadowColour) return false
        if (secondaryShadowColour != other.secondaryShadowColour) return false
        if (primaryShadowModifier != other.primaryShadowModifier) return false
        if (secondaryShadowModifier != other.secondaryShadowModifier) return false
        if (walkMode != other.walkMode) return false
        if (translations != null) {
            if (other.translations == null) return false
            if (!translations.contentDeepEquals(other.translations)) return false
        } else if (other.translations != null) {
            return false
        }
        if (hitbarSprite != other.hitbarSprite) return false
        if (height != other.height) return false
        if (respawnDirection != other.respawnDirection) return false
        if (renderEmote != other.renderEmote) return false
        if (idleSound != other.idleSound) return false
        if (crawlSound != other.crawlSound) return false
        if (walkSound != other.walkSound) return false
        if (runSound != other.runSound) return false
        if (soundDistance != other.soundDistance) return false
        if (primaryCursorOp != other.primaryCursorOp) return false
        if (primaryCursor != other.primaryCursor) return false
        if (secondaryCursorOp != other.secondaryCursorOp) return false
        if (secondaryCursor != other.secondaryCursor) return false
        if (attackCursor != other.attackCursor) return false
        if (armyIcon != other.armyIcon) return false
        if (spriteId != other.spriteId) return false
        if (ambientSoundVolume != other.ambientSoundVolume) return false
        if (visiblePriority != other.visiblePriority) return false
        if (mapFunction != other.mapFunction) return false
        if (invisiblePriority != other.invisiblePriority) return false
        if (hue != other.hue) return false
        if (saturation != other.saturation) return false
        if (lightness != other.lightness) return false
        if (opacity != other.opacity) return false
        if (mainOptionIndex != other.mainOptionIndex) return false
        if (campaigns != null) {
            if (other.campaigns == null) return false
            if (!campaigns.contentEquals(other.campaigns)) return false
        } else if (other.campaigns != null) {
            return false
        }
        if (vorbis != other.vorbis) return false
        if (slayerType != other.slayerType) return false
        if (soundRateMin != other.soundRateMin) return false
        if (soundRateMax != other.soundRateMax) return false
        if (pickSizeShift != other.pickSizeShift) return false
        if (soundRangeMin != other.soundRangeMin) return false
        if (params != other.params) return false
        if (stringId != other.stringId) return false
        return extras == other.extras
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (modelIds?.contentHashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + size
        result = 31 * result + options.contentHashCode()
        result = 31 * result + (originalColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedColours?.contentHashCode() ?: 0)
        result = 31 * result + (originalTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + (recolourPalette?.contentHashCode() ?: 0)
        result = 31 * result + (dialogueModels?.contentHashCode() ?: 0)
        result = 31 * result + drawMinimapDot.hashCode()
        result = 31 * result + combat
        result = 31 * result + scaleXY
        result = 31 * result + scaleZ
        result = 31 * result + priorityRender.hashCode()
        result = 31 * result + lightModifier
        result = 31 * result + shadowModifier
        result = 31 * result + headIcon
        result = 31 * result + rotation
        result = 31 * result + varbit
        result = 31 * result + varp
        result = 31 * result + (transforms?.contentHashCode() ?: 0)
        result = 31 * result + clickable.hashCode()
        result = 31 * result + slowWalk.hashCode()
        result = 31 * result + animateIdle.hashCode()
        result = 31 * result + primaryShadowColour
        result = 31 * result + secondaryShadowColour
        result = 31 * result + primaryShadowModifier
        result = 31 * result + secondaryShadowModifier
        result = 31 * result + walkMode
        result = 31 * result + (translations?.contentDeepHashCode() ?: 0)
        result = 31 * result + hitbarSprite
        result = 31 * result + height
        result = 31 * result + respawnDirection
        result = 31 * result + renderEmote
        result = 31 * result + idleSound
        result = 31 * result + crawlSound
        result = 31 * result + walkSound
        result = 31 * result + runSound
        result = 31 * result + soundDistance
        result = 31 * result + primaryCursorOp
        result = 31 * result + primaryCursor
        result = 31 * result + secondaryCursorOp
        result = 31 * result + secondaryCursor
        result = 31 * result + attackCursor
        result = 31 * result + armyIcon
        result = 31 * result + spriteId
        result = 31 * result + ambientSoundVolume
        result = 31 * result + visiblePriority.hashCode()
        result = 31 * result + mapFunction
        result = 31 * result + invisiblePriority.hashCode()
        result = 31 * result + hue
        result = 31 * result + saturation
        result = 31 * result + lightness
        result = 31 * result + opacity
        result = 31 * result + mainOptionIndex
        result = 31 * result + (campaigns?.contentHashCode() ?: 0)
        result = 31 * result + vorbis.hashCode()
        result = 31 * result + slayerType
        result = 31 * result + soundRateMin
        result = 31 * result + soundRateMax
        result = 31 * result + pickSizeShift
        result = 31 * result + soundRangeMin
        result = 31 * result + (params?.hashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        val EMPTY = NPCDefinitionFull()
    }
}
