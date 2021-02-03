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
        println(decoder.size)
        var maxId = 0
        var maxIndex = 0
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            var index = 0
            for ((id, comp) in def.components ?: continue) {
                if (id > maxId) {
                    println("${comp.id} $id ${comp.options?.toList()}")
                    maxId = id
                }
                if(index > maxIndex) {
                    maxIndex = index
                }
                index++
            }
        }
        println("Max component id $maxId index: $maxIndex")
    }
}