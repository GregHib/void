package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
data class MapRegionMessage(
    val chunkX: Int,
    val chunkY: Int,
    val forceReload: Boolean,
    val mapSize: Int,
    val xteas: Array<IntArray>,
    val clientIndex: Int? = null,
    val clientTile: Int? = null,
    val playerRegions: IntArray? = null
) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapRegionMessage

        if (chunkX != other.chunkX) return false
        if (chunkY != other.chunkY) return false
        if (forceReload != other.forceReload) return false
        if (mapSize != other.mapSize) return false
        if (!xteas.contentDeepEquals(other.xteas)) return false
        if (clientIndex != other.clientIndex) return false
        if (playerRegions != null) {
            if (other.playerRegions == null) return false
            if (!playerRegions.contentEquals(other.playerRegions)) return false
        } else if (other.playerRegions != null) return false
        if (clientTile != other.clientTile) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chunkX
        result = 31 * result + chunkY
        result = 31 * result + forceReload.hashCode()
        result = 31 * result + mapSize
        result = 31 * result + xteas.contentDeepHashCode()
        result = 31 * result + (clientIndex ?: 0)
        result = 31 * result + (playerRegions?.contentHashCode() ?: 0)
        result = 31 * result + (clientTile ?: 0)
        return result
    }

}