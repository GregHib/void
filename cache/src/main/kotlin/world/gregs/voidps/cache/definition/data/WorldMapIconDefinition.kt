package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition

/**
 * @author GregHib <greg@gregs.world>
 * @since December 21, 2020
 */
data class WorldMapIconDefinition(
    override var id: Int = -1,
    var icons: Array<WorldMapIcon> = emptyArray()
) : Definition {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorldMapIconDefinition

        if (id != other.id) return false
        if (!icons.contentEquals(other.icons)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + icons.contentHashCode()
        return result
    }
}