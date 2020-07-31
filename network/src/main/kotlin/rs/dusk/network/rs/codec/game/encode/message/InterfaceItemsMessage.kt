package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends a list of items to display on a interface item group component
 * @param key The id of the interface item group
 * @param items List of the item ids and amounts to display
 * @param negativeKey Whether the key is negative and needs encoding differently (optional - calculated automatically)
 */
data class InterfaceItemsMessage(val key: Int, val items: IntArray, val amounts: IntArray, val negativeKey: Boolean = key < 0) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceItemsMessage

        if (key != other.key) return false
        if (!items.contentEquals(other.items)) return false
        if (!amounts.contentEquals(other.amounts)) return false
        if (negativeKey != other.negativeKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key
        result = 31 * result + items.contentHashCode()
        result = 31 * result + amounts.contentHashCode()
        result = 31 * result + negativeKey.hashCode()
        return result
    }
}