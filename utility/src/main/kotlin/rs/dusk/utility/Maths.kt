package rs.dusk.utility

object Maths {
    /**
     * Interpolates between two known points
     * https://en.wikipedia.org/wiki/Linear_interpolation
     */
    fun interpolate(x: Int, y1: Int, y2: Int, x1: Int, x2: Int): Int {
        return (y1 * (x2 - x) + y2 * (x - x1)) / (x2 - x1)
    }
}