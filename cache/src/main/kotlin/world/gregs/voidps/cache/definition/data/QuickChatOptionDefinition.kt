package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition

data class QuickChatOptionDefinition(
    override var id: Int = -1,
    var optionText: String? = null,
    var quickReplyOptions: IntArray? = null,
    var navigateChars: CharArray? = null,
    var dynamicData: IntArray? = null,
    var staticData: CharArray? = null,
) : Definition {
    fun getDynamicIndex(c: Char): Int {
        if (dynamicData == null) {
            return -1
        }
        for (i in dynamicData!!.indices) {
            if (staticData!![i] == c) {
                return dynamicData!![i]
            }
        }
        return -1
    }

    fun getOption(c: Char): Int {
        if (quickReplyOptions == null) {
            return -1
        }
        for (i in quickReplyOptions!!.indices) {
            if (navigateChars!![i] == c) {
                return quickReplyOptions!![i]
            }
        }
        return -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuickChatOptionDefinition

        if (id != other.id) return false
        if (optionText != other.optionText) return false
        if (quickReplyOptions != null) {
            if (other.quickReplyOptions == null) return false
            if (!quickReplyOptions.contentEquals(other.quickReplyOptions)) return false
        } else if (other.quickReplyOptions != null) {
            return false
        }
        if (navigateChars != null) {
            if (other.navigateChars == null) return false
            if (!navigateChars.contentEquals(other.navigateChars)) return false
        } else if (other.navigateChars != null) {
            return false
        }
        if (dynamicData != null) {
            if (other.dynamicData == null) return false
            if (!dynamicData.contentEquals(other.dynamicData)) return false
        } else if (other.dynamicData != null) {
            return false
        }
        if (staticData != null) {
            if (other.staticData == null) return false
            if (!staticData.contentEquals(other.staticData)) return false
        } else if (other.staticData != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (optionText?.hashCode() ?: 0)
        result = 31 * result + (quickReplyOptions?.contentHashCode() ?: 0)
        result = 31 * result + (navigateChars?.contentHashCode() ?: 0)
        result = 31 * result + (dynamicData?.contentHashCode() ?: 0)
        result = 31 * result + (staticData?.contentHashCode() ?: 0)
        return result
    }
}
