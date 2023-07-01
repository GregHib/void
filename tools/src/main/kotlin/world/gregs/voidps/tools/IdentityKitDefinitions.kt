package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.cache.config.decoder.IdentityKitDecoder
import world.gregs.voidps.engine.client.cacheModule

object IdentityKitDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, module {
                single { IdentityKitDecoder(get()) }
            })
        }.koin
        val decoder = IdentityKitDecoder(koin.get())
        println(decoder.last)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println("$i $def")
        }
    }
}