package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.data.FileStorage

/**
 * Looks up [Definition]'s using Definitions unique string identifier
 * Sets [Extra] values inside [Definition]
 */
interface DefinitionsDecoder<D> where D : Definition, D : Extra {
    val definitions: Array<D>
    var ids: Map<String, Int>

    fun getOrNull(id: Int): D? {
        if (id == -1) {
            return null
        }
        return definitions.getOrNull(id)
    }

    fun empty(): D

    fun get(id: Int): D {
        return getOrNull(id) ?: empty()
    }

    fun getOrNull(id: String): D? {
        if (id.isBlank()) {
            return null
        }
        val int = id.toIntOrNull()
        if (int != null) {
            return getOrNull(int)
        }
        return getOrNull(ids[id] ?: return null)
    }

    fun get(id: String): D {
        return getOrNull(id) ?: empty()
    }

    fun contains(id: String): Boolean {
        return getOrNull(id) != null
    }

    fun printDuplicates(storage: FileStorage, path: String) {
        for ((key, group) in storage.loadMapIds(path).entries.groupBy { it.value["id"] }.filter { it.value.size > 1 }) {
            println("Found ${group.size} duplicates for id $key - ${group.map { it.key }}")
        }
    }

    fun decode(storage: FileStorage, path: String, modifications: DefinitionModifications = DefinitionModifications()): Int {
        return decode(storage.loadMapIds(path), modifications)
    }

    fun decode(data: Map<String, Map<String, Any>>, modifications: DefinitionModifications = DefinitionModifications()): Int {
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        ids = data.map { it.key to it.value["id"] as Int }.toMap()
        apply(names, modifications.apply(data))
        modifications.apply(definitions, names)
        return names.size
    }

    fun apply(names: Map<Int, String>, extras: Map<String, Map<String, Any>>, block: (D) -> Unit = {}) {
        for (i in definitions.indices) {
            val definition = definitions[i]
            val name = names[i]
            definition.stringId = name ?: i.toString()
            val extra = extras[name] ?: continue
            definition.extras = Object2ObjectOpenHashMap(extra)
            block.invoke(definition)
        }
    }

    fun FileStorage.loadMapIds(path: String): Map<String, Map<String, Any>> = load<Map<String, Any>>(path).mapIds()

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