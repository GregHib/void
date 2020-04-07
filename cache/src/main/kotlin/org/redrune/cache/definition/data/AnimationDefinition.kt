package org.redrune.cache.definition.data

import org.redrune.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class AnimationDefinition(
    override var id: Int = -1,
    var durations: IntArray? = null,
    var primaryFrames: IntArray? = null,
    var loopOffset: Int = -1,
    var interleaveOrder: BooleanArray? = null,
    var priority: Int = 5,
    var leftHand: Int = -1,
    var rightHand: Int = -1,
    var maxLoops: Int = 99,
    var animatingPrecedence: Int = -1,
    var walkingPrecedence: Int = -1,
    var replayMode: Int = 2,
    var secondaryFrames: IntArray? = null,
    var anIntArrayArray700: Array<IntArray?>? = null,
    var aBoolean691: Boolean = false,
    var tweened: Boolean = false,
    var aBoolean699: Boolean = false,
    var anIntArray701: IntArray? = null,
    var anIntArray690: IntArray? = null,
    var anIntArray692: IntArray? = null
) : Definition {

    val time: Int
        get() = (durations?.sum() ?: 0) * 10

    val clientCycles: Int
        get() {
            if (durations == null) {
                return 0
            }
            var total = 0
            for (i in 0 until durations!!.size - 3) {
                total += durations!![i]
            }
            return total
        }
}