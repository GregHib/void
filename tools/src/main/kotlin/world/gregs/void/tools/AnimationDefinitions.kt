package world.gregs.void.tools

import org.koin.core.context.startKoin
import world.gregs.void.cache.definition.decoder.AnimationDecoder
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule

object AnimationDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = AnimationDecoder(koin.get())
        var count = 0
        println(decoder.get(877))
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
        }
        println(count)
    }
}