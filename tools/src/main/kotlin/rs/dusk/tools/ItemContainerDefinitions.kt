package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object ItemContainerDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            koin.setProperty("cachePath", "${System.getProperty("user.home")}\\Downloads\\rs718_cache\\")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemContainerDecoder(koin.get())
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
            println(def)
        }
    }
}