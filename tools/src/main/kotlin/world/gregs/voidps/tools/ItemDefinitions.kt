package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.voidps.engine.data.configFiles

object ItemDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val categories = CategoryDefinitions().load(Settings["definitions.categories"])
        val ammo = AmmoDefinitions().load(Settings["definitions.ammoGroups"])
        val parameters = ParameterDefinitions(categories, ammo).load(Settings["definitions.parameters"])
        val files = configFiles()
        val decoder = ItemDefinitions(ItemDecoder(parameters).load(cache)).load(files.getOrDefault(Settings["definitions.items"], emptyList())
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