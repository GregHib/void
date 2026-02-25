package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions

object EnumDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val files = configFiles()
        ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))
        val definitions = EnumDefinitions.init(EnumDecoder().load(cache)).load(files.list(Settings["definitions.enums"]))
        for (i in definitions.definitions.indices) {
            val def = definitions.getOrNull(i) ?: continue
            println("$i $def")
        }
    }
}
