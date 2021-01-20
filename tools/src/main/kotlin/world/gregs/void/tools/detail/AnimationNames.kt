package world.gregs.void.tools.detail

import org.koin.core.context.startKoin
import world.gregs.void.cache.Cache
import world.gregs.void.cache.config.decoder.RenderAnimationDecoder
import world.gregs.void.cache.definition.decoder.AnimationDecoder
import world.gregs.void.cache.definition.decoder.ItemDecoder
import world.gregs.void.cache.definition.decoder.NPCDecoder
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule
import world.gregs.void.engine.data.file.FileLoader
import world.gregs.void.engine.data.file.fileLoaderModule
import world.gregs.void.engine.entity.definition.DefinitionsDecoder.Companion.toIdentifier

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
        val itemDecoder = ItemDecoder(cache)
        val renders = getRenderAnimations(cache)
        val map = mutableMapOf<String, MutableList<Int>>()
        repeat(decoder.size) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            val render = renders[id]
            if(render != null) {
                map.getOrPut(render) { mutableListOf() }.add(id)
            }
            if (def.leftHand != -1) {
                map.getOrPut(toIdentifier(itemDecoder.get(def.leftHand).name)) { mutableListOf() }.add(id)
            }
            if (def.rightHand != -1) {
                map.getOrPut(toIdentifier(itemDecoder.get(def.rightHand).name)) { mutableListOf() }.add(id)
            }
        }

        val path = "./animation-details.yml"
        val sorted = map.flatMap {
            it.value.sortedBy { it }.mapIndexed { index, i -> (if(index > 0) "${it.key}_${index + 1}" else it.key) to Ids(i) }
        }.sortedBy { it.second.id }.toMap()
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