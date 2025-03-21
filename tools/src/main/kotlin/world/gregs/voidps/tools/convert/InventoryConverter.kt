package world.gregs.voidps.tools.convert

import world.gregs.config.Config
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Config.INVENTORIES
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.encoder.InventoryEncoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.tools.property
import world.gregs.yaml.Yaml
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Converts inventories from one cache into another, dumping the default values into inventories.toml
 */
@Suppress("UNCHECKED_CAST")
object InventoryConverter {

    fun convert(target: File, provider: File) {
        val targetCache = CacheDelegate(target.path)
        val otherCache = CacheDelegate(provider.path)

        val otherDecoder = InventoryDecoder().load(otherCache)
        val targetDecoder = InventoryDecoder().load(targetCache)
        val itemDefinitions = ItemDefinitions(ItemDecoder().load(targetCache)).load(property("definitions.items"))
        val encoder = InventoryEncoder()
        val data: MutableMap<String, Any> = mutableMapOf()

        Config.fileReader(property("definitions.inventories")) {
            while (nextSection()) {
                val section = section()
                val map = mutableMapOf<String, Any>()
                while (nextPair()) {
                    val key = key()
                    val value = value()
                    map[key] = value
                }
                data[section] = map
            }
        }

        var counter = 0
        for (index in targetDecoder.indices) {
            val otherDef = otherDecoder.getOrNull(index)
            val targetDef = targetDecoder.getOrNull(index)
            if (targetDef == null || otherDef == null) {
                continue
            }

            if (otherDef.ids != null) {
                targetDef.ids = otherDef.ids?.filter { itemDefinitions.getOrNull(it) != null }?.toIntArray()
                targetDef.amounts = otherDef.amounts!!.take(targetDef.ids!!.size).toIntArray()
                counter++
                val writer = BufferWriter(4096)
                with(encoder) {
                    writer.encode(targetDef)
                }
                targetCache.write(Index.CONFIGS, INVENTORIES, index, writer.toArray())

                var found: String? = null
                var int = false
                data.forEach { (key, value) ->
                    if (value is Int && value == index) {
                        found = key
                        int = true
                    } else if (value is Map<*, *> && value["id"] as Int == index) {
                        found = key
                    }
                }
                val list = mutableListOf<Map<String, Int>>()
                targetDef.ids!!.forEachIndexed { i, id ->
                    list.add(mapOf(itemDefinitions.get(id).stringId to targetDef.amounts!![i]))
                }
                if (found != null) {
                    if (int) {
                        data[found!!] = mapOf("id" to index)
                    }
                    val map = (data[found] as Map<String, Any>).toMutableMap()
                    map["defaults"] = list
                    data[found!!] = map
                } else {
                    data["inventory_${index}"] = mapOf("id" to index, "defaults" to list)
                }
//                println("$index ${otherDef.ids!!.mapIndexed { index, it -> "${itemDefinitions.getOrNull(it)?.name} ${otherDef.amounts!![index]}" }.joinToString(separator = ", ")}")
            }
        }
        if (targetCache.update()) {
            println("Updated $counter inventories.")
        }
//        yaml.save("inventories.toml", data)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val target = File("${System.getProperty("user.home")}/Downloads/rs634_cache/")
        val other = File("${System.getProperty("user.home")}/Downloads/rs718_cache/")
        convert(target, other)
    }
}