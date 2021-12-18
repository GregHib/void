package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object GraphicDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = GraphicDecoder(koin.get())
        println(decoder.get(212))
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.modelId == 3139) {
                println(def)
            }
        }
    }
}