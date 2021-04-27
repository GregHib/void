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