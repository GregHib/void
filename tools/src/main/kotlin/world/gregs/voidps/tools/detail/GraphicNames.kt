package world.gregs.voidps.tools.detail

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.cache.definition.decoder.NPCDecoderFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.yaml.Yaml

/**
 * Dumps unique string identifiers for graphic ids
 * Identifies graphic names by cross-referencing graphic models with npc/object/item names & their models
 */
object GraphicNames {

    private data class Ids(val id: Int)

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val models = mutableMapOf<Int, MutableList<String>>()
        addItemModels(cache, models)
        addNPCModels(cache, models)
        addObjectModels(cache, models)
        val yaml = Yaml()
        val decoder = GraphicDecoder().load(cache)
        val map = mutableMapOf<Int, MutableList<String>>()
        for (id in decoder.indices) {
            val def = decoder.getOrNull(id) ?: continue
            if (def.modelId != 0) {
                val name = models[def.modelId]?.firstOrNull() ?: continue
                map.getOrPut(id) { mutableListOf() }.add(toIdentifier(name))
            }
        }

        val path = "./graphic-details.yml"
        val sorted = map.map { it.value.first() to Ids(it.key) }.sortedBy { it.second.id }.toMap()
        yaml.save(path, sorted)
        println("${sorted.size} graphic identifiers dumped to $path.")
    }

    private fun addItemModels(cache: Cache, models: MutableMap<Int, MutableList<String>>) {
        val decoder = ItemDecoderFull().load(cache)
        for (id in decoder.indices) {
            val def = decoder.getOrNull(id) ?: continue
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
        val decoder = NPCDecoderFull(members = true).load(cache)
        for (id in decoder.indices) {
            val def = decoder.getOrNull(id) ?: continue
            def.modelIds?.forEach { model ->
                models.add(model, def.name)
            }
        }
    }

    private fun addObjectModels(cache: Cache, models: MutableMap<Int, MutableList<String>>) {
        val decoder = ObjectDecoderFull(members = true, lowDetail = false).load(cache)
        for (id in decoder.indices) {
            val def = decoder.getOrNull(id) ?: continue
            def.modelIds?.forEach { array ->
                array.forEach { model ->
                    models.add(model, def.name)
                }
            }
        }
    }

    private fun MutableMap<Int, MutableList<String>>.add(id: Int, name: String) {
        if (id != -1 && name != "" && name != "null") {
            getOrPut(id) { mutableListOf() }.add(name)
        }
    }
}
