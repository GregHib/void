package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin

        val list = mutableListOf<NPCDefinition>()
        val decoder = NPCDecoder(koin.get(), false)
        println(decoder.size)
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.name.startsWith("giant spider", true)) {
                println("$i ${def.name} ${def.modelIds?.toList()} ${def.options.toList()} ${def.modifiedColours?.toList()}")
                list.add(def)
            }
        }
        val map = list.groupBy { it.modelIds?.toList() }.mapValues { it.value.groupBy({ def -> def.modifiedColours?.toList() }, { def -> def.id }).toMap() }
        println(map)
    }
}