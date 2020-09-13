package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends a list of items to display on a interface item group component
 * @param key The id of the container
 * @param items List of the item ids to display
 * @param amounts List of the item amounts to display
 * @param secondary Optional to send to the primary or secondary container
 */
data class ContainerItemsMessage(val key: Int, val items: IntArray, val amounts: IntArray, val secondary: Boolean) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContainerItemsMessage

        if (key != other.key) return false
        if (!items.contentEquals(other.items)) return false
        if (!amounts.contentEquals(other.amounts)) return false
        if (secondary != other.secondary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key
        result = 31 * result + items.contentHashCode()
        result = 31 * result + amounts.contentHashCode()
        result = 31 * result + secondary.hashCode()
        return result
    }
}