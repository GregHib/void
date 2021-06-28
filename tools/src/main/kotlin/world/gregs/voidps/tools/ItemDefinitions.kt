package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object ItemDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())
        println(decoder.last)
        val map = mutableMapOf<Int, MutableList<ItemDefinition>>()
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.params?.containsKey(686) == true) {
                map.getOrPut(def.params!![686] as Int) { mutableListOf() }.add(def)
            }
        }

        for ((key, list) in map.toSortedMap()) {
            println("$key - ${list.map { it.name }}")
        }
    }
}