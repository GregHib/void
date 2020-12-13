package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object ClientScriptDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin

        val cache: Cache = koin.get()
        val decoder = ClientScriptDecoder(koin.get())
        val validContexts = arrayOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 73, 76)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if(def.stringOperands?.contains("Lower level") == true) {
                println(def)
            }
        }
    }
}