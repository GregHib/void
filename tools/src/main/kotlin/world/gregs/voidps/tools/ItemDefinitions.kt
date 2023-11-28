package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.yaml.Yaml

object ItemDefinitions {

    fun test(def: ItemDefinition) = (def.stringId.endsWith("halberd"))

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = ItemDefinitions(ItemDecoder().loadCache(cache)).load(Yaml(), "./data/definitions/items.yml")
        val groups = mutableMapOf<Int, MutableList<ItemDefinition>>()
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.has(key = "2195")) {
                val cat: Int = def["2195"]
                if(cat == 1 || cat == 2 || cat == 3) {
                    println(def)
                }
            }
            if (def.has(key = "21")) {
                val cat: Int = def["21"]
                groups.getOrPut(cat) { mutableListOf() }.add(def)
            }
        }
        for((value, list) in groups) {
            println("$value - ${list.map { it.name }}")
        }
    }
}