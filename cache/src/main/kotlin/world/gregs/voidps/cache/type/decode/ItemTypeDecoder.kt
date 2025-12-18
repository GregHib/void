package world.gregs.voidps.cache.type.decode

import world.gregs.voidps.cache.type.TypeDecoder
import world.gregs.voidps.cache.type.field.codec.ByteCodec
import world.gregs.voidps.cache.type.field.codec.ShortCodec
import world.gregs.voidps.cache.type.field.codec.UnsignedByteCodec
import world.gregs.voidps.cache.type.field.type.ItemStack
import world.gregs.voidps.cache.type.types.ItemType

class ItemTypeDecoder : TypeDecoder<ItemType>(size = 256) {
    override val id = int("id", default = -1, opcode = 250)
    val stringId = string("[section]", default = "", opcode = 251)
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

    override fun loaded(types: Array<ItemType?>) {
        var index = 0
        for (type in types) {
            type ?: continue
            if (type.equipIndex > 0) {
                type.equipIndex = index++
            }
            if (type.notedTemplateId != -1) {
                types[type.id] = type.toNote(types[type.notedTemplateId], types[type.noteId]) ?: continue
            }
            if (type.lendTemplateId != -1) {
                types[type.id] = type.toLend(types[type.lendId], types[type.lendTemplateId]) ?: continue
            }
        }
    }

    override fun create(): ItemType {
        val equipIndex = if (primaryFemaleModel.value >= 0) 2 else if (primaryMaleModel.value >= 0) 1 else -1
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
            equipIndex = equipIndex,
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
        parameters.value = type.params?.toMutableMap()
    }
}