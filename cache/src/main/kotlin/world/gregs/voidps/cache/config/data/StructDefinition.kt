package world.gregs.voidps.cache.config.data

import kotlinx.serialization.Serializable
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.serialization.ParamsSerializer

@Serializable
data class StructDefinition(
    override var id: Int = -1,
    override var stringId: String = "",
    @Serializable(with = ParamsSerializer::class)
    override var params: Map<Int, Any>? = null,
) : Definition,
    Parameterized {
    companion object {
        val EMPTY = StructDefinition()
    }
}
