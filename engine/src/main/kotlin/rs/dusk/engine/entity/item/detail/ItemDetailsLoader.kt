package rs.dusk.engine.entity.item.detail

import com.google.common.collect.HashBiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

class ItemDetailsLoader(private val loader: FileLoader) : TimedLoader<ItemDetails>("item detail") {

    override fun load(args: Array<out Any?>): ItemDetails {
        val path = args[0] as String
        val data: Map<String, LinkedHashMap<String, Any>> = loader.load(path)
        val map: Map<String, ItemDetail> = data.mapValues { convert(it.value) }
        val items = map.map { it.value.id to it.value }.toMap()
        val names = map.map { it.value.id to it.key }.toMap()
        count = names.size
        return ItemDetails(items, HashBiMap.create(names))
    }

    fun convert(map: Map<String, Any>): ItemDetail {
        val id: Int by map
        return ItemDetail(id)
    }
}