package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

/**
 * Looks up [Definition]'s using Definitions unique string identifier
 * Sets [Extra] values inside [Definition]
 */
interface DefinitionsDecoder<D> where D : Definition, D : Extra {
    var definitions: Array<D>
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

    companion object {
        private val tagRegex = "<.*?>".toRegex()

        fun removeTags(text: String) = text.replace(tagRegex, "")

        private val chars = "[\"',()?.!]".toRegex()
        private val underscoreChars = "[ /-]".toRegex()

        fun toIdentifier(name: String) =
            removeTags(name.lowercase().replace(underscoreChars, "_")).replace(chars, "").replace("&", "and").replace("à", "a").replace("é", "e").replace("ï", "i").replace("&#39;", "")
    }
}