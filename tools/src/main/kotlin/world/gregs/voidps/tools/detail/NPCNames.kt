package world.gregs.voidps.tools.detail

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.yaml.Yaml

/**
 * Dumps unique string identifiers for NPCs using formatted npc definition name plus index for duplicates
 */
private class NPCNames(val decoder: NPCDecoder) : NameDumper() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val koin = startKoin {
                fileProperties("/tool.properties")
                modules(cacheModule, cacheDefinitionModule, module {
                    single { Yaml() }
                })
            }.koin
            val decoder = NPCDecoder(koin.get(), member = true)
            val yaml: Yaml = koin.get()
            val names = NPCNames(decoder)
            names.dump(yaml, "./npc-details.yml", "npc", decoder.last)
        }
    }

    override fun createName(id: Int): String? {
        return decoder.getOrNull(id)?.name
    }

    override fun createData(id: Int): Map<String, Any> {
        return mutableMapOf("id" to id)
    }

}