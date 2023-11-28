package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.yaml.Yaml

object ItemDefinitions {

    fun test(def: ItemDefinition) = (def.stringId.endsWith("halberd"))

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val yaml = Yaml()
        val categories = CategoryDefinitions().load(yaml, property("categoryDefinitionsPath"))
        val parameters = ParameterDefinitions(categories).load(yaml, property("parameterDefinitionsPath"))
        val decoder = ItemDefinitions(ItemDecoder(parameters).loadCache(cache)).load(yaml, property("itemDefinitionsPath"))
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.extras != null) {
                println(def)
            }
        }
    }
}