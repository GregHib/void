package rs.dusk.cache.definition.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class QuickChatOptionDefinition(
    override var id: Int = -1,
    var optionText: String? = null,
    var quickReplyOptions: IntArray? = null,
    var navigateChars: CharArray? = null,
    var dynamicData: IntArray? = null,
    var staticData: CharArray? = null
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
}