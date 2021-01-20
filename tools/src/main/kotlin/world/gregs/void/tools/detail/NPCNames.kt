package world.gregs.void.tools.detail

import org.koin.core.context.startKoin
import world.gregs.void.cache.definition.decoder.NPCDecoder
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule
import world.gregs.void.engine.data.file.FileLoader
import world.gregs.void.engine.data.file.fileLoaderModule

/**
 * Dumps unique string identifiers for NPCs using formatted npc definition name plus index for duplicates
 */
private class NPCNames(val decoder: NPCDecoder) : NameDumper() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val koin = startKoin {
                fileProperties("/tool.properties")
                modules(cacheModule, cacheDefinitionModule, fileLoaderModule)
            }.koin
            val decoder = NPCDecoder(koin.get(), member = true)
            val loader: FileLoader = koin.get()
            val names = NPCNames(decoder)
            names.dump(loader, "./npc-details.yml", "npc", decoder.size)
        }
    }

    override fun createName(id: Int): String? {
        return decoder.getOrNull(id)?.name
    }

    override fun createData(id: Int): Map<String, Any> {
        return mutableMapOf("id" to id)
    }

}