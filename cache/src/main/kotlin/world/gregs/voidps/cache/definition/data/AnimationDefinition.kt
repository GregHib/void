package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class AnimationDefinition(
    override var id: Int = -1,
    var priority: Int = 5,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimationDefinition

        if (id != other.id) return false
        if (priority != other.priority) return false
        if (stringId != other.stringId) return false
        return extras == other.extras
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + priority
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        val EMPTY = AnimationDefinition()
    }
}
