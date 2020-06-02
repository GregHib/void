package rs.dusk.tools

import org.koin.core.Koin
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.cacheModule
import rs.dusk.cache.definition.data.NPCDefinition
import rs.dusk.cache.definition.decoder.NPCDecoder
import java.io.File

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }
        val decoder = NPCDecoder(false)
        var count = 0
        val map = mutableMapOf<Int, MutableList<NPCDefinition>>()
        for (i in 0 until decoder.size) {
            val def = decoder.get(i) ?: continue
            val list = map.getOrPut(def.walkMask.toInt()) { mutableListOf() }
            list.add(def)
        }
        map.keys.toSortedSet().forEach { id ->
            val list = map[id]!!
            println("--- $id - ${list.size} - normal: ${id and 0x2 != 0} swim: ${id and 0x4 != 0} fly: ${id and 0x8 != 0} ---")
            for(i in 0 until 20) {
                val def = list.getOrNull(i) ?: continue
                println("${def.name} ${def.id}")
            }
        }
    }
}