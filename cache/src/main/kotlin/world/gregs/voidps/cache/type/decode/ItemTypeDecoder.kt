package world.gregs.voidps.cache.type.decode

import world.gregs.voidps.cache.type.TypeDecoder
import world.gregs.voidps.cache.type.field.codec.ByteCodec
import world.gregs.voidps.cache.type.field.codec.ShortCodec
import world.gregs.voidps.cache.type.field.codec.UnsignedByteCodec
import world.gregs.voidps.cache.type.field.type.ItemStack
import world.gregs.voidps.cache.type.types.ItemType

class ItemTypeDecoder : TypeDecoder<ItemType>(250) {
    val id = int("id", default = -1)
    val stringId = string("[section]", default = "")
    val modelId = ushort("model_id", default = 0, opcode = 1)
    val name = string("name", default = "null", opcode = 2)
    val spriteScale = short("sprite_scale", default = 2000, opcode = 4)
    val spritePitch = short("sprite_pitch", default = 0, opcode = 5)
    val spriteCameraRoll = short("sprite_camera_roll", default = 0, opcode = 6)
    val spriteTranslateX = short("sprite_translate_x", default = 0, opcode = 7)
    val spriteTranslateY = short("sprite_translate_y", default = 0, opcode = 8)
    val stackable = intLiteral("stackable", default = 0, value = 1, opcode = 11)
    val cost = int("cost", default = 1, opcode = 12)
    val members = boolLiteral("members", default = false, value = true, opcode = 16)
    val multiStackSize = short("multi_stack_size", default = -1, opcode = 18)
    val primaryMaleModel = ushort("primary_male_model", default = -1, opcode = 23)
    val secondaryMaleModel = ushort("secondary_male_model", default = -1, opcode = 24)
    val primaryFemaleModel = ushort("primary_female_model", default = -1, opcode = 25)
    val secondaryFemaleModel = ushort("secondary_female_model", default = -1, opcode = 26)
    val floorOptions = indexedStringArray("floor_options", arrayOf(null, null, "Take", null, null, "Examine"), opcodes = 30..34)
    val options = indexedStringArray("options", arrayOf(null, null, null, null, "Drop"), opcodes = 35..39)
    val colours = colours("original_colours", "modified_colours", opcode = 40)
    val textureColours = colours("original_texture_colours", "modified_texture_colours", opcode = 41)
    val recolourPalette = byteArray("recolour_palette", opcode = 42)
    val exchangeable = boolLiteral("exchangeable", default = false, value = true, opcode = 65)
    val tertiaryMaleModel = short("tertiary_male_model", default = -1, opcode = 78)
    val tertiaryFemaleModel = short("tertiary_female_model", default = -1, opcode = 79)
    val primaryMaleDialogueHead = short("primary_male_dialogue_head", default = -1, opcode = 90)
    val primaryFemaleDialogueHead = short("secondary_male_dialogue_head", default = -1, opcode = 91)
    val secondaryMaleDialogueHead = short("primary_female_dialogue_head", default = -1, opcode = 92)
    val secondaryFemaleDialogueHead = short("secondary_female_dialogue_head", default = -1, opcode = 93)
    val spriteCameraYaw = short("sprite_camera_yaw", default = 0, opcode = 95)
    val dummyItem = ubyte("dummy_item", default = 0, opcode = 96)
    val noteId = short("note_id", default = -1, opcode = 97)
    val notedTemplateId = short("noted_template_id", default = -1, opcode = 98)
    val stack = register(ItemStack("stack_ids", "stack_amounts", 100), opcodes = 100..109)
    val floorScaleX = short("floor_scale_x", default = 128, opcode = 110)
    val floorScaleY = short("floor_scale_y", default = 128, opcode = 111)
    val floorScaleZ = short("floor_scale_z", default = 128, opcode = 112)
    val ambience = byte("ambience", default = 0, opcode = 113)
    val diffusion = byte("diffusion", default = 0, opcode = 114)
    val team = ubyte("team", default = 0, opcode = 115)
    val lendId = short("lend_id", default = -1, opcode = 121)
    val lendTemplateId = short("lend_template_id", default = -1, opcode = 122)
    val maleWield = triple(ByteCodec("male_wield_x", 0), ByteCodec("male_wield_y", 0), ByteCodec("male_wield_z", 0), opcode = 125)
    val femaleWield = triple(ByteCodec("female_wield_x", 0), ByteCodec("female_wield_y", 0), ByteCodec("female_wield_z", 0), opcode = 126)
    val primaryCursor = pair(UnsignedByteCodec("primary_cursor_opcode", default = -1), ShortCodec("primary_cursor", -1), opcode = 127)
    val secondaryCursor = pair(UnsignedByteCodec("secondary_cursor_opcode", -1), ShortCodec("secondary_cursor", -1), opcode = 128)
    val primaryInterfaceCursor = pair(UnsignedByteCodec("primary_interface_cursor_opcode", -1), ShortCodec("primary_interface_cursor", -1), opcode = 129)
    val secondaryInterfaceCursor = pair(UnsignedByteCodec("secondary_interface_cursor_opcode", -1), ShortCodec("secondary_interface_cursor", -1), opcode = 130)
    val campaigns = shortArray("campaigns", opcode = 132)
    val pickSizeShift = ubyte("pick_size_shift", default = 0, opcode = 134)
    val singleNoteId = short("single_note_id", default = -1, opcode = 139)
    val singleNoteTemplateId = short("single_note_template_id", default = -1, opcode = 140)
    val parameters = params(opcode = 249) {
        add("1", 1)
        add("2", 2)
    }

    override fun create(): ItemType {
        var equipIndex = -1
        if (primaryMaleModel.value >= 0) {
            equipIndex = 1
        }
        if (primaryFemaleModel.value >= 0) {
           equipIndex = 2
        }
        return ItemType(
            id = id.value,
            name = name.value,
            stackable = stackable.value,
            cost = cost.value,
            members = members.value,
            floorOptions = floorOptions.value,
            options = options.value,
            exchangeable = exchangeable.value,
            dummyItem = dummyItem.value,
            noteId = noteId.value,
            notedTemplateId = notedTemplateId.value,
            lendId = lendId.value,
            lendTemplateId = lendTemplateId.value,
            stringId = stringId.value,
            equipIndex = equipIndex, // FIXME
            params = parameters.value,
        )
    }

    override fun load(type: ItemType) {
        id.value = type.id
        name.value = type.name
        stackable.value = type.stackable
        cost.value = type.cost
        members.value = type.members
        floorOptions.value = type.floorOptions
        options.value = type.options
        exchangeable.value = type.exchangeable
        dummyItem.value = type.dummyItem
        noteId.value = type.noteId
        notedTemplateId.value = type.notedTemplateId
        lendId.value = type.lendId
        lendTemplateId.value = type.lendTemplateId
        stringId.value = type.stringId
        parameters.value = type.params
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemTypeDecoder

        if (id != other.id) return false
        if (modelId != other.modelId) return false
        if (name != other.name) return false
        if (spriteScale != other.spriteScale) return false
        if (spritePitch != other.spritePitch) return false
        if (spriteCameraRoll != other.spriteCameraRoll) return false
        if (spriteTranslateX != other.spriteTranslateX) return false
        if (spriteTranslateY != other.spriteTranslateY) return false
        if (stackable != other.stackable) return false
        if (cost != other.cost) return false
        if (members != other.members) return false
        if (multiStackSize != other.multiStackSize) return false
        if (primaryMaleModel != other.primaryMaleModel) return false
        if (secondaryMaleModel != other.secondaryMaleModel) return false
        if (primaryFemaleModel != other.primaryFemaleModel) return false
        if (secondaryFemaleModel != other.secondaryFemaleModel) return false
        if (floorOptions != other.floorOptions) return false
        if (options != other.options) return false
        if (colours != other.colours) return false
        if (textureColours != other.textureColours) return false
        if (recolourPalette != other.recolourPalette) return false
        if (exchangeable != other.exchangeable) return false
        if (tertiaryMaleModel != other.tertiaryMaleModel) return false
        if (tertiaryFemaleModel != other.tertiaryFemaleModel) return false
        if (primaryMaleDialogueHead != other.primaryMaleDialogueHead) return false
        if (primaryFemaleDialogueHead != other.primaryFemaleDialogueHead) return false
        if (secondaryMaleDialogueHead != other.secondaryMaleDialogueHead) return false
        if (secondaryFemaleDialogueHead != other.secondaryFemaleDialogueHead) return false
        if (spriteCameraYaw != other.spriteCameraYaw) return false
        if (dummyItem != other.dummyItem) return false
        if (noteId != other.noteId) return false
        if (notedTemplateId != other.notedTemplateId) return false
        if (stack != other.stack) return false
        if (floorScaleX != other.floorScaleX) return false
        if (floorScaleY != other.floorScaleY) return false
        if (floorScaleZ != other.floorScaleZ) return false
        if (ambience != other.ambience) return false
        if (diffusion != other.diffusion) return false
        if (team != other.team) return false
        if (lendId != other.lendId) return false
        if (lendTemplateId != other.lendTemplateId) return false
        if (maleWield != other.maleWield) return false
        if (femaleWield != other.femaleWield) return false
        if (primaryCursor != other.primaryCursor) return false
        if (secondaryCursor != other.secondaryCursor) return false
        if (primaryInterfaceCursor != other.primaryInterfaceCursor) return false
        if (secondaryInterfaceCursor != other.secondaryInterfaceCursor) return false
        if (campaigns != other.campaigns) return false
        if (pickSizeShift != other.pickSizeShift) return false
        if (singleNoteId != other.singleNoteId) return false
        if (singleNoteTemplateId != other.singleNoteTemplateId) return false
        if (stringId != other.stringId) return false
        if (parameters != other.parameters) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + modelId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + spriteScale.hashCode()
        result = 31 * result + spritePitch.hashCode()
        result = 31 * result + spriteCameraRoll.hashCode()
        result = 31 * result + spriteTranslateX.hashCode()
        result = 31 * result + spriteTranslateY.hashCode()
        result = 31 * result + stackable.hashCode()
        result = 31 * result + cost.hashCode()
        result = 31 * result + members.hashCode()
        result = 31 * result + multiStackSize.hashCode()
        result = 31 * result + primaryMaleModel.hashCode()
        result = 31 * result + secondaryMaleModel.hashCode()
        result = 31 * result + primaryFemaleModel.hashCode()
        result = 31 * result + secondaryFemaleModel.hashCode()
        result = 31 * result + floorOptions.hashCode()
        result = 31 * result + options.hashCode()
        result = 31 * result + colours.hashCode()
        result = 31 * result + textureColours.hashCode()
        result = 31 * result + recolourPalette.hashCode()
        result = 31 * result + exchangeable.hashCode()
        result = 31 * result + tertiaryMaleModel.hashCode()
        result = 31 * result + tertiaryFemaleModel.hashCode()
        result = 31 * result + primaryMaleDialogueHead.hashCode()
        result = 31 * result + primaryFemaleDialogueHead.hashCode()
        result = 31 * result + secondaryMaleDialogueHead.hashCode()
        result = 31 * result + secondaryFemaleDialogueHead.hashCode()
        result = 31 * result + spriteCameraYaw.hashCode()
        result = 31 * result + dummyItem.hashCode()
        result = 31 * result + noteId.hashCode()
        result = 31 * result + notedTemplateId.hashCode()
        result = 31 * result + stack.hashCode()
        result = 31 * result + floorScaleX.hashCode()
        result = 31 * result + floorScaleY.hashCode()
        result = 31 * result + floorScaleZ.hashCode()
        result = 31 * result + ambience.hashCode()
        result = 31 * result + diffusion.hashCode()
        result = 31 * result + team.hashCode()
        result = 31 * result + lendId.hashCode()
        result = 31 * result + lendTemplateId.hashCode()
        result = 31 * result + maleWield.hashCode()
        result = 31 * result + femaleWield.hashCode()
        result = 31 * result + primaryCursor.hashCode()
        result = 31 * result + secondaryCursor.hashCode()
        result = 31 * result + primaryInterfaceCursor.hashCode()
        result = 31 * result + secondaryInterfaceCursor.hashCode()
        result = 31 * result + campaigns.hashCode()
        result = 31 * result + pickSizeShift.hashCode()
        result = 31 * result + singleNoteId.hashCode()
        result = 31 * result + singleNoteTemplateId.hashCode()
        result = 31 * result + stringId.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }

    override fun toString(): String {
        return "ItemTypeDecoder(id=$id, modelId=$modelId, name=$name, spriteScale=$spriteScale, spritePitch=$spritePitch, spriteCameraRoll=$spriteCameraRoll, spriteTranslateX=$spriteTranslateX, spriteTranslateY=$spriteTranslateY, stackable=$stackable, cost=$cost, members=$members, multiStackSize=$multiStackSize, primaryMaleModel=$primaryMaleModel, secondaryMaleModel=$secondaryMaleModel, primaryFemaleModel=$primaryFemaleModel, secondaryFemaleModel=$secondaryFemaleModel, floorOptions=$floorOptions, options=$options, colours=$colours, textureColours=$textureColours, recolourPalette=$recolourPalette, exchangeable=$exchangeable, tertiaryMaleModel=$tertiaryMaleModel, tertiaryFemaleModel=$tertiaryFemaleModel, primaryMaleDialogueHead=$primaryMaleDialogueHead, primaryFemaleDialogueHead=$primaryFemaleDialogueHead, secondaryMaleDialogueHead=$secondaryMaleDialogueHead, secondaryFemaleDialogueHead=$secondaryFemaleDialogueHead, spriteCameraYaw=$spriteCameraYaw, dummyItem=$dummyItem, noteId=$noteId, notedTemplateId=$notedTemplateId, stack=$stack, floorScaleX=$floorScaleX, floorScaleY=$floorScaleY, floorScaleZ=$floorScaleZ, ambience=$ambience, diffusion=$diffusion, team=$team, lendId=$lendId, lendTemplateId=$lendTemplateId, maleWield=$maleWield, femaleWield=$femaleWield, primaryCursor=$primaryCursor, secondaryCursor=$secondaryCursor, primaryInterfaceCursor=$primaryInterfaceCursor, secondaryInterfaceCursor=$secondaryInterfaceCursor, campaigns=$campaigns, pickSizeShift=$pickSizeShift, singleNoteId=$singleNoteId, singleNoteTemplateId=$singleNoteTemplateId, stringId=$stringId, parameters=$parameters)"
    }


}