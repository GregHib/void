package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState

/**
 * Also known as AttributeMaps in cs2 or rows
 */
object StructDefinitions : DefinitionsDecoder<StructDefinition> {

    override var definitions: Array<StructDefinition> = emptyArray()

    override var ids: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    fun init(definitions: Array<StructDefinition>): StructDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<StructDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

    override fun empty() = StructDefinition.EMPTY

    fun install(state: DefinitionsState<StructDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the config at [path] to the already decoded [definitions].
     */
    fun load(path: String): StructDefinitions {
        ids = StructLoader.parse(definitions, path)
        return this
    }
}
