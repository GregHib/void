package rs.dusk.cache.definition.decoder

import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices.ITEMS
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class ItemDecoder : DefinitionDecoder<ItemDefinition>(ITEMS) {

    override fun create() = ItemDefinition()

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun ItemDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> modelId = buffer.readShort()
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
            42 -> {
                val length = buffer.readUnsignedByte()
                recolourPalette = ByteArray(length)
                repeat(length) { count ->
                    recolourPalette!![count] = buffer.readByte().toByte()
                }
            }
            65 -> unnoted = true
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
            111 -> floorScaleY = buffer.readShort()
            112 -> floorScaleZ = buffer.readShort()
            113 -> ambience = buffer.readByte()
            114 -> diffusion = buffer.readByte() * 5
            115 -> team = buffer.readUnsignedByte()
            121 -> lendId = buffer.readShort()
            122 -> lendTemplateId = buffer.readShort()
            125 -> {
                maleWieldX = buffer.readByte() shl 2
                maleWieldY = buffer.readByte() shl 2
                maleWieldZ = buffer.readByte() shl 2
            }
            126 -> {
                femaleWieldX = buffer.readByte() shl 2
                femaleWieldY = buffer.readByte() shl 2
                femaleWieldZ = buffer.readByte() shl 2
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
                campaigns = IntArray(length)
                repeat(length) { count ->
                    campaigns!![count] = buffer.readShort()
                }
            }
            134 -> pickSizeShift = buffer.readUnsignedByte()
            139 -> bindId = buffer.readShort()
            140 -> bindTemplateId = buffer.readShort()
            249 -> readParameters(buffer)
        }
    }

    override fun ItemDefinition.changeValues() {
        if (notedTemplateId != -1) {
            toNote(getSafe(notedTemplateId), getSafe(noteId))
        }
        if (lendTemplateId != -1) {
            toLend(getSafe(lendId), getSafe(lendTemplateId))
        }
        if (bindTemplateId != -1) {
            toBind(getSafe(bindTemplateId), getSafe(bindId))
        }
    }

    fun ItemDefinition.toLend(item: ItemDefinition, template: ItemDefinition) {
        modifiedColours = item.modifiedColours
        primaryMaleDialogueHead = item.primaryMaleDialogueHead
        secondaryMaleDialogueHead = item.secondaryMaleDialogueHead
        tertiaryMaleModel = item.tertiaryMaleModel
        team = item.team
        params = item.params
        members = item.members
        modifiedTextureColours = item.modifiedTextureColours
        maleWieldY = item.maleWieldY
        secondaryFemaleModel = item.secondaryFemaleModel
        spriteCameraYaw = template.spriteCameraYaw
        floorOptions = item.floorOptions
        secondaryFemaleDialogueHead = item.secondaryFemaleDialogueHead
        recolourPalette = item.recolourPalette
        femaleWieldY = item.femaleWieldY
        spritePitch = template.spritePitch
        primaryFemaleModel = item.primaryFemaleModel
        modelId = template.modelId
        options = arrayOfNulls(5)
        spriteCameraRoll = template.spriteCameraRoll
        spriteTranslateY = template.spriteTranslateY
        originalTextureColours = item.originalTextureColours
        femaleWieldX = item.femaleWieldX
        secondaryMaleModel = item.secondaryMaleModel
        cost = 0
        maleWieldZ = item.maleWieldZ
        originalColours = item.originalColours
        spriteTranslateX = template.spriteTranslateX
        femaleWieldZ = item.femaleWieldZ
        primaryFemaleDialogueHead = item.primaryFemaleDialogueHead
        spriteScale = template.spriteScale
        name = item.name
        tertiaryFemaleModel = item.tertiaryFemaleModel
        primaryMaleModel = item.primaryMaleModel
        maleWieldX = item.maleWieldX
        System.arraycopy(item.options, 0, options, 0, 4)
        options[4] = "Discard"
    }

    fun ItemDefinition.toNote(template: ItemDefinition, item: ItemDefinition) {
        spriteTranslateY = template.spriteTranslateY
        originalColours = template.originalColours
        cost = item.cost
        name = item.name
        modifiedTextureColours = template.modifiedTextureColours
        spriteCameraRoll = template.spriteCameraRoll
        spriteCameraYaw = template.spriteCameraYaw
        originalTextureColours = template.originalTextureColours
        modelId = template.modelId
        spriteScale = template.spriteScale
        recolourPalette = template.recolourPalette
        stackable = 1
        spritePitch = template.spritePitch
        spriteTranslateX = template.spriteTranslateX
        members = item.members
        modifiedColours = template.modifiedColours
    }

    fun ItemDefinition.toBind(template: ItemDefinition, item: ItemDefinition) {
        cost = 0
        tertiaryMaleModel = item.tertiaryMaleModel
        stackable = item.stackable
        members = item.members
        recolourPalette = item.recolourPalette
        spriteTranslateY = template.spriteTranslateY
        team = item.team
        secondaryMaleModel = item.secondaryMaleModel
        options = arrayOfNulls(5)
        floorOptions = item.floorOptions
        maleWieldY = item.maleWieldY
        primaryMaleDialogueHead = item.primaryMaleDialogueHead
        femaleWieldY = item.femaleWieldY
        name = item.name
        spriteScale = template.spriteScale
        originalColours = item.originalColours
        secondaryFemaleDialogueHead = item.secondaryFemaleDialogueHead
        params = item.params
        primaryFemaleModel = item.primaryFemaleModel
        spritePitch = template.spritePitch
        spriteCameraRoll = template.spriteCameraRoll
        femaleWieldX = item.femaleWieldX
        secondaryMaleDialogueHead = item.secondaryMaleDialogueHead
        tertiaryFemaleModel = item.tertiaryFemaleModel
        modifiedTextureColours = item.modifiedTextureColours
        maleWieldX = item.maleWieldX
        primaryFemaleDialogueHead = item.primaryFemaleDialogueHead
        modelId = template.modelId
        modifiedColours = item.modifiedColours
        secondaryFemaleModel = item.secondaryFemaleModel
        spriteTranslateX = template.spriteTranslateX
        spriteCameraYaw = template.spriteCameraYaw
        primaryMaleModel = item.primaryMaleModel
        femaleWieldZ = item.femaleWieldZ
        maleWieldZ = item.maleWieldZ
        originalTextureColours = item.originalTextureColours
        System.arraycopy(item.options, 0, options, 0, 4)
        options[4] = "Discard"
    }
}