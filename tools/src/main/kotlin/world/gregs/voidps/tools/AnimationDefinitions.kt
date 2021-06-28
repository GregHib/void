package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object AnimationDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = AnimationDecoder(koin.get())
        println(decoder.get(806))
        val frames = decoder.get(806).primaryFrames
        for (i in 0 until decoder.last) {
            val def = decoder.getOrNull(i) ?: continue
            if(def.primaryFrames.contentEquals(frames)) {
                println(def)
            }
        }
    }
}