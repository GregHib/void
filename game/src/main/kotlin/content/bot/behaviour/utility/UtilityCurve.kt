package content.bot.behaviour.utility

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

sealed class UtilityCurve {
    abstract fun score(x: Double): Double

    data class Linear(val min: Double = 0.0, val max: Double = 1.0) : UtilityCurve() {
        override fun score(x: Double): Double {
            if (max == min) return 0.0
            val t = (x - min) / (max - min)
            return t.coerceIn(0.0, 1.0)
        }
    }

    data class Exponential(val base: Double) : UtilityCurve() {
        override fun score(x: Double): Double = base.pow(x).coerceIn(0.0, 1.0)
    }

    object Sine : UtilityCurve() {
        override fun score(x: Double): Double {
            val t = x.coerceIn(0.0, 1.0)
            return 0.5 * (1.0 - cos(PI * t))
        }
    }

    object Cosine : UtilityCurve() {
        override fun score(x: Double): Double {
            val t = x.coerceIn(0.0, 1.0)
            return 0.5 * (1.0 + cos(PI * t))
        }
    }

    data class Logistic(val midpoint: Double = 0.5, val steepness: Double = 1.0) : UtilityCurve() {
        override fun score(x: Double): Double = 1.0 / (1.0 + exp(-steepness * (x - midpoint)))
    }

    data class Logit(val midpoint: Double = 0.5, val steepness: Double = 1.0) : UtilityCurve() {
        override fun score(x: Double): Double {
            val t = x.coerceIn(EPSILON, 1.0 - EPSILON)
            return midpoint + ln(t / (1.0 - t)) / steepness
        }
    }

    data class SmoothStep(val min: Double = 0.0, val max: Double = 1.0) : UtilityCurve() {
        override fun score(x: Double): Double {
            if (max == min) return 0.0
            val t = ((x - min) / (max - min)).coerceIn(0.0, 1.0)
            return t * t * (3.0 - 2.0 * t)
        }
    }

    data class SmootherStep(val min: Double = 0.0, val max: Double = 1.0) : UtilityCurve() {
        override fun score(x: Double): Double {
            if (max == min) return 0.0
            val t = ((x - min) / (max - min)).coerceIn(0.0, 1.0)
            return t * t * t * (t * (t * 6.0 - 15.0) + 10.0)
        }
    }

    companion object {
        private const val EPSILON = 1e-9
    }
}
