package world.gregs.voidps.tools.convert

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Config.INVENTORIES
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.encoder.InventoryEncoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.tools.property
import world.gregs.yaml.Yaml

/**
 * Converts inventories from one cache into another, dumping the default values into inventories.yml
 */
object InventoryConverter {
    @Suppress("USELESS_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val cacheModule = module {
            single { CacheDelegate("${System.getProperty("user.home")}/Downloads/rs634_cache/") as Cache }
        }
        val cache718Module = module {
            single { CacheDelegate("${System.getProperty("user.home")}/Downloads/rs718_cache/") as Cache }
        }
        val koin = startKoin {
        }.koin
        koin.loadModules(listOf(cache718Module))
        var decoder = InventoryDecoder().loadCache(get<Cache>())

        val inventories = decoder.indices.associateWith { decoder.getOrNull(it) }

        koin.unloadModules(listOf(cache718Module))
        koin.loadModules(listOf(cacheModule))
        val encoder = InventoryEncoder()
        val cache: Cache = get()

        val yaml = Yaml()
        val data: MutableMap<String, Any> = yaml.load<Map<String, Any>>(property("inventoryDefinitionsPath")).toMutableMap()


        val itemDecoder = ItemDefinitions(ItemDecoder().loadCache(cache)).load(Yaml(), property("itemDefinitionsPath"))
        decoder = InventoryDecoder().loadCache(cache)
        var counter = 0
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i)
            val cont = inventories[i]
            if (def == null || cont == null) {
                continue
            }

            if (def.length != cont.length) {
//                println("Length changed $i ${def.length} ${cont.length} ${cont.ids?.mapIndexed { index, it -> "${itemDecoder.getOrNull(it)?.name} ${cont.amounts!![index]}" }?.joinToString(separator = ", ")}")
            }

            if (cont.ids != null) {
                def.ids = cont.ids?.filter { itemDecoder.getOrNull(it) != null }?.toIntArray()
                def.amounts = cont.amounts!!.take(def.ids!!.size).toIntArray()
                def.length = def.ids!!.size
                counter++
                val writer = BufferWriter(4096)
                with(encoder) {
                    writer.encode(def)
                }
                cache.write(Index.CONFIGS, INVENTORIES, i, writer.toArray())

                var found: String? = null
                var int = false
                data.forEach { (key, value) ->
                    if (value is Int && value == i) {
                        found = key
                        int = true
                    } else if (value is Map<*, *> && value["id"] as Int == i) {
                        found = key
                    }
                }
                val list = mutableListOf<Map<String, Int>>()
                def.ids!!.forEachIndexed { index, id ->
                    list.add(mapOf(itemDecoder.get(id).stringId to def.amounts!![index]))
                }
                if (found != null) {
                    if (int) {
                        data[found!!] = mapOf("id" to i)
                    }
                    val map = (data[found] as Map<String, Any>).toMutableMap()
                    map["defaults"] = list
                    data[found!!] = map
                } else {
                    data["inventory_${i}"] = mapOf("id" to i, "defaults" to list)
                }
                println("$i ${cont.ids!!.mapIndexed { index, it -> "${itemDecoder.getOrNull(it)?.name} ${cont.amounts!![index]}" }.joinToString(separator = ", ")}")
            }
        }
        cache.update()
        println("Shops: $counter")
//        yaml.save("inventories.yml", data)
    }
}