package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull

class NPCEncoder : DefinitionEncoder<NPCDefinitionFull> {

    override fun Writer.encode(definition: NPCDefinitionFull, members: NPCDefinitionFull) {
        if (definition.id == -1) {
            return
        }

        val modelIds = definition.modelIds
        if (modelIds != null) {
            writeByte(1)
            writeByte(modelIds.size)
            for (models in modelIds) {
                writeShort(models)
            }
        }

        val name = definition.name
        if (name != "null") {
            writeByte(2)
            writeString(name)
        }

        if (definition.size != 1) {
            writeByte(12)
            writeByte(definition.size)
        }

        val options = definition.options
        for (index in 0 until 5) {
            val option = options[index] ?: continue
            writeByte(30 + index)
            writeString(option)
        }

        definition.writeColoursTextures(this)

        definition.writeRecolourPalette(this)

        val dialogueModels = definition.dialogueModels
        if (dialogueModels != null) {
            writeByte(60)
            writeByte(dialogueModels.size)
            for (id in dialogueModels) {
                writeShort(id)
            }
        }

        if (!definition.drawMinimapDot) {
            writeByte(93)
        }

        if (definition.combat != -1) {
            writeByte(95)
            writeShort(definition.combat)
        }

        if (definition.scaleXY != 128) {
            writeByte(97)
            writeShort(definition.scaleXY)
        }

        if (definition.scaleZ != 128) {
            writeByte(98)
            writeShort(definition.scaleZ)
        }

        if (definition.priorityRender) {
            writeByte(99)
        }

        if (definition.lightModifier != 0) {
            writeByte(100)
            writeByte(definition.lightModifier)
        }

        if (definition.shadowModifier != 0) {
            writeByte(101)
            writeByte(definition.shadowModifier / 5)
        }

        if (definition.headIcon != -1) {
            writeByte(102)
            writeShort(definition.headIcon)
        }

        if (definition.rotation != 32) {
            writeByte(103)
            writeShort(definition.rotation)
        }

        definition.writeTransforms(this, 106, 118)

        if (!definition.clickable) {
            writeByte(107)
        }

        if (!definition.slowWalk) {
            writeByte(109)
        }

        if (!definition.animateIdle) {
            writeByte(111)
        }

        if (definition.primaryShadowColour != 0.toShort() || definition.secondaryShadowColour != 0.toShort()) {
            writeByte(113)
            writeShort(definition.primaryShadowColour.toInt())
            writeShort(definition.secondaryShadowColour.toInt())
        }

        if (definition.primaryShadowModifier.toInt() != -96 || definition.secondaryShadowModifier.toInt() != -16) {
            writeByte(114)
            writeByte(definition.primaryShadowModifier.toInt())
            writeByte(definition.secondaryShadowModifier.toInt())
        }

        if (definition.walkMask.toInt() != 0) {
            writeByte(119)
            writeByte(definition.walkMask.toInt())
        }

        val translations = definition.translations
        if (translations != null) {
            writeByte(121)
            writeByte(translations.filterNotNull().size)
            for (i in translations.indices) {
                val translation = translations[i] ?: continue
                writeByte(i)
                writeByte(translation[0])
                writeByte(translation[1])
                writeByte(translation[2])
            }
        }

        if (definition.hitbarSprite != -1) {
            writeByte(122)
            writeShort(definition.hitbarSprite)
        }

        if (definition.height != -1) {
            writeByte(123)
            writeShort(definition.height)
        }

        if (definition.respawnDirection.toInt() != 4) {
            writeByte(125)
            writeByte(definition.respawnDirection.toInt())
        }

        if (definition.renderEmote != -1) {
            writeByte(127)
            writeShort(definition.renderEmote)
        }

        if (definition.idleSound != -1 || definition.crawlSound != -1 || definition.walkSound != -1 || definition.runSound != -1 || definition.soundDistance != 0) {
            writeByte(134)
            writeShort(definition.idleSound)
            writeShort(definition.crawlSound)
            writeShort(definition.walkSound)
            writeShort(definition.runSound)
            writeByte(definition.soundDistance)
        }

        if (definition.primaryCursorOp != -1 || definition.primaryCursor != -1) {
            writeByte(135)
            writeByte(definition.primaryCursorOp)
            writeShort(definition.primaryCursor)
        }

        if (definition.secondaryCursorOp != -1 || definition.secondaryCursor != -1) {
            writeByte(136)
            writeByte(definition.secondaryCursorOp)
            writeShort(definition.secondaryCursor)
        }

        if (definition.attackCursor != -1) {
            writeByte(137)
            writeShort(definition.attackCursor)
        }

        if (definition.armyIcon != -1) {
            writeByte(138)
            writeShort(definition.armyIcon)
        }

        if (definition.spriteId != -1) {
            writeByte(139)
            writeShort(definition.spriteId)
        }

        if (definition.ambientSoundVolume != 255) {
            writeByte(140)
            writeByte(definition.ambientSoundVolume)
        }

        if (definition.visiblePriority) {
            writeByte(141)
        }

        if (definition.mapFunction != -1) {
            writeByte(142)
            writeShort(definition.mapFunction)
        }

        if (definition.invisiblePriority) {
            writeByte(143)
        }

        val membersOptions = members.options
        for (index in 0 until 5) {
            val option = options[index]
            if (option != null) {
                continue
            }
            val membersOption = membersOptions[index] ?: continue
            writeByte(150 + index)
            writeString(membersOption)
        }

        if (definition.hue.toInt() != 0 || definition.saturation.toInt() != 0 || definition.lightness.toInt() != 0 || definition.opacity.toInt() != 0) {
            writeByte(155)
            writeByte(definition.hue.toInt())
            writeByte(definition.saturation.toInt())
            writeByte(definition.lightness.toInt())
            writeByte(definition.opacity.toInt())
        }

        if (definition.mainOptionIndex.toInt() == 1) {
            writeByte(158)
        }

        if (definition.mainOptionIndex.toInt() == 0) {
            writeByte(159)
        }

        val campaigns = definition.campaigns
        if (campaigns != null) {
            writeByte(160)
            writeByte(campaigns.size)
            campaigns.forEach {
                writeShort(it)
            }
        }

        if (definition.vorbis) {
            writeByte(162)
        }

        if (definition.slayerType != -1) {
            writeByte(163)
            writeByte(definition.slayerType)
        }

        if (definition.soundRateMin != 256 || definition.soundRateMax != 256) {
            writeByte(164)
            writeShort(definition.soundRateMin)
            writeShort(definition.soundRateMax)
        }

        if (definition.pickSizeShift != 0) {
            writeByte(165)
            writeByte(definition.pickSizeShift)
        }

        if (definition.soundRangeMin != 0) {
            writeByte(165)
            writeByte(definition.soundRangeMin)
        }

        definition.writeParameters(this)
        writeByte(0)
    }
}
