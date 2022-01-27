package world.gregs.voidps.tools.detail

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.data.file.fileStorageModule
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.toIdentifier

/**
 * Dumps unique string identifiers for animation ids
 * Identifies animation names by cross-referencing npc names with render animations
 */
object AnimationNames {

    private data class Ids(val id: Int)

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, fileStorageModule)
        }.koin
        val cache: Cache = koin.get()
        val storage: FileStorage = koin.get()
        val decoder = AnimationDecoder(cache)
        val itemDecoder = ItemDecoder(cache)
        val renders = getRenderAnimations(cache)
        val map = mutableMapOf<String, MutableList<Int>>()
        repeat(decoder.last) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            val render = renders[id]
            if(render != null) {
                map.getOrPut(render) { mutableListOf() }.add(id)
            }
            if (def.leftHandItem != -1) {
                map.getOrPut(toIdentifier(itemDecoder.get(def.leftHandItem).name)) { mutableListOf() }.add(id)
            }
            if (def.rightHandItem != -1) {
                map.getOrPut(toIdentifier(itemDecoder.get(def.rightHandItem).name)) { mutableListOf() }.add(id)
            }
        }

        val path = "./animation-details.yml"
        val sorted = map.flatMap {
            it.value.sortedBy { value -> value }.mapIndexed { index, i -> (if(index > 0) "${it.key}_${index + 1}" else it.key) to Ids(i) }
        }.sortedBy { it.second.id }.toMap()
        storage.save(path, sorted)
        println("${sorted.size} animation identifiers dumped to $path.")
    }

    private fun getRenderAnimations(cache: Cache): Map<Int, String> {
        val renders = getNPCRenderIds(cache)
        val decoder = RenderAnimationDecoder(cache)
        val map = mutableMapOf<Int, String>()
        repeat(decoder.last) { id ->
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
        repeat(decoder.last) { id ->
            val def = decoder.getOrNull(id) ?: return@repeat
            map[def.renderEmote] = def.name
        }
        return map
    }

}