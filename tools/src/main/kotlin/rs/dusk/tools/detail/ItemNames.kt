package rs.dusk.tools.detail

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.data.file.fileLoaderModule

/**
 * Dumps unique string identifiers for items using formatted item definition name plus index for duplicates
 */
private object ItemNames : NameDumper() {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, fileLoaderModule)
        }.koin
        val decoder = ItemDecoder(koin.get())
        val loader: FileLoader = koin.get()
        dump(loader, "./item-details.yml", "item", decoder.size) { id -> decoder.get(id)?.name }
    }

}