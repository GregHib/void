package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class AnimationDefinitionFull(
    override var id: Int = -1,
    var durations: IntArray? = null,
    var frames: IntArray? = null,
    var loopOffset: Int = -1,
    var interleaveOrder: BooleanArray? = null,
    var priority: Int = 5,
    var leftHandItem: Int = -1,
    var rightHandItem: Int = -1,
    var maxLoops: Int = 99,
    var animatingPrecedence: Int = -1,
    var walkingPrecedence: Int = -1,
    var replayMode: Int = 2,
    var expressionFrames: IntArray? = null,
    var sounds: Array<IntArray?>? = null,
    var aBoolean691: Boolean = false,
    var tweened: Boolean = false,
    var useSounds: Boolean = false,
    var volumes: IntArray? = null,
    var primarySpeeds: IntArray? = null,
    var secondarySpeeds: IntArray? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimationDefinitionFull

        if (id != other.id) return false
        if (durations != null) {
            if (other.durations == null) return false
            if (!durations.contentEquals(other.durations)) return false
        } else if (other.durations != null) {
            return false
        }
        if (frames != null) {
            if (other.frames == null) return false
            if (!frames.contentEquals(other.frames)) return false
        } else if (other.frames != null) {
            return false
        }
        if (loopOffset != other.loopOffset) return false
        if (interleaveOrder != null) {
            if (other.interleaveOrder == null) return false
            if (!interleaveOrder.contentEquals(other.interleaveOrder)) return false
        } else if (other.interleaveOrder != null) {
            return false
        }
        if (priority != other.priority) return false
        if (leftHandItem != other.leftHandItem) return false
        if (rightHandItem != other.rightHandItem) return false
        if (maxLoops != other.maxLoops) return false
        if (animatingPrecedence != other.animatingPrecedence) return false
        if (walkingPrecedence != other.walkingPrecedence) return false
        if (replayMode != other.replayMode) return false
        if (expressionFrames != null) {
            if (other.expressionFrames == null) return false
            if (!expressionFrames.contentEquals(other.expressionFrames)) return false
        } else if (other.expressionFrames != null) {
            return false
        }
        if (sounds != null) {
            if (other.sounds == null) return false
            if (!sounds.contentDeepEquals(other.sounds)) return false
        } else if (other.sounds != null) {
            return false
        }
        if (aBoolean691 != other.aBoolean691) return false
        if (tweened != other.tweened) return false
        if (useSounds != other.useSounds) return false
        if (volumes != null) {
            if (other.volumes == null) return false
            if (!volumes.contentEquals(other.volumes)) return false
        } else if (other.volumes != null) {
            return false
        }
        if (primarySpeeds != null) {
            if (other.primarySpeeds == null) return false
            if (!primarySpeeds.contentEquals(other.primarySpeeds)) return false
        } else if (other.primarySpeeds != null) {
            return false
        }
        if (secondarySpeeds != null) {
            if (other.secondarySpeeds == null) return false
            if (!secondarySpeeds.contentEquals(other.secondarySpeeds)) return false
        } else if (other.secondarySpeeds != null) {
            return false
        }
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (durations?.contentHashCode() ?: 0)
        result = 31 * result + (frames?.contentHashCode() ?: 0)
        result = 31 * result + loopOffset
        result = 31 * result + (interleaveOrder?.contentHashCode() ?: 0)
        result = 31 * result + priority
        result = 31 * result + leftHandItem
        result = 31 * result + rightHandItem
        result = 31 * result + maxLoops
        result = 31 * result + animatingPrecedence
        result = 31 * result + walkingPrecedence
        result = 31 * result + replayMode
        result = 31 * result + (expressionFrames?.contentHashCode() ?: 0)
        result = 31 * result + (sounds?.contentDeepHashCode() ?: 0)
        result = 31 * result + aBoolean691.hashCode()
        result = 31 * result + tweened.hashCode()
        result = 31 * result + useSounds.hashCode()
        result = 31 * result + (volumes?.contentHashCode() ?: 0)
        result = 31 * result + (primarySpeeds?.contentHashCode() ?: 0)
        result = 31 * result + (secondarySpeeds?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + extras.hashCode()
        return result
    }

    companion object {
        val EMPTY = AnimationDefinitionFull()
    }
}
