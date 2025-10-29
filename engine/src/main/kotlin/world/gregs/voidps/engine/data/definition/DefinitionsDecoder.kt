package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Transforms
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

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

    fun get(id: Int): D = getOrNull(id) ?: empty()

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

    fun get(id: String): D = getOrNull(id) ?: empty()

    fun contains(id: String): Boolean = getOrNull(id) != null

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

    fun resolve(definition: D, player: Player): D {
        if (definition !is Transforms) {
            return definition
        }
        val transforms = definition.transforms ?: return definition
        if (definition.varbit != -1) {
            val index = index(player, definition.varbit, true)
            return get(transforms.getOrNull(index.coerceAtMost(transforms.lastIndex)) ?: return definition)
        }
        if (definition.varp != -1) {
            val index = index(player, definition.varp, false)
            return get(transforms.getOrNull(index.coerceAtMost(transforms.lastIndex)) ?: return definition)
        }
        return definition
    }

    private fun index(player: Player, id: Int, varbit: Boolean): Int {
        val definitions: VariableDefinitions = get()
        val key = (if (varbit) definitions.getVarbit(id) else definitions.getVarp(id)) ?: return 0
        val definition = definitions.get(key)
        val value = player.variables.get<Any>(key) ?: return 0
        return definition?.values?.toInt(value) ?: 0
    }

    companion object {
        private val tagRegex = "<.*?>".toRegex()

        fun removeTags(text: String) = text.replace(tagRegex, "")

        private val chars = "[\"',()?.!]".toRegex()
        private val underscoreChars = "[ /-]".toRegex()

        fun toIdentifier(name: String) = removeTags(name.lowercase().replace(underscoreChars, "_")).replace(chars, "").replace("&", "and").replace("à", "a").replace("é", "e").replace("ï", "i").replace("&#39;", "")
    }
}
