package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class SoundDefinition(
    override var id: Int,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {
    companion object {
        val EMPTY = SoundDefinition(-1)
    }
}
