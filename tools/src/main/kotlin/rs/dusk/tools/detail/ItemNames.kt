package rs.dusk.tools.detail

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import java.io.DataInputStream
import java.io.File

/**
 * Dumps unique string identifiers for items using formatted item definition name plus index for duplicates
 */
private class ItemNames(val decoder: ItemDecoder) : NameDumper() {

    val equipSlots = readEquipSlots()
    val equipTypes = readEquipTypes()

    fun readEquipSlots(): Map<Int, Int> {
        val equipSlots = mutableMapOf<Int, Int>()
        val file = File("./equipmentSlots.dat")
        val stream = DataInputStream(file.inputStream())
        while(stream.available() > 0) {
            equipSlots[stream.readShort().toInt()] = stream.readByte().toInt()
        }
        return equipSlots
    }

    fun readEquipTypes(): Map<Int, Int> {
        val equipTypes = mutableMapOf<Int, Int>()
        val file = File("./equipmentTypes.dat")
        val stream = DataInputStream(file.inputStream())
        while(stream.available() > 0) {
            equipTypes[stream.readShort().toInt()] = stream.readByte().toInt()
        }
        return equipTypes
    }

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
        val slot = equipSlots[id]
        if(slot != null) {
            val s = EquipSlot.by(slot)
            if(s == null) {
                println("Unknown slot $slot $id")
            } else {
                map["slot"] = s.name
            }
        }
        val type = equipTypes[id]
        if(type != null) {
            val t = EquipType.by(type)
            if(t == null) {
                println("Unknown type $type $id")
            } else {
                map["type"] = t.name
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
            val names = ItemNames(decoder)
            names.dump(loader, "./item-details.yml", "item", decoder.size)

        }
    }

}