package rs.dusk.cache.definition.data

import rs.dusk.cache.Definition
import rs.dusk.cache.definition.Extra

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
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
    var anIntArray692: IntArray? = null,
    override var extras: Map<String, Any> = emptyMap()
) : Definition, Extra {

    val time: Int
        get() = (durations?.sum() ?: 0) * 10

    val clientTicks: Int
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimationDefinition

        if (id != other.id) return false
        if (durations != null) {
            if (other.durations == null) return false
            if (!durations!!.contentEquals(other.durations!!)) return false
        } else if (other.durations != null) return false
        if (primaryFrames != null) {
            if (other.primaryFrames == null) return false
            if (!primaryFrames!!.contentEquals(other.primaryFrames!!)) return false
        } else if (other.primaryFrames != null) return false
        if (loopOffset != other.loopOffset) return false
        if (interleaveOrder != null) {
            if (other.interleaveOrder == null) return false
            if (!interleaveOrder!!.contentEquals(other.interleaveOrder!!)) return false
        } else if (other.interleaveOrder != null) return false
        if (priority != other.priority) return false
        if (leftHand != other.leftHand) return false
        if (rightHand != other.rightHand) return false
        if (maxLoops != other.maxLoops) return false
        if (animatingPrecedence != other.animatingPrecedence) return false
        if (walkingPrecedence != other.walkingPrecedence) return false
        if (replayMode != other.replayMode) return false
        if (secondaryFrames != null) {
            if (other.secondaryFrames == null) return false
            if (!secondaryFrames!!.contentEquals(other.secondaryFrames!!)) return false
        } else if (other.secondaryFrames != null) return false
        if (anIntArrayArray700 != null) {
            if (other.anIntArrayArray700 == null) return false
            if (!anIntArrayArray700!!.contentDeepEquals(other.anIntArrayArray700!!)) return false
        } else if (other.anIntArrayArray700 != null) return false
        if (aBoolean691 != other.aBoolean691) return false
        if (tweened != other.tweened) return false
        if (aBoolean699 != other.aBoolean699) return false
        if (anIntArray701 != null) {
            if (other.anIntArray701 == null) return false
            if (!anIntArray701!!.contentEquals(other.anIntArray701!!)) return false
        } else if (other.anIntArray701 != null) return false
        if (anIntArray690 != null) {
            if (other.anIntArray690 == null) return false
            if (!anIntArray690!!.contentEquals(other.anIntArray690!!)) return false
        } else if (other.anIntArray690 != null) return false
        if (anIntArray692 != null) {
            if (other.anIntArray692 == null) return false
            if (!anIntArray692!!.contentEquals(other.anIntArray692!!)) return false
        } else if (other.anIntArray692 != null) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (durations?.contentHashCode() ?: 0)
        result = 31 * result + (primaryFrames?.contentHashCode() ?: 0)
        result = 31 * result + loopOffset
        result = 31 * result + (interleaveOrder?.contentHashCode() ?: 0)
        result = 31 * result + priority
        result = 31 * result + leftHand
        result = 31 * result + rightHand
        result = 31 * result + maxLoops
        result = 31 * result + animatingPrecedence
        result = 31 * result + walkingPrecedence
        result = 31 * result + replayMode
        result = 31 * result + (secondaryFrames?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArrayArray700?.contentDeepHashCode() ?: 0)
        result = 31 * result + aBoolean691.hashCode()
        result = 31 * result + tweened.hashCode()
        result = 31 * result + aBoolean699.hashCode()
        result = 31 * result + (anIntArray701?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray690?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray692?.contentHashCode() ?: 0)
        result = 31 * result + extras.hashCode()
        return result
    }
}