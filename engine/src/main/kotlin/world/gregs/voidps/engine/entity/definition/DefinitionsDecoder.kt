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
        get() = decoder.last

    val indices: IntRange
        get() = decoder.indices

    fun setExtras(definition: T, name: String, map: Map<String, Any>?) {
        map?.let {
            definition.extras = it
        }
    }

    fun getOrNull(name: String): T? {
        var id = name.toIntOrNull() ?: -1
        var map: Map<String, Any>? = null
        if (id == -1) {
            map = extras[name]
            id = map?.get("id") as? Int ?: return null
        } else {
            names[id]?.let {
                map = extras[it]
            }
        }
        val definition = decoder.getOrNull(id) ?: return null
        setExtras(definition, name, map)
        return definition
    }

    fun get(name: String): T {
        var id = name.toIntOrNull() ?: -1
        var map: Map<String, Any>? = null
        if (id == -1) {
            map = extras[name]
            id = map?.get("id") as? Int ?: -1
        } else {
            names[id]?.let {
                map = extras[it]
            }
        }
        val definition = decoder.get(id)
        setExtras(definition, name, map)
        return definition
    }

    companion object {

        internal fun Map<String, Any>.mapIds(): Map<String, Map<String, Any>> = mapValues { (_, value) ->
            if (value is Int) mapOf("id" to value) else value as Map<String, Any>
        }.toMap()

        private val tagRegex = "<.*?>".toRegex()

        fun removeTags(text: String) = text.replace(tagRegex, "")

        private val chars = "[\"',()?.!]".toRegex()
        private val underscoreChars = "[ /-]".toRegex()

        fun toIdentifier(name: String) = removeTags(name.toLowerCase().replace(underscoreChars, "_")).replace(chars, "").replace("&", "and").replace("à", "a").replace("é", "e").replace("ï", "i").replace("&#39;", "")
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
    single(createdAtStart = true) { VariableDefinitions().load() }
    single(createdAtStart = true) { JingleDefinitions().load() }
    single(createdAtStart = true) { SpellDefinitions().load() }
}