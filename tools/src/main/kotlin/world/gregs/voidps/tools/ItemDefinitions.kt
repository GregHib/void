package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.yaml.Yaml

object ItemDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val yaml = Yaml()
        val categories = CategoryDefinitions().load(yaml, property("categoryDefinitionsPath"))
        val ammo = AmmoDefinitions().load(yaml, property("ammoDefinitionsPath"))
        val parameters = ParameterDefinitions(categories, ammo).load(yaml, property("parameterDefinitionsPath"))
        val decoder = ItemDefinitions(ItemDecoder(parameters).load(cache)).load(yaml, property("itemDefinitionsPath"))
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
//            if(def.stringId.contains("toktz_xil_ul"))
            if (/*def.get("category", "") == "throwable" &&*/ def.contains("secondary_use_level")) {
                println("${def.stringId} ${def.extras}")
            }
//            if (def.contains("ammo_group")) {
//                println("${def.stringId} ${def.extras}")
//            }
        }
    }
}