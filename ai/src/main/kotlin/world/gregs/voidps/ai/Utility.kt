package world.gregs.voidps.ai

fun Double.scale(min: Double, max: Double): Double {
    return (coerceIn(min, max) - min) / (max - min)
}

fun Int.scale(min: Int, max: Int): Int {
    return (coerceIn(min, max) - min) / (max - min)
}

fun Double.inverse() = 1.0 - this

fun Double.toBoolean() = this == 1.0

fun Boolean.toDouble() = if (this) 1.0 else 0.0

fun combine(vararg considerations: Double): Double {
    val compensationFactor = 1.0 - (1.0 / considerations.size)
    var result = 1.0
    for (consideration in considerations) {
        var finalScore = consideration
        val modification = (1.0 - finalScore) * compensationFactor
        finalScore += (modification * finalScore)
        result *= finalScore
        if (result == 0.0) {
            return result
        }
    }
    return result
}