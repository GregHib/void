package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ItemDefinitions

object ItemDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val files = configFiles()
        val decoder = ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.stringId.contains("rune_plate")) {
                //            if (def.get("category", "") != "")
//            if (/*def.get("category", "") == "throwable" &&*/ def.contains("secondary_use_level"))
//                println("${def.stringId} ${def.params}")
                println(def)
            }
//            if (def.contains("ammo_group")) {
//                println("${def.stringId} ${def.params?.get(Params.AMMO_GROUP)}")
//            }
        }
    }
}
