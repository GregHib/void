package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized

data class MidiDefinition(
    override var id: Int,
    override var stringId: String = "",
    override var params: Map<Int, Any>? = null,
) : Definition,
    Parameterized {
    companion object {
        val EMPTY = MidiDefinition(-1)
    }
}
