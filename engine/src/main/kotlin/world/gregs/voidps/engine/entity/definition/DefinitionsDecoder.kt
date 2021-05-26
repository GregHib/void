package world.gregs.voidps.engine.entity.definition

import org.koin.dsl.module
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.definition.Extra

/**
 * Looks up [Definition]'s using [Extras] unique string identifier
 * Sets [Extra] values inside [Definition]
 */
interface DefinitionsDecoder<T, D : DefinitionDecoder<T>> : Extras where T : Definition, T : Extra {
    val decoder: D

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
        setExtras(definition, name, map)
    }

    open fun setExtras(definition: T, name: String, map: Map<String, Any>) {
        definition.extras = map
    }

    fun getOrNull(name: String): T? {
        val map = extras[name] ?: return null
        val id = map["id"] as? Int ?: return null
        val definition = decoder.getOrNull(id) ?: return null
        setExtras(definition, name, map)
        return definition
    }

    fun get(name: String): T {
        val map = extras[name]
        val id = map?.get("id") as? Int ?: -1
        val definition = decoder.get(id)
        if (map != null) {
            setExtras(definition, name, map)
        }
        return definition
    }

    companion object {
        private val tagRegex = "<.*?>".toRegex()

        fun removeTags(text: String) = text.replace(tagRegex, "")

        private val chars = "[\"',()]".toRegex()
        private val underscoreChars = "[ /]".toRegex()

        // TODO remove exclamations
        fun toIdentifier(name: String) = removeTags(name.toLowerCase().replace(underscoreChars, "_")).replace(chars, "").replace("&#39;", "")
    }
}

val definitionsModule = module {
    single(createdAtStart = true) { ObjectDefinitions(get()).load() }
    single(createdAtStart = true) { NPCDefinitions(get()).load() }
    single(createdAtStart = true) { ItemDefinitions(get()).load() }
    single(createdAtStart = true) { AnimationDefinitions(get()).load() }
    single(createdAtStart = true) { GraphicDefinitions(get()).load() }
    single(createdAtStart = true) { ContainerDefinitions(get()).load() }
    single(createdAtStart = true) { InterfaceDefinitions(get()).load() }
    single(createdAtStart = true) { SoundDefinitions().load() }
    single(createdAtStart = true) { MidiDefinitions().load() }
    single(createdAtStart = true) { JingleDefinitions().load() }
}