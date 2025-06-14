package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.ITEMS
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull

class ItemDecoderFull : DefinitionDecoder<ItemDefinitionFull>(ITEMS) {

    override fun create(size: Int) = Array(size) { ItemDefinitionFull(it) }

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun ItemDefinitionFull.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> modelId = buffer.readUnsignedShort()
            2 -> name = buffer.readString()
            4 -> spriteScale = buffer.readShort()
            5 -> spritePitch = buffer.readShort()
            6 -> spriteCameraRoll = buffer.readShort()
            7 -> {
                spriteTranslateX = buffer.readShort()
                if (spriteTranslateX > 32767) {
                    spriteTranslateX -= 65536
                }
            }
            8 -> {
                spriteTranslateY = buffer.readShort()
                if (spriteTranslateY > 32767) {
                    spriteTranslateY -= 65536
                }
            }
            11 -> stackable = 1
            12 -> cost = buffer.readInt()
            16 -> members = true
            18 -> multiStackSize = buffer.readShort()
            23 -> primaryMaleModel = buffer.readUnsignedShort()
            24 -> secondaryMaleModel = buffer.readUnsignedShort()
            25 -> primaryFemaleModel = buffer.readUnsignedShort()
            26 -> secondaryFemaleModel = buffer.readUnsignedShort()
            in 30..34 -> floorOptions[opcode - 30] = buffer.readString()
            in 35..39 -> options[opcode - 35] = buffer.readString()
            40 -> readColours(buffer)
            41 -> readTextures(buffer)
            42 -> readColourPalette(buffer)
            65 -> exchangeable = true
            78 -> tertiaryMaleModel = buffer.readShort()
            79 -> tertiaryFemaleModel = buffer.readShort()
            90 -> primaryMaleDialogueHead = buffer.readShort()
            91 -> primaryFemaleDialogueHead = buffer.readShort()
            92 -> secondaryMaleDialogueHead = buffer.readShort()
            93 -> secondaryFemaleDialogueHead = buffer.readShort()
            95 -> spriteCameraYaw = buffer.readShort()
            96 -> dummyItem = buffer.readUnsignedByte()
            97 -> noteId = buffer.readShort()
            98 -> notedTemplateId = buffer.readShort()
            in 100..109 -> {
                if (stackIds == null) {
                    stackAmounts = IntArray(10)
                    stackIds = IntArray(10)
                }
                stackIds!![opcode - 100] = buffer.readShort()
                stackAmounts!![opcode - 100] = buffer.readShort()
            }
            110 -> floorScaleX = buffer.readShort()
            111 -> floorScaleZ = buffer.readShort()
            112 -> floorScaleY = buffer.readShort()
            113 -> ambience = buffer.readByte()
            114 -> diffusion = buffer.readByte() * 5
            115 -> team = buffer.readUnsignedByte()
            121 -> lendId = buffer.readShort()
            122 -> lendTemplateId = buffer.readShort()
            125 -> {
                maleWieldX = buffer.readByte() shl 2
                maleWieldZ = buffer.readByte() shl 2
                maleWieldY = buffer.readByte() shl 2
            }
            126 -> {
                femaleWieldX = buffer.readByte() shl 2
                femaleWieldZ = buffer.readByte() shl 2
                femaleWieldY = buffer.readByte() shl 2
            }
            127 -> {
                primaryCursorOpcode = buffer.readUnsignedByte()
                primaryCursor = buffer.readShort()
            }
            128 -> {
                secondaryCursorOpcode = buffer.readUnsignedByte()
                secondaryCursor = buffer.readShort()
            }
            129 -> {
                primaryInterfaceCursorOpcode = buffer.readUnsignedByte()
                primaryInterfaceCursor = buffer.readShort()
            }
            130 -> {
                secondaryInterfaceCursorOpcode = buffer.readUnsignedByte()
                secondaryInterfaceCursor = buffer.readShort()
            }
            132 -> {
                val length = buffer.readUnsignedByte()
                campaigns = IntArray(length) { buffer.readShort() }
            }
            134 -> pickSizeShift = buffer.readUnsignedByte()
            139 -> singleNoteId = buffer.readShort()
            140 -> singleNoteTemplateId = buffer.readShort()
            249 -> readParameters(buffer)
        }
    }

    override fun changeValues(definitions: Array<ItemDefinitionFull>, definition: ItemDefinitionFull) {
        if (definition.notedTemplateId != -1) {
            definition.toNote(definitions.getOrNull(definition.notedTemplateId), definitions.getOrNull(definition.noteId))
        }
        if (definition.lendTemplateId != -1) {
            definition.toLend(definitions.getOrNull(definition.lendId), definitions.getOrNull(definition.lendTemplateId))
        }
        if (definition.singleNoteTemplateId != -1) {
            definition.toSingleNote(definitions.getOrNull(definition.singleNoteTemplateId), definitions.getOrNull(definition.singleNoteId))
        }
    }
}
