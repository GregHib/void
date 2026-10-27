package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState

class FontDefinitions(
    override var definitions: Array<FontDefinition>,
) : DefinitionsDecoder<FontDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = FontDefinition.EMPTY

    fun install(state: DefinitionsState<FontDefinition>) {
        definitions = state.definitions
        ids = state.ids
    }

    /**
     * Applies the config at [path] to the already decoded [definitions].
     */
    fun load(path: String): FontDefinitions {
        ids = FontLoader.parse(definitions, path)
        return this
    }
}
