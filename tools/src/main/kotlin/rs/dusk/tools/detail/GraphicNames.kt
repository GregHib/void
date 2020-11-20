package rs.dusk.tools.detail

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.definition.decoder.GraphicDecoder
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.entity.definition.DefinitionsDecoder.Companion.toIdentifier

/**
 * Dumps unique string identifiers for graphic ids
 * Identifies graphic names by cross referencing graphic models with npc/object/item names & their models
 */
object GraphicNames {

    private data class Ids(val id: Int)

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, fileLoaderModule)
        }.koin
        val cache: Cache = koin.get()
        val models = mutableMapOf<Int, MutableList<String>>()
        addItemModels(cache, models)
        addNPCModels(cache, models)
        addObjectModels(cache, models)
        val loader: FileLoader = koin.get()
        val decoder = GraphicDecoder(cache)
        val map = mutableMapOf<Int, MutableList<String>>()
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            if(def.modelId != 0) {
                val name = models[def.modelId]?.firstOrNull() ?: return@repeat
                map.getOrPut(id) { mutableListOf() }.add(toIdentifier(name))
            }
        }

        val path = "./graphic-details.yml"
        val sorted = map.map { it.value.first() to Ids(it.key) }.sortedBy { it.second.id }.toMap()
        loader.save(path, sorted)
        println("${sorted.size} graphic identifiers dumped to $path.")
    }

    private fun addItemModels(cache: Cache, models: MutableMap<Int, MutableList<String>>) {
        val decoder = ItemDecoder(cache)
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            models.add(def.primaryMaleModel, def.name)
            models.add(def.secondaryMaleModel, def.name)
            models.add(def.tertiaryMaleModel, def.name)
            models.add(def.primaryFemaleModel, def.name)
            models.add(def.secondaryFemaleModel, def.name)
            models.add(def.tertiaryFemaleModel, def.name)
            models.add(def.modelId, def.name)
        }
    }

    private fun addNPCModels(cache: Cache, models: MutableMap<Int, MutableList<String>>) {
        val decoder = NPCDecoder(cache, member = true)
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            def.modelIds?.forEach { model ->
                models.add(model, def.name)
            }
        }
    }

    private fun addObjectModels(cache: Cache, models: MutableMap<Int, MutableList<String>>) {
        val decoder = ObjectDecoder(cache, member = true, lowDetail = false, configReplace = false)
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            def.modelIds?.forEach { array ->
                array.forEach { model ->
                    models.add(model, def.name)
                }
            }
        }
    }

    private fun MutableMap<Int, MutableList<String>>.add(id: Int, name: String) {
        if(id != -1 && name != "" && name != "null") {
            getOrPut(id) { mutableListOf() }.add(name)
        }
    }

}