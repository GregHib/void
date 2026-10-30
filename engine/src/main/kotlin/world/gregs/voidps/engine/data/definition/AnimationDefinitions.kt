package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState

object AnimationDefinitions : DefinitionsDecoder<AnimationDefinition> {

    override var definitions: Array<AnimationDefinition> = emptyArray()
    override var ids: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    val size: Int
        get() = definitions.size

    override fun empty() = AnimationDefinition.EMPTY

    fun init(definitions: Array<AnimationDefinition>): AnimationDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<AnimationDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

    fun install(state: DefinitionsState<AnimationDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs in [paths] to the already decoded [definitions].
     */
    fun load(paths: List<String>): AnimationDefinitions {
        ids = AnimationLoader.parse(definitions, paths)
        return this
    }
}
