package rs.dusk.tools.detail

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.config.decoder.RenderAnimationDecoder
import rs.dusk.cache.definition.decoder.AnimationDecoder
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.entity.definition.DefinitionsDecoder.Companion.toIdentifier

/**
 * Dumps unique string identifiers for animation ids
 * Identifies animation names by cross referencing npc names with render animations
 */
object AnimationNames {

    private data class Ids(val id: Int)

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, fileLoaderModule)
        }.koin
        val cache: Cache = koin.get()
        val loader: FileLoader = koin.get()
        val decoder = AnimationDecoder(cache)
        val renders = getRenderAnimations(cache)
        val map = mutableMapOf<Int, MutableList<String>>()
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            val render = renders[id]
            if(render != null) {
                map.getOrPut(id) { mutableListOf() }.add(render)
            }
        }

        val path = "./animation-details.yml"
        val sorted = map.map { it.value.first() to Ids(it.key) }.sortedBy { it.second.id }.toMap()
        loader.save(path, sorted)
        println("${sorted.size} animation identifiers dumped to $path.")
    }

    private fun getRenderAnimations(cache: Cache): Map<Int, String> {
        val renders = getNPCRenderIds(cache)
        val decoder = RenderAnimationDecoder(cache)
        val map = mutableMapOf<Int, String>()
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            val name = toIdentifier(renders[id] ?: return@repeat)
            map.add(def.run, name, "_run")
            map.add(def.primaryIdle, name, "_idle")
            map.add(def.primaryWalk, name, "_walk")
            map.add(def.turning, name, "_turn")
            map.add(def.secondaryWalk, name, "_walk2")
            map.add(def.walkBackwards, name, "_walk_back")
            map.add(def.sideStepLeft, name, "_step_left")
            map.add(def.sideStepRight, name, "_step_right")
        }
        return map
    }

    private fun MutableMap<Int, String>.add(id: Int, name: String, suffix: String) {
        if(id != -1 && name != "" && name != "null") {
            set(id, "${name}${suffix}")
        }
    }

    private fun getNPCRenderIds(cache: Cache): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        val decoder = NPCDecoder(cache, member = true)
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            map[def.renderEmote] = def.name
        }
        return map
    }

}