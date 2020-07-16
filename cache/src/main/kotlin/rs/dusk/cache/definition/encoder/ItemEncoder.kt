package rs.dusk.cache.definition.encoder

import rs.dusk.cache.DefinitionEncoder
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.core.io.write.Writer

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 13, 2020
 */
class ItemEncoder : DefinitionEncoder<ItemDefinition> {

    override fun Writer.encode(definition: ItemDefinition) {
        if (definition.id == -1) {
            return
        }

        if (definition.modelId != 0) {
            writeByte(1)
            writeShort(definition.modelId)
        }

        if (definition.name != "null" && definition.name.isNotBlank()) {
            writeByte(2)
            writeString(definition.name)
        }

        if (definition.spriteScale != 2000) {
            writeByte(4)
            writeShort(definition.spriteScale)
        }

        if (definition.spritePitch != 0) {
            writeByte(5)
            writeShort(definition.spritePitch)
        }

        if (definition.spriteCameraRoll != 0) {
            writeByte(6)
            writeShort(definition.spriteCameraRoll)
        }

        if (definition.spriteTranslateX != 0) {
            writeByte(7)
            var translateX = definition.spriteTranslateX
            if (translateX < -32767) {
                translateX += 65536
            }
            writeShort(translateX)
        }

        if (definition.spriteTranslateY != 0) {
            writeByte(8)
            var translateY = definition.spriteTranslateY
            if (translateY < -32767) {
                translateY += 65536
            }
            writeShort(translateY)
        }

        if (definition.stackable == 1) {
            writeByte(11)
        }

        if (definition.cost != 1) {
            writeByte(12)
            writeInt(definition.cost)
        }

        if (definition.members) {
            writeByte(16)
        }

        if (definition.multiStackSize != -1) {
            writeByte(18)
            writeShort(definition.multiStackSize)
        }

        if (definition.primaryMaleModel != -1) {
            writeByte(23)
            writeShort(definition.primaryMaleModel)
        }

        if (definition.secondaryMaleModel != -1) {
            writeByte(24)
            writeShort(definition.secondaryMaleModel)
        }

        if (definition.primaryFemaleModel != -1) {
            writeByte(25)
            writeShort(definition.primaryFemaleModel)
        }

        if (definition.secondaryFemaleModel != -1) {
            writeByte(26)
            writeShort(definition.secondaryFemaleModel)
        }

        val floorOptions = definition.floorOptions
        for (index in 0 until 5) {
            val option = floorOptions[index]
            if ((index == 5 && option == "Examine") || (index == 2 && option == "Take") || option == null) {
                continue
            }
            writeByte(30 + index)
            writeString(option)
        }

        val options = definition.options
        for (index in 0 until 5) {
            val option = options[index]
            if (index == 4 && option == "Drop" || option == null) {
                continue
            }
            writeByte(35 + index)
            writeString(option)
        }

        var original = definition.originalColours
        var modified = definition.modifiedColours
        if (original != null && modified != null) {
            writeByte(40)
            writeByte(original.size)
            for (i in original.indices) {
                writeShort(original[i].toInt())
                writeShort(modified[i].toInt())
            }
        }

        original = definition.originalTextureColours
        modified = definition.modifiedTextureColours
        if (original != null && modified != null) {
            writeByte(41)
            writeByte(original.size)
            for (i in original.indices) {
                writeShort(original[i].toInt())
                writeShort(modified[i].toInt())
            }
        }

        val palette = definition.recolourPalette
        if (palette != null) {
            writeByte(42)
            writeByte(palette.size)
            for (colour in palette) {
                writeByte(colour.toInt())
            }
        }

        if (definition.unnoted) {
            writeByte(65)
        }

        if (definition.tertiaryMaleModel != -1) {
            writeByte(78)
            writeShort(definition.tertiaryMaleModel)
        }

        if (definition.tertiaryFemaleModel != -1) {
            writeByte(79)
            writeShort(definition.tertiaryFemaleModel)
        }

        if (definition.primaryMaleDialogueHead != -1) {
            writeByte(90)
            writeShort(definition.primaryMaleDialogueHead)
        }

        if (definition.primaryFemaleDialogueHead != -1) {
            writeByte(91)
            writeShort(definition.primaryFemaleDialogueHead)
        }

        if (definition.secondaryMaleDialogueHead != -1) {
            writeByte(92)
            writeShort(definition.secondaryMaleDialogueHead)
        }

        if (definition.secondaryFemaleDialogueHead != -1) {
            writeByte(93)
            writeShort(definition.secondaryFemaleDialogueHead)
        }

        if (definition.spriteCameraYaw != 0) {
            writeByte(95)
            writeShort(definition.spriteCameraYaw)
        }

        if (definition.dummyItem != 0) {
            writeByte(96)
            writeByte(definition.dummyItem)
        }

        if (definition.noteId != -1) {
            writeByte(97)
            writeShort(definition.noteId)
        }

        if (definition.notedTemplateId != -1) {
            writeByte(98)
            writeShort(definition.notedTemplateId)
        }

        val stackIds = definition.stackIds
        val stackAmounts = definition.stackAmounts
        if (stackIds != null && stackAmounts != null) {
            for (i in 0 until 10) {
                val id = stackIds[i]
                val amount = stackAmounts[i]
                if (id != 0 || amount != 0) {
                    writeByte(100 + i)
                    writeShort(id)
                    writeShort(amount)
                }
            }
        }

        if (definition.floorScaleX != 128) {
            writeByte(110)
            writeShort(definition.floorScaleX)
        }

        if (definition.floorScaleY != 128) {
            writeByte(111)
            writeShort(definition.floorScaleY)
        }

        if (definition.floorScaleZ != 128) {
            writeByte(112)
            writeShort(definition.floorScaleZ)
        }

        if (definition.ambience != 0) {
            writeByte(113)
            writeByte(definition.ambience)
        }

        if (definition.diffusion != 0) {
            writeByte(114)
            writeByte(definition.diffusion / 5)
        }

        if (definition.team != 0) {
            writeByte(115)
            writeByte(definition.team)
        }

        if (definition.lendId != -1) {
            writeByte(121)
            writeShort(definition.lendId)
        }

        if (definition.lendTemplateId != -1) {
            writeByte(122)
            writeShort(definition.lendTemplateId)
        }

        if (definition.maleWieldX != 0 || definition.maleWieldY != 0 || definition.maleWieldZ != 0) {
            writeByte(125)
            writeByte(definition.maleWieldX shr 2)
            writeByte(definition.maleWieldY shr 2)
            writeByte(definition.maleWieldZ shr 2)
        }

        if (definition.femaleWieldX != 0 || definition.femaleWieldY != 0 || definition.femaleWieldZ != 0) {
            writeByte(126)
            writeByte(definition.femaleWieldX shr 2)
            writeByte(definition.femaleWieldY shr 2)
            writeByte(definition.femaleWieldZ shr 2)
        }

        if (definition.primaryCursorOpcode != -1 || definition.primaryCursor != -1) {
            writeByte(127)
            writeByte(definition.primaryCursorOpcode)
            writeShort(definition.primaryCursor)
        }

        if (definition.secondaryCursorOpcode != -1 || definition.secondaryCursor != -1) {
            writeByte(128)
            writeByte(definition.secondaryCursorOpcode)
            writeShort(definition.secondaryCursor)
        }

        if (definition.primaryInterfaceCursorOpcode != -1 || definition.primaryInterfaceCursor != -1) {
            writeByte(129)
            writeByte(definition.primaryInterfaceCursorOpcode)
            writeShort(definition.primaryInterfaceCursor)
        }

        if (definition.secondaryInterfaceCursorOpcode != -1 || definition.secondaryInterfaceCursor != -1) {
            writeByte(130)
            writeByte(definition.secondaryInterfaceCursorOpcode)
            writeShort(definition.secondaryInterfaceCursor)
        }

        val campaigns = definition.campaigns
        if (campaigns != null) {
            writeByte(132)
            writeByte(campaigns.size)
            campaigns.forEach { campaign ->
                writeShort(campaign)
            }
        }

        if (definition.pickSizeShift != 0) {
            writeByte(134)
            writeByte(definition.pickSizeShift)
        }

        if (definition.bindId != -1) {
            writeByte(139)
            writeShort(definition.bindId)
        }

        if (definition.bindTemplateId != -1) {
            writeByte(140)
            writeShort(definition.bindTemplateId)
        }

        val params = definition.params
        if (params != null) {
            writeByte(249)
            writeByte(params.size)
            params.forEach { (id, value) ->
                writeByte(value is String)
                writeMedium(id.toInt())
                if (value is String) {
                    writeString(value)
                } else if (value is Int) {
                    writeInt(value)
                }
            }
        }

        writeByte(0)
    }

}