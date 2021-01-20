package world.gregs.void.tools

import org.koin.core.context.startKoin
import world.gregs.void.cache.config.decoder.ContainerDecoder
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule

object ContainerDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            koin.setProperty("cachePath", "${System.getProperty("user.home")}\\Downloads\\rs718_cache\\")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ContainerDecoder(koin.get())
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
            println(def)
        }
    }
}