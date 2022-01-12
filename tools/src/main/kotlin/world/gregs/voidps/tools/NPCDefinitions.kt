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
        for (i in 0 until decoder.last) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.name.contains("barricade", true)) {
                println("$i ${def.name} ${def.options.toList()}")
            }
        }
    }
}