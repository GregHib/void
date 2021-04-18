package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object ContainerDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            koin.setProperty("cachePath", "${System.getProperty("user.home")}\\Downloads\\rs718_cache\\")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ContainerDecoder(koin.get())
        println(decoder.size)
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
            println(def)
        }
    }
}