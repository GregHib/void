package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.ITEMS
import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.cache.definition.data.ItemDefinition

class ItemDecoder(
    private val parameters: Parameters = Parameters.EMPTY
) : DefinitionDecoder<ItemDefinition>(ITEMS) {

    override fun create(size: Int) = Array(size) { ItemDefinition(it) }

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun load(cache: Cache): Array<ItemDefinition> {
        val definitions = super.load(cache)
        var index = 0
        for (def in definitions) {
            if (def.equipIndex > 0) {
                def.equipIndex = index++
            }
        }
        return definitions
    }

    override fun ItemDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1, 4, 5, 6, 7, 8, 18, 24, 26, 78, 79, 90, 91, 92, 93, 95, 110, 111, 112, 139, 140 -> buffer.skip(2)
            2 -> name = buffer.readString()
            11 -> stackable = 1
            12 -> cost = buffer.readInt()
            16 -> members = true
            23 -> {
                val primaryMaleModel = buffer.readUnsignedShort()
                if (primaryMaleModel >= 0) {
                    equipIndex = 1
                }
            }
            25 -> {
                val primaryFemaleModel = buffer.readUnsignedShort()
                if (primaryFemaleModel >= 0) {
                    equipIndex = 2
                }
            }
            in 30..34 -> floorOptions[opcode - 30] = buffer.readString()
            in 35..39 -> options[opcode - 35] = buffer.readString()
            40, 41 -> buffer.skip(buffer.readUnsignedByte() * 4)
            42 -> buffer.skip(buffer.readUnsignedByte())
            65 -> return
            96 -> dummyItem = buffer.readUnsignedByte()
            97 -> noteId = buffer.readShort()
            98 -> notedTemplateId = buffer.readShort()
            in 100..109 -> buffer.skip(4)
            113, 114, 115, 134 -> buffer.skip(1)
            121 -> lendId = buffer.readShort()
            122 -> lendTemplateId = buffer.readShort()
            125, 126, 127, 128, 129, 130 -> buffer.skip(3)
            132 -> buffer.skip(buffer.readUnsignedByte() * 2)
            249 -> readParameters(buffer, parameters)
        }
    }

    override fun changeValues(definitions: Array<ItemDefinition>, definition: ItemDefinition) {
        if (definition.notedTemplateId != -1) {
            definition.toNote(definitions.getOrNull(definition.notedTemplateId), definitions.getOrNull(definition.noteId))
        }
        if (definition.lendTemplateId != -1) {
            definition.toLend(definitions.getOrNull(definition.lendId), definitions.getOrNull(definition.lendTemplateId))
        }
    }
}