package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState

object ItemDefinitions : DefinitionsDecoder<ItemDefinition> {

    override var definitions: Array<ItemDefinition> = emptyArray()

    var loaded = false
        private set

    val size: Int
        get() = definitions.size

    override var ids: Map<String, Int> = emptyMap()

    fun init(definitions: Array<ItemDefinition>): ItemDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<ItemDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

    override fun empty() = ItemDefinition.EMPTY

    fun install(state: DefinitionsState<ItemDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs in [paths] to the already decoded [definitions].
     */
    fun load(paths: List<String>): ItemDefinitions {
        ids = ItemLoader.parse(definitions, paths)
        return this
    }
}
