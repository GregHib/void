package rs.dusk.tools.detail

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType

/**
 * Dumps unique string identifiers for items using formatted item definition name plus index for duplicates
 */
private class ItemNames(val decoder: ItemDecoder, val types: ItemTypes) : NameDumper() {

    override fun createName(id: Int): String? {
        val decoder = decoder.get(id) ?: return "null"
        val builder = StringBuilder()
        builder.append(decoder.name)
        when {
            decoder.notedTemplateId != -1 -> builder.append("_noted")
            decoder.lendTemplateId != -1 -> builder.append("_lent")
            decoder.singleNoteTemplateId != -1 -> builder.append("_note")
        }
        return builder.toString()
    }

    override fun createData(id: Int): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["id"] = id
        val def = decoder.getSafe(id)
        if (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0) {
            val slot = types.slots[id]
            if (slot != null) {
                var s = EquipSlot.by(slot)
                if(id == 11277) {
                    s = EquipSlot.Hat
                }
                if (s == EquipSlot.None) {
                    println("Unknown slot $slot $id")
                } else {
                    map["slot"] = s.name
                }
            }
            val type = types.getEquipType(id)
            if (type != EquipType.None) {
                map["type"] = type.name
            }
        }
        return map
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val koin = startKoin {
                fileProperties("/tool.properties")
                modules(cacheModule, cacheDefinitionModule, fileLoaderModule)
            }.koin
            val decoder = ItemDecoder(koin.get())
            val loader: FileLoader = koin.get()
            val types = ItemTypes(decoder)
            val names = ItemNames(decoder, types)
            names.dump(loader, "./item-details.yml", "item", decoder.size)
        }
    }

}