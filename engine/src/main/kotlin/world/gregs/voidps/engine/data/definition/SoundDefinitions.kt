package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState

/**
 * Officially known as Synths
 */
class SoundDefinitions : DefinitionsDecoder<SoundDefinition> {

    override lateinit var definitions: Array<SoundDefinition>
    override lateinit var ids: Map<String, Int>

    fun install(state: DefinitionsState<SoundDefinition>) {
        definitions = state.definitions
        ids = state.ids
    }

    fun load(paths: List<String>): SoundDefinitions {
        install(SoundLoader.parse(paths))
        return this
    }

    override fun empty() = SoundDefinition.EMPTY
}
