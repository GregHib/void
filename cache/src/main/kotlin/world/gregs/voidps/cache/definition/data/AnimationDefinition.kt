package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized

data class AnimationDefinition(
    override var id: Int = -1,
    var priority: Int = 5,
    override var stringId: String = "",
    override var params: Map<Int, Any>? = null,
) : Definition,
    Parameterized {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimationDefinition

        if (id != other.id) return false
        if (priority != other.priority) return false
        if (stringId != other.stringId) return false
        return params == other.params
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + priority
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (params?.hashCode() ?: 0)
        return result
    }

    companion object {
        val EMPTY = AnimationDefinition()
    }
}
