package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin

        val decoder = NPCDecoder(koin.get(), false)
        println(decoder.last)
        (0 until decoder.last).mapNotNull { decoder.getOrNull(it) }.groupBy { it.walkMask }.toSortedMap().forEach { t, u ->
            println("$t ${u.take(15).map { "${it.id} ${it.name}" }}")
        }
        for (i in 0 until decoder.last) {
            val def = decoder.getOrNull(i) ?: continue
//            println("$i ${def.name} ${def.walkMask}")
            if (def.name.contains("ghast", true)) {
                println("$i ${def.name} ${def.walkMask}")
            }
        }
    }
}