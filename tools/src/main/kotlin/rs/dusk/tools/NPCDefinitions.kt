package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = NPCDecoder(koin.get(), false)
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
            if(def.name.equals("Alfonse", true)) {
                println("Found $i $def")
            }
        }
    }
}