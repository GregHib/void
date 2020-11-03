package rs.dusk.engine.entity

import org.koin.dsl.module
import rs.dusk.cache.Definition
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.definition.Details
import rs.dusk.engine.entity.anim.detail.AnimationDetailsLoader
import rs.dusk.engine.entity.character.contain.detail.ContainerDetailsLoader
import rs.dusk.engine.entity.character.npc.detail.NPCDetailsLoader
import rs.dusk.engine.entity.gfx.detail.GraphicDetailsLoader
import rs.dusk.engine.entity.item.detail.ItemDetailsLoader
import rs.dusk.engine.entity.obj.detail.ObjectDetailsLoader

/**
 * Stores additional static information about an entity as well as a unique string identifier
 */
interface DetailsDecoder<T, D : DefinitionDecoder<T>> where T : Definition, T : Details {
    val decoder: D
    val details: Map<String, Map<String, Any>>
    val names: Map<Int, String>

    fun getOrNull(id: Int) = decoder.getOrNull(id)

    fun getOrNull(name: String): T? {
        val map = details[name] ?: return null
        val id = map["id"] as? Int ?: return null
        val definitions = decoder.getOrNull(id) ?: return null
        definitions.details = map
        return definitions
    }

    fun get(id: Int) = decoder.get(id)

    fun get(name: String): T {
        val map = details[name]
        val id = map?.get("id") as? Int ?: -1
        val definitions = decoder.get(id)
        if (map != null) {
            definitions.details = map
        }
        return definitions
    }

    fun getNameOrNull(id: Int): String? {
        return names[id]
    }

    fun getName(id: Int): String = getNameOrNull(id) ?: ""

    fun getIdOrNull(name: String): Int? {
        return details[name]?.get("id") as? Int
    }

    fun getId(name: String): Int {
        return getIdOrNull(name) ?: -1
    }

    companion object {
        private val regex = Regex("<.*>")
        fun toIdentifier(name: String) = name.toLowerCase().replace(" ", "_").replace(regex, "").replace("\"", "").replace("\'", "").replace(",", "").replace("(", "").replace(")", "")
    }
}

val detailsModule = module {
    single(createdAtStart = true) { ObjectDetailsLoader(get(), get()).run(getProperty("objectDetailsPath")) }
    single(createdAtStart = true) { NPCDetailsLoader(get(), get()).run(getProperty("npcDetailsPath")) }
    single(createdAtStart = true) { ItemDetailsLoader(get(), get()).run(getProperty("itemDetailsPath")) }
    single(createdAtStart = true) { AnimationDetailsLoader(get(), get()).run(getProperty("animationDetailsPath")) }
    single(createdAtStart = true) { GraphicDetailsLoader(get(), get()).run(getProperty("graphicDetailsPath")) }
    single(createdAtStart = true) { ContainerDetailsLoader(get(), get()).run(getProperty("containerDetailsPath")) }
}