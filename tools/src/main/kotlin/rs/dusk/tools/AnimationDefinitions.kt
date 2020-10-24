package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.AnimationDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

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