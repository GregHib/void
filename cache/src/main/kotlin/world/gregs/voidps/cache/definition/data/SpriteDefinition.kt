package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition

data class SpriteDefinition(
    override var id: Int = -1,
    var sprites: Array<IndexedSprite>? = null,
) : Definition {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpriteDefinition

        if (id != other.id) return false
        if (sprites != null) {
            if (other.sprites == null) return false
            if (!sprites.contentEquals(other.sprites)) return false
        } else if (other.sprites != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (sprites?.contentHashCode() ?: 0)
        return result
    }
}
