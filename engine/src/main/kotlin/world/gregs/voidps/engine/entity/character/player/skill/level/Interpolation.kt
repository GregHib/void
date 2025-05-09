package world.gregs.voidps.engine.entity.character.player.skill.level

object Interpolation {
    /**
     * Interpolates between two known points
     * https://en.wikipedia.org/wiki/Linear_interpolation
     * @param x Value between [x1] - [x2]
     * @return interpolated value between [y1] and [y2] relative to the position of [x] between [x1] and [x2]
     */
    fun interpolate(x: Int, y1: Int, y2: Int, x1: Int, x2: Int): Int {
        return (y1 * (x2 - x) + y2 * (x - x1)) / (x2 - x1)
    }

    fun interpolate(start: Double, end: Double, fraction: Double): Double {
        val result = start + fraction * (end - start)
        return kotlin.math.round(result * 10) / 10.0
    }

    fun lerp(value: Int, inRange: IntRange, result: IntRange): Int {
        return interpolate(value, result.first, result.last, inRange.first, inRange.last)
    }

}