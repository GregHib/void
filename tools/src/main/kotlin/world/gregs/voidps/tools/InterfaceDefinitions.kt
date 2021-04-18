package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.ui.Interface

object InterfaceDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = InterfaceDecoder(koin.get())
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            for ((id, comp) in def.components ?: continue) {
                if(comp.containers != null) {
                    println("${Interface.getId(comp.id)} ${Interface.getComponentId(comp.id)} $id ${comp.containers?.toList()}")
                }
            }
        }
    }
}