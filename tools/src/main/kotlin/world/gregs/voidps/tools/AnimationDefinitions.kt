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
        println(decoder.get(9807))
        val frames = decoder.get(9807).primaryFrames?.take(5)!!
        loop@for (i in 0 until decoder.last) {
            val def = decoder.getOrNull(i) ?: continue
            for (x in 0 until frames.size) {
                if (def.primaryFrames?.get(x) != frames[x]) {
                    continue@loop
                }
            }
            println(def)
        }
    }
}