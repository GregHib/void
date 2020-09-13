package rs.dusk.tools.detail

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.data.file.fileLoaderModule

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