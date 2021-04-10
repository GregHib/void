package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object InterfaceDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = InterfaceDecoder(koin.get())
        for (i in listOf(620)) {
            val def = decoder.getOrNull(i) ?: continue
            for ((id, comp) in def.components ?: continue) {
                println("${comp.id} $id ${comp}")
            }
        }
    }
}