package rs.dusk.engine.entity

import org.koin.dsl.module
import rs.dusk.cache.Definition
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.definition.Details
import rs.dusk.engine.entity.anim.detail.AnimationDefinitionLoader
import rs.dusk.engine.entity.character.contain.detail.ContainerDefinitionLoader
import rs.dusk.engine.entity.character.npc.detail.NPCDefinitionLoader
import rs.dusk.engine.entity.gfx.detail.GraphicDefinitionLoader
import rs.dusk.engine.entity.item.detail.ItemDefinitionLoader
import rs.dusk.engine.entity.obj.detail.ObjectDefinitionLoader

/**
 * Stores additional static information about an entity as well as a unique string identifier
 */
interface DetailsDecoder<T, D : DefinitionDecoder<T>> where T : Definition, T : Details {
    val decoder: D
    val details: Map<String, Map<String, Any>>
    val names: Map<Int, String>

    val size: Int
        get() = decoder.size

    val indices: IntRange
        get() = decoder.indices

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
    single(createdAtStart = true) { ObjectDefinitionLoader(get(), get()).run(getProperty("objectDetailsPath")) }
    single(createdAtStart = true) { NPCDefinitionLoader(get(), get()).run(getProperty("npcDetailsPath")) }
    single(createdAtStart = true) { ItemDefinitionLoader(get(), get()).run(getProperty("itemDetailsPath")) }
    single(createdAtStart = true) { AnimationDefinitionLoader(get(), get()).run(getProperty("animationDetailsPath")) }
    single(createdAtStart = true) { GraphicDefinitionLoader(get(), get()).run(getProperty("graphicDetailsPath")) }
    single(createdAtStart = true) { ContainerDefinitionLoader(get(), get()).run(getProperty("containerDetailsPath")) }
}