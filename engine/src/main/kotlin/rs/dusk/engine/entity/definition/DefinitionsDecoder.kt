package rs.dusk.engine.entity.definition

import org.koin.dsl.module
import rs.dusk.cache.Definition
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.definition.Extra
import rs.dusk.engine.entity.definition.load.*

/**
 * Stores additional static information about an entity as well as a unique string identifier
 */
interface DefinitionsDecoder<T, D : DefinitionDecoder<T>> where T : Definition, T : Extra {
    val decoder: D
    val extras: Map<String, Map<String, Any>>
    val names: Map<Int, String>

    val size: Int
        get() = decoder.size

    val indices: IntRange
        get() = decoder.indices

    fun getOrNull(id: Int) = decoder.getOrNull(id)?.apply {
        applyExtras(this)
    }

    fun get(id: Int) = decoder.get(id).apply {
        applyExtras(this)
    }

    private fun applyExtras(definition: T) {
        val name = names[definition.id] ?: return
        val map = extras[name] ?: return
        definition.extras = map
    }

    fun getOrNull(name: String): T? {
        val map = extras[name] ?: return null
        val id = map["id"] as? Int ?: return null
        val definition = decoder.getOrNull(id) ?: return null
        definition.extras = map
        return definition
    }

    fun get(name: String): T {
        val map = extras[name]
        val id = map?.get("id") as? Int ?: -1
        val definition = decoder.get(id)
        if (map != null) {
            definition.extras = map
        }
        return definition
    }

    fun getNameOrNull(id: Int): String? {
        return names[id]
    }

    fun getName(id: Int): String = getNameOrNull(id) ?: ""

    fun getIdOrNull(name: String): Int? {
        return extras[name]?.get("id") as? Int
    }

    fun getId(name: String): Int {
        return getIdOrNull(name) ?: -1
    }

    companion object {
        private val tagRegex = "<.*?>".toRegex()

        fun removeTags(text: String) = text.replace(tagRegex, "")

        private val chars = "[\"',()]".toRegex()
        private val underscoreChars = "[ /]".toRegex()

        fun toIdentifier(name: String) = removeTags(name.toLowerCase().replace(underscoreChars, "_")).replace(chars, "").replace("&#39;", "")
    }
}

val detailsModule = module {
    single(createdAtStart = true) { ObjectDefinitionLoader(get(), get()).run(getProperty("objectDefinitionsPath")) }
    single(createdAtStart = true) { NPCDefinitionLoader(get(), get()).run(getProperty("npcDefinitionsPath")) }
    single(createdAtStart = true) { ItemDefinitionLoader(get(), get()).run(getProperty("itemDefinitionsPath")) }
    single(createdAtStart = true) { AnimationDefinitionLoader(get(), get()).run(getProperty("animationDefinitionsPath")) }
    single(createdAtStart = true) { GraphicDefinitionLoader(get(), get()).run(getProperty("graphicDefinitionsPath")) }
    single(createdAtStart = true) { ContainerDefinitionLoader(get(), get()).run(getProperty("containerDefinitionsPath")) }
}