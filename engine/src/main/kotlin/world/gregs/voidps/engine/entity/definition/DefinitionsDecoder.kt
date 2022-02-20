package world.gregs.voidps.engine.entity.definition

import org.koin.dsl.module
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.decoder.*

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
            for ((k, v) in value) {
                mut[k] = v
            }
            modifications.modify(mut)
        } else {
            modifications.modify(value)
        }
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
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
    single(createdAtStart = true) { ObjectDefinitions(ObjectDecoder(get(), member = true, lowDetail = false, configReplace = true)).load() }
    single(createdAtStart = true) { NPCDefinitions(NPCDecoder(get(), member = true)).load() }
    single(createdAtStart = true) { ItemDefinitions(ItemDecoder(get())).load() }
    single(createdAtStart = true) { AnimationDefinitions(AnimationDecoder(get())).load() }
    single(createdAtStart = true) { GraphicDefinitions(GraphicDecoder(get())).load() }
    single(createdAtStart = true) { ContainerDefinitions(get()).load() }
    single(createdAtStart = true) { InterfaceDefinitions(InterfaceDecoder(get())).load() }
    single(createdAtStart = true) { SoundDefinitions().load() }
    single(createdAtStart = true) { MidiDefinitions().load() }
    single(createdAtStart = true) { VariableDefinitions().load() }
    single(createdAtStart = true) { JingleDefinitions().load() }
    single(createdAtStart = true) { SpellDefinitions().load() }
    single(createdAtStart = true) { GearDefinitions().load() }
    single(createdAtStart = true) { ItemOnItemDefinitions().load() }
    single(createdAtStart = true) { StyleDefinitions().load(ClientScriptDecoder(get(), revision634 = true)) }
    single(createdAtStart = true) { AccountDefinitions(get()).load() }
}