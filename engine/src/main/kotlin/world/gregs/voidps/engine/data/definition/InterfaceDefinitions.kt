package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition

object InterfaceDefinitions : DefinitionsDecoder<InterfaceDefinition> {

    override var definitions: Array<InterfaceDefinition> = emptyArray()
    override var ids: Map<String, Int> = emptyMap()
    var componentIds: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    fun init(definitions: Array<InterfaceDefinition>): InterfaceDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<InterfaceDefinition>, ids: Map<String, Int>, components: Map<String, Int> = emptyMap()) {
        this.definitions = definitions
        this.ids = ids
        this.componentIds = components
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        this.componentIds = emptyMap()
        loaded = false
    }

    fun getComponentId(id: String, component: String) = componentIds["$id:$component"]

    fun getComponent(id: String, component: String): InterfaceComponentDefinition? {
        return get(id).components?.get(getComponentId(id, component) ?: return null)
    }

    override fun empty() = InterfaceDefinition.EMPTY

    fun install(state: InterfaceState) {
        definitions = state.definitions
        ids = state.ids
        componentIds = state.componentIds
        loaded = true
    }

    /**
     * Applies the configs to the already decoded [definitions].
     */
    fun load(paths: List<String>, typePath: String): InterfaceDefinitions {
        val state = InterfaceLoader.parse(definitions, paths, typePath)
        ids = state.ids
        componentIds = state.componentIds
        return this
    }
}
