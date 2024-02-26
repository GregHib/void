package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.voidps.engine.event.CharTrieWildcardSearch
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.yaml.Yaml
import kotlin.system.measureTimeMillis

object ItemDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val yaml = Yaml()
        val categories = CategoryDefinitions().load(yaml, property("categoryDefinitionsPath"))
        val ammo = AmmoDefinitions().load(yaml, property("ammoDefinitionsPath"))
        val parameters = ParameterDefinitions(categories, ammo).load(yaml, property("parameterDefinitionsPath"))
        val decoder = ItemDefinitions(ItemDecoder(parameters).load(cache)).load(yaml, property("itemDefinitionsPath"))

        val root = CharTrieWildcardSearch()
        for (key in decoder.ids.keys) {
//            if (!key.endsWith("noted")) {
                root.insert(key)
//            }
        }
        println(decoder.ids.size)
        val wildcards = listOf(
            "*_tiara", "castle_wars_brace*", "void_*", "elite_void_*", "ahrims_*", "dharoks_*", "guthans_*",
            "karils_*",
            "torags_*",
            "veracs_*",
            "avas_*")
        println("Trie: ${
            measureTimeMillis {
                for (wildcard in wildcards) {
                    println(root.search(wildcard))
                }
            }
        }")
        println("List: ${
            measureTimeMillis {
                for (wildcard in wildcards) {
                    println(decoder.ids.keys.filter { wildcardEquals(wildcard, it) })
                }
            }
        }")
//        for (i in decoder.definitions.indices) {
//            val def = decoder.getOrNull(i) ?: continue
//            if (def.contains("extra_equipment_option")) {
//                println(def)
//            }
//        }
    }
}