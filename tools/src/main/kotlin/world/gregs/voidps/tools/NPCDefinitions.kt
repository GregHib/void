package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.client.cacheModule

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule)
        }.koin

        val decoder = NPCDecoder(koin.get(), true)
        println(decoder.last)
        for (i in 0 until decoder.last) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.name.contains("sir prysin", true)) {
                println("$i ${def.name} ${def.walkMask}")
            }
        }
    }
}