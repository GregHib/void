package rs.dusk.tools.definition.obj

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.definition.DefinitionsDecoder.Companion.toIdentifier
import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras
import rs.dusk.tools.definition.item.ItemDefinitionPipeline
import rs.dusk.tools.definition.item.pipe.page.PageCollector
import rs.dusk.tools.definition.item.pipe.page.UniqueIdentifiers
import rs.dusk.tools.definition.obj.pipe.ObjectManualChanges
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Dumps unique string identifiers for objects using formatted object definition name plus index for duplicates
 */
private object ObjectDefinitionPipeline {

    private fun buildObjectExtras(
        decoder: ObjectDecoder,
        pages: Map<Int, PageCollector>
    ): MutableMap<Int, Extras> {
        val output = mutableMapOf<Int, Extras>()
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            val page = pages[def.id]
            if (page != null) {
                val uid = page.uid
                if (uid.isNotEmpty() && !uid.startsWith("null", true)) {
                    output[id] = page to mutableMapOf<String, Any>("id" to id)
                }
            }
        }
        val postProcess = Pipeline<MutableMap<Int, Extras>>().apply {
            add(UniqueIdentifiers())
            add(ObjectManualChanges())
        }
        return postProcess.modify(output)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val start = System.currentTimeMillis()
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ObjectDecoder(koin.get(), member = true, lowDetail = false, configReplace = false)
        val pages = decoder.indices.mapNotNull {
            val def = decoder.getOrNull(it)
            if (def != null) {
                it to PageCollector(it, def.name, uid = toIdentifier(def.name))
            } else {
                null
            }
        }.toMap()
        val output = buildObjectExtras(decoder, pages)
        val map = ItemDefinitionPipeline.convertToYaml(output)
        val loader = FileLoader(true)
        val file = File("object-definition-extras.yml")
        loader.save(file, map)
        val contents = "# Don't edit; apply changes to the ObjectDefinitionPipeline tool's ObjectManualChanges class instead.\n${file.readText()}"
        file.writeText(contents)
        println("${output.size} object definitions written to ${file.path} in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }

}