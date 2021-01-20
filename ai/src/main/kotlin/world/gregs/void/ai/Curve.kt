package world.gregs.void.ai

import kotlin.math.*

/**
 * @param slope 0..100
 * @param offset -1..1
 */
fun Double.linear(slope: Double = 1.0, offset: Double = 0.0) = (this / slope) - offset

/**
 * @param exponent 0.001..100
 * @param offset 0..1
 */
fun Double.exponential(exponent: Double = 2.0, offset: Double = 0.0): Double {
    return 1.0 - ((1.0 - pow(exponent)) / 1.0) + offset
}

/**
 * @param steepness 0..1
 * @param offset -1..1
 */
fun Double.sine(steepness: Double = 0.5, offset: Double = 0.0): Double {
    return sin(this * PI * steepness) + offset
}

/**
 * @param steepness 0..1
 * @param offset -1..1
 */
fun Double.cosine(steepness: Double = 0.5, offset: Double = 0.0): Double {
    return 1 - cos(this * PI * steepness) + offset
}

/**
 * Note: normalized
 * @param steepness 0..1
 * @param midpoint -1..1
 */
fun Double.logistic(steepness: Double = 1.0, midpoint: Double = 0.0): Double {
    return 1 / (1 + E.pow(-steepness * (4 * E * (this - midpoint) - (2 * E))))
}

/**
 * Note: normalized
 * @param logBase 0.01..100
 */
fun Double.logit(logBase: Double = E) = (log(this / (1 - this), logBase) + (2 * E)) / (4 * E)

fun Double.smoothStep() = this * this * (3 - 2 * this)

fun Double.smootherStep() = this * this * this * (this * (6 * this - 15) + 10)