package world.gregs.voidps.engine.entity.definition

import org.koin.dsl.module
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.definition.Extra

/**
 * Looks up [Definition]'s using [Definitions] unique string identifier
 * Sets [Extra] values inside [Definition]
 */
abstract class DefinitionsDecoder<T, D : DefinitionDecoder<T>> : Definitions<T> where T : Definition, T : Extra {
    abstract val decoder: D

    val modifications = DefinitionModifications()

    val size: Int
        get() = decoder.last

    val indices: IntRange
        get() = decoder.indices

    override fun decodeOrNull(name: String, id: Int): T? = decoder.getOrNull(id)

    override fun decode(name: String, id: Int): T = decoder.get(id)

    internal fun Map<String, Map<String, Any>>.mapModifications(): Map<String, Map<String, Any>> = mapValues { (_, value) ->
        val copy = this[value["copy"]]
        if (copy != null) {
            val mut = copy.toMutableMap()
            mut["id"] = value["id"] as Int
            modifications.modify(mut)
        } else {
            modifications.modify(value)
        }
    }

    companion object {

        internal fun Map<String, Any>.mapIds(): Map<String, Map<String, Any>> = mapValues { (_, value) ->
            if (value is Int) mapOf("id" to value) else value as Map<String, Any>
        }

        private val tagRegex = "<.*?>".toRegex()

        fun removeTags(text: String) = text.replace(tagRegex, "")

        private val chars = "[\"',()?.!]".toRegex()
        private val underscoreChars = "[ /-]".toRegex()

        fun toIdentifier(name: String) = removeTags(name.lowercase().replace(underscoreChars, "_")).replace(chars, "").replace("&", "and").replace("à", "a").replace("é", "e").replace("ï", "i").replace("&#39;", "")
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
    single(createdAtStart = true) { GearDefinitions().load() }
    single(createdAtStart = true) { ItemOnItemDefinitions().load() }
}