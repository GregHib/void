package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.entity.item.drop.DropTables

object NPCDefinitions : DefinitionsDecoder<NPCDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = NPCDefinition.EMPTY

    override lateinit var definitions: Array<NPCDefinition>

    var loaded = false
        private set

    fun init(definitions: Array<NPCDefinition>): NPCDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<NPCDefinition>, map: Map<String, Int>) {
        this.definitions = definitions
        this.ids = map
        loaded = true
    }

    fun clear() {
        definitions = emptyArray()
        ids = emptyMap()
        loaded = false
    }

    fun install(state: DefinitionsState<NPCDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs in [paths] to the already decoded [definitions].
     */
    fun load(paths: List<String>, dropTables: DropTables? = null): NPCDefinitions {
        ids = NPCLoader.parse(definitions, paths, dropTables?.tables)
        return this
    }
}
