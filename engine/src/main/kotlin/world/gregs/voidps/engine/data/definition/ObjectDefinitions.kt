package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState

object ObjectDefinitions : DefinitionsDecoder<ObjectDefinition> {

    override lateinit var definitions: Array<ObjectDefinition>
    override lateinit var ids: Map<String, Int>

    fun getValue(id: Int): ObjectDefinition = definitions[id]

    override fun empty() = ObjectDefinition.EMPTY

    var loaded = false
        private set

    fun init(definitions: Array<ObjectDefinition>): ObjectDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<ObjectDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        definitions = emptyArray()
        ids = emptyMap()
        loaded = false
    }

    fun install(state: DefinitionsState<ObjectDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs in [paths] to the already decoded [definitions].
     */
    fun load(paths: List<String>): ObjectDefinitions {
        ids = ObjectLoader.parse(definitions, paths)
        return this
    }
}
