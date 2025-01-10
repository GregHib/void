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
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val yaml = Yaml()
        val categories = CategoryDefinitions().load(yaml, property("definitions.categories"))
        val ammo = AmmoDefinitions().load(yaml, property("definitions.ammoGroups"))
        val parameters = ParameterDefinitions(categories, ammo).load(yaml, property("definitions.parameters"))
        val decoder = ItemDefinitions(ItemDecoder(parameters).load(cache)).load(yaml, property("definitions.items"))
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if(def.stringId.contains("anchor"))
//            if (def.get("category", "") != "")
//            if (/*def.get("category", "") == "throwable" &&*/ def.contains("secondary_use_level"))
                println("${def.stringId} ${def.extras}")
//            if (def.contains("ammo_group")) {
//                println("${def.stringId} ${def.extras}")
//            }
        }
    }
}