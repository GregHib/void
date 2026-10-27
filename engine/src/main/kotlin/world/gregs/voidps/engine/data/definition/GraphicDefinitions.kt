package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState

object GraphicDefinitions : DefinitionsDecoder<GraphicDefinition> {

    override var definitions: Array<GraphicDefinition> = emptyArray()
    override var ids: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    val size: Int
        get() = definitions.size

    override fun empty() = GraphicDefinition.EMPTY

    fun init(definitions: Array<GraphicDefinition>): GraphicDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<GraphicDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

    fun install(state: DefinitionsState<GraphicDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs in [paths] to the already decoded [definitions].
     */
    fun load(paths: List<String>): GraphicDefinitions {
        ids = GraphicLoader.parse(definitions, paths)
        return this
    }
}
