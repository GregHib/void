package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition

data class RenderAnimationDefinition(
    override var id: Int = -1,
    var anInt3281: Int = 0,
    var primaryIdle: Int = -1,
    var primaryWalk: Int = -1,
    var anInt3262: Int = -1,
    var anInt3297: Int = -1,
    var anInt3269: Int = -1,
    var anInt3304: Int = -1,
    var run: Int = -1,
    var anInt3271: Int = -1,
    var anInt3270: Int = -1,
    var anInt3293: Int = -1,
    var anInt3261: Int = 0,
    var anInt3266: Int = 0,
    var anIntArrayArray3273: Array<IntArray?>? = null,
    var anIntArray3276: IntArray? = null,
    var anInt3258: Int = 0,
    var anInt3283: Int = 0,
    var anInt3278: Int = 0,
    var anInt3284: Int = 0,
    var anInt3250: Int = 0,
    var anInt3272: Int = 0,
    var anInt3289: Int = 0,
    var anInt3285: Int = 0,
    var anInt3256: Int = -1,
    var turning: Int = -1,
    var secondaryWalk: Int = -1,
    var walkBackwards: Int = -1,
    var sideStepLeft: Int = -1,
    var sideStepRight: Int = -1,
    var anInt3290: Int = -1,
    var anInt3292: Int = -1,
    var anInt3303: Int = -1,
    var anInt3275: Int = -1,
    var anInt3260: Int = -1,
    var anInt3282: Int = -1,
    var anInt3253: Int = -1,
    var anInt3298: Int = -1,
    var anInt3305: Int = -1,
    var anIntArray3294: IntArray? = null,
    var anIntArray3302: IntArray? = null,
    var aBoolean3267: Boolean = true,
    var anInt3263: Int = 0,
    var anInt3291: Int = 0,
    var anIntArray3255: IntArray? = null,
    var anIntArrayArray3249: Array<IntArray?>? = null,
) : Definition {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RenderAnimationDefinition

        if (id != other.id) return false
        if (anInt3281 != other.anInt3281) return false
        if (primaryIdle != other.primaryIdle) return false
        if (primaryWalk != other.primaryWalk) return false
        if (anInt3262 != other.anInt3262) return false
        if (anInt3297 != other.anInt3297) return false
        if (anInt3269 != other.anInt3269) return false
        if (anInt3304 != other.anInt3304) return false
        if (run != other.run) return false
        if (anInt3271 != other.anInt3271) return false
        if (anInt3270 != other.anInt3270) return false
        if (anInt3293 != other.anInt3293) return false
        if (anInt3261 != other.anInt3261) return false
        if (anInt3266 != other.anInt3266) return false
        if (anIntArrayArray3273 != null) {
            if (other.anIntArrayArray3273 == null) return false
            if (!anIntArrayArray3273.contentDeepEquals(other.anIntArrayArray3273)) return false
        } else if (other.anIntArrayArray3273 != null) {
            return false
        }
        if (anIntArray3276 != null) {
            if (other.anIntArray3276 == null) return false
            if (!anIntArray3276.contentEquals(other.anIntArray3276)) return false
        } else if (other.anIntArray3276 != null) {
            return false
        }
        if (anInt3258 != other.anInt3258) return false
        if (anInt3283 != other.anInt3283) return false
        if (anInt3278 != other.anInt3278) return false
        if (anInt3284 != other.anInt3284) return false
        if (anInt3250 != other.anInt3250) return false
        if (anInt3272 != other.anInt3272) return false
        if (anInt3289 != other.anInt3289) return false
        if (anInt3285 != other.anInt3285) return false
        if (anInt3256 != other.anInt3256) return false
        if (turning != other.turning) return false
        if (secondaryWalk != other.secondaryWalk) return false
        if (walkBackwards != other.walkBackwards) return false
        if (sideStepLeft != other.sideStepLeft) return false
        if (sideStepRight != other.sideStepRight) return false
        if (anInt3290 != other.anInt3290) return false
        if (anInt3292 != other.anInt3292) return false
        if (anInt3303 != other.anInt3303) return false
        if (anInt3275 != other.anInt3275) return false
        if (anInt3260 != other.anInt3260) return false
        if (anInt3282 != other.anInt3282) return false
        if (anInt3253 != other.anInt3253) return false
        if (anInt3298 != other.anInt3298) return false
        if (anInt3305 != other.anInt3305) return false
        if (anIntArray3294 != null) {
            if (other.anIntArray3294 == null) return false
            if (!anIntArray3294.contentEquals(other.anIntArray3294)) return false
        } else if (other.anIntArray3294 != null) {
            return false
        }
        if (anIntArray3302 != null) {
            if (other.anIntArray3302 == null) return false
            if (!anIntArray3302.contentEquals(other.anIntArray3302)) return false
        } else if (other.anIntArray3302 != null) {
            return false
        }
        if (aBoolean3267 != other.aBoolean3267) return false
        if (anInt3263 != other.anInt3263) return false
        if (anInt3291 != other.anInt3291) return false
        if (anIntArray3255 != null) {
            if (other.anIntArray3255 == null) return false
            if (!anIntArray3255.contentEquals(other.anIntArray3255)) return false
        } else if (other.anIntArray3255 != null) {
            return false
        }
        if (anIntArrayArray3249 != null) {
            if (other.anIntArrayArray3249 == null) return false
            if (!anIntArrayArray3249.contentDeepEquals(other.anIntArrayArray3249)) return false
        } else if (other.anIntArrayArray3249 != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + anInt3281
        result = 31 * result + primaryIdle
        result = 31 * result + primaryWalk
        result = 31 * result + anInt3262
        result = 31 * result + anInt3297
        result = 31 * result + anInt3269
        result = 31 * result + anInt3304
        result = 31 * result + run
        result = 31 * result + anInt3271
        result = 31 * result + anInt3270
        result = 31 * result + anInt3293
        result = 31 * result + anInt3261
        result = 31 * result + anInt3266
        result = 31 * result + (anIntArrayArray3273?.contentDeepHashCode() ?: 0)
        result = 31 * result + (anIntArray3276?.contentHashCode() ?: 0)
        result = 31 * result + anInt3258
        result = 31 * result + anInt3283
        result = 31 * result + anInt3278
        result = 31 * result + anInt3284
        result = 31 * result + anInt3250
        result = 31 * result + anInt3272
        result = 31 * result + anInt3289
        result = 31 * result + anInt3285
        result = 31 * result + anInt3256
        result = 31 * result + turning
        result = 31 * result + secondaryWalk
        result = 31 * result + walkBackwards
        result = 31 * result + sideStepLeft
        result = 31 * result + sideStepRight
        result = 31 * result + anInt3290
        result = 31 * result + anInt3292
        result = 31 * result + anInt3303
        result = 31 * result + anInt3275
        result = 31 * result + anInt3260
        result = 31 * result + anInt3282
        result = 31 * result + anInt3253
        result = 31 * result + anInt3298
        result = 31 * result + anInt3305
        result = 31 * result + (anIntArray3294?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray3302?.contentHashCode() ?: 0)
        result = 31 * result + aBoolean3267.hashCode()
        result = 31 * result + anInt3263
        result = 31 * result + anInt3291
        result = 31 * result + (anIntArray3255?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArrayArray3249?.contentDeepHashCode() ?: 0)
        return result
    }
}
