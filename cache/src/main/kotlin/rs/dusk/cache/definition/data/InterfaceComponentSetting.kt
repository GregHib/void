package rs.dusk.cache.definition.data

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
data class InterfaceComponentSetting(var setting: Int, var anInt7413: Int) {
    fun method2743(): Int {
        return setting and 0x3fda8 shr 11
    }

    fun method2744(): Boolean {
        return 0x1 and setting shr 22 != 0
    }

    fun method2745(): Int {
        return 0x1d36c1 and setting shr 18
    }

    fun method2746(): Boolean {
        return 0x1 and setting != 0
    }

    fun method2747(): Boolean {
        return 0x1 and setting shr 21 != 0
    }

    fun unlockedSlot(slot: Int): Boolean {
        return 0x1 and setting shr slot + 1 != 0
    }
}