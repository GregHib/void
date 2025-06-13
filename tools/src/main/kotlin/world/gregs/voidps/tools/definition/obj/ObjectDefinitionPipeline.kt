package world.gregs.voidps.tools.definition.obj

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import world.gregs.voidps.tools.definition.item.ItemDefinitionPipeline
import world.gregs.voidps.tools.definition.item.pipe.page.PageCollector
import world.gregs.voidps.tools.definition.item.pipe.page.UniqueIdentifiers
import world.gregs.voidps.tools.definition.obj.pipe.*
import world.gregs.yaml.Yaml
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Dumps unique string identifiers for objects using formatted object definition name plus index for duplicates
 */
private object ObjectDefinitionPipeline {

    private fun buildObjectExtras(
        decoder: Array<ObjectDefinitionFull>,
        pages: Map<Int, PageCollector>,
    ): MutableMap<Int, Extras> {
        val output = mutableMapOf<Int, Extras>()
        for (id in decoder.indices) {
            val def = decoder.getOrNull(id) ?: continue
            val page = pages[def.id]
            if (page != null) {
                val uid = page.uid
                if (uid.isNotEmpty()) {
                    output[id] = page to mutableMapOf("id" to id)
                }
            }
        }
        val postProcess = Pipeline<MutableMap<Int, Extras>>().apply {
            add(ObjectDoorsGates(decoder))
            add(ObjectTrapdoors(decoder))
            add(ObjectManualChanges())
            add(ObjectManualTreeChanges())
            add(RemoveNullEmptyExtras())
            add(UniqueIdentifiers())
        }
        return postProcess.modify(output)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val start = System.currentTimeMillis()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = ObjectDecoderFull(members = true, lowDetail = false).load(cache)
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
        val yaml = Yaml()
        val file = File("objects.toml")
        yaml.save(file.path, map)
        val contents = "# Don't edit; apply changes to the ObjectDefinitionPipeline tool's ObjectManualChanges class instead.\n${file.readText()}"
        file.writeText(contents)
        println("${output.size} object definitions written to ${file.path} in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }
}
