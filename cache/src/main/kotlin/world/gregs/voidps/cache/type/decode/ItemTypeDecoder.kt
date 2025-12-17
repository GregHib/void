package world.gregs.voidps.cache.type.decode

import world.gregs.voidps.cache.type.TypeDecoder
import world.gregs.voidps.cache.type.field.BooleanField
import world.gregs.voidps.cache.type.field.IntField
import world.gregs.voidps.cache.type.types.ItemType

class ItemTypeDecoder : TypeDecoder<ItemType>() {
    val id = int("id", ItemType.EMPTY.id, opcode = -1) // TODO where from?
    val skip2 = skip(1, 4, 5, 6, 7, 8, 18, 24, 26, 78, 79, 90, 91, 92, 93, 95, 110, 111, 112, 139, 140, amount = 2)
    val name = string("name", ItemType.EMPTY.name, opcode = 2)
    val stackable = fixed("stackable", ItemType.EMPTY.stackable, value = 1, field = IntField(), opcode = 11)
    val cost = int("cost", ItemType.EMPTY.cost, opcode = 12)
    val members = fixed("members", ItemType.EMPTY.members, value = true, field = BooleanField(), opcode = 16)
    // TODO equipIndex
    val floorOptions = stringArray("floor_options", ItemType.EMPTY.floorOptions, opcodes = 30..34)
    val options = stringArray("options", ItemType.EMPTY.options, opcodes = 35..39)
    init {
        skip(40, 41) { it.readUnsignedByte() * 4 }
        skip(42) { it.readUnsignedByte() }
    }
    val exchangeable = bool("exchangeable", ItemType.EMPTY.exchangeable, opcode = 65)
    val dummyItem = byte("dummy_item", ItemType.EMPTY.dummyItem, opcode = 96)
    val noteId = short("note_id", ItemType.EMPTY.noteId, opcode = 97)
    val notedTemplateId = short("note_template_id", ItemType.EMPTY.notedTemplateId, opcode = 98)
    init {
        skip(100, 101, 102, 103, 104, 105, 106, 107, 108, 109, amount = 4)
        skip(113, 114, 115, 134, amount = 1)
    }
    val lendId = short("lend_id", ItemType.EMPTY.lendId, opcode = 121)
    val lendTemplateId = short("lend_template_id", ItemType.EMPTY.lendTemplateId, opcode = 122)
    init {
        skip(125, 126, 127, 128, 129, 130, amount = 3)
        skip(132) { it.readUnsignedByte() * 2}
    }
    val equipIndex = int("equip_index", ItemType.EMPTY.equipIndex, opcode = 122)
    val stringId = string("[section]", ItemType.EMPTY.stringId, opcode = 122)
    val params = params(opcode = 249) {
        add("examine", 10_000)
    }

    override fun create(): ItemType {
        return ItemType(
            id = id.value,
            name = name.value,
            stackable = stackable.value,
            cost = cost.value,
            members = members.value,
            floorOptions = floorOptions.array,
            options = options.array,
            exchangeable = exchangeable.value,
            dummyItem = dummyItem.value,
            noteId = noteId.value,
            notedTemplateId = notedTemplateId.value,
            lendId = lendId.value,
            lendTemplateId = lendTemplateId.value,
            equipIndex = lendTemplateId.value,
            stringId = stringId.value,
            extras = params.value
        )
    }
}