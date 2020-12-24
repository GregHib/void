package rs.dusk.ai

import kotlin.math.*

object Curve {

    /**
     * @param slope 0..100
     * @param offset -1..1
     */
    fun linear(value: Double, slope: Double = 1.0, offset: Double = 0.0): Double {
        return (value / slope) - offset
    }

    /**
     * @param exponent 0.001..100
     * @param offset 0..1
     */
    fun exponential(value: Double, exponent: Double = 2.0, offset: Double = 0.0): Double {
        return 1.0 - ((1.0 - value.pow(exponent)) / 1.0) + offset
    }

    /**
     * @param steepness 0..1
     * @param offset -1..1
     */
    fun sine(value: Double, steepness: Double = 0.5, offset: Double = 0.0): Double {
        return sin(value * PI * steepness) + offset
    }

    /**
     * @param steepness 0..1
     * @param offset -1..1
     */
    fun cosine(value: Double, steepness: Double = 0.5, offset: Double = 0.0): Double {
        return 1 - cos(value * PI * steepness) + offset
    }

    /**
     * Note: normalized
     * @param steepness 0..1
     * @param midpoint -1..1
     */
    fun logistic(value: Double, steepness: Double = 1.0, midpoint: Double = 0.0): Double {
        return 1 / (1 + E.pow(-steepness * (4 * E * (value - midpoint) - (2 * E))))
    }

    /**
     * Note: normalized
     * @param logBase 0.01..100
     */
    fun logit(value: Double, logBase: Double = E): Double {
        return (log(value / (1 - value), logBase) + (2 * E)) / (4 * E)
    }

    fun smoothStep(value: Double): Double {
        return value * value * (3 - 2 * value)
    }

    fun smootherStep(value: Double): Double {
        return value * value * value * (value * (6 * value - 15) + 10)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(linear(0.8, 2.4, 0.0))
    }
}