package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.InterfaceDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object InterfaceDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = InterfaceDecoder(koin.get())
        for (i in 334..334/*0 until decoder.size*/) {
            val def = decoder.getOrNull(i) ?: continue
            for ((id, component) in def.components ?: continue) {
                println("$i $id ${component.containers?.toList()}")
            }
        }
    }
}