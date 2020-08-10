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
        dump(loader, "./item-details.yml", "item", decoder.size) { id ->
            val decoder = decoder.get(id) ?: return@dump "null"
            val builder = StringBuilder()
            builder.append(decoder.name)
            when {
                decoder.notedTemplateId != -1 -> builder.append("_noted")
                decoder.lendTemplateId != -1 -> builder.append("_lent")
                decoder.singleNoteTemplateId != -1 -> builder.append("_note")
            }
            builder.toString()
        }
    }

}