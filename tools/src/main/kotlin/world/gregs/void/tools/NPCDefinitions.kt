package world.gregs.void.tools

import org.koin.core.context.startKoin
import world.gregs.void.cache.definition.data.NPCDefinition
import world.gregs.void.cache.definition.decoder.NPCDecoder
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin

        val list = mutableListOf<NPCDefinition>()
        val decoder = NPCDecoder(koin.get(), false)
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.name.startsWith("saradomin owl", true)) {
                println("$i ${def.name} ${def.modelIds?.toList()} ${def.options.toList()} ${def.modifiedColours?.toList()}")
                list.add(def)
            }
        }
        val map = list.groupBy { it.modelIds?.toList() }.mapValues { it.value.groupBy({ def -> def.modifiedColours?.toList() }, { def -> def.id }).toMap() }
        println(map)
    }
}