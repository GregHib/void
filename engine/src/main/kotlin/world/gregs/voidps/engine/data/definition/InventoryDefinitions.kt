package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState

object InventoryDefinitions : DefinitionsDecoder<InventoryDefinition> {

    override var definitions: Array<InventoryDefinition> = emptyArray()
    override var ids: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    fun init(definitions: Array<InventoryDefinition>): InventoryDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<InventoryDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

    override fun empty() = InventoryDefinition.EMPTY

    fun install(state: DefinitionsState<InventoryDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs to the already decoded [definitions].
     */
    fun load(paths: List<String>, shopPaths: List<String>): InventoryDefinitions {
        ids = InventoryLoader.parse(definitions, paths, shopPaths)
        return this
    }
}
