package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.engine.data.config.RowDefinition

object Rows {

    var definitions: Array<RowDefinition> = emptyArray()
        private set
    var ids: Map<String, Int> = emptyMap()
        private set
    var loaded = false
        private set

    val size: Int
        get() = definitions.size

    fun get(name: String) = getOrNull(name) ?: error("Row not found: $name")

    fun get(id: Int) = definitions[id]

    fun getOrNull(name: String): RowDefinition? {
        return getOrNull(ids[name] ?: return null)
    }

    fun getOrNull(id: Int) = definitions.getOrNull(id)

    @TestOnly
    fun set(definitions: Array<RowDefinition>, ids: Map<String, Int>, ) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

}