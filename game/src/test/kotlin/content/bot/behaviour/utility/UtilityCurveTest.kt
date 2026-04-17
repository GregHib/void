package content.bot.behaviour.utility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

class UtilityCurveTest {

    private val tolerance = 1e-6

    @Test
    fun `Linear maps range to 0-1`() {
        val curve = UtilityCurve.Linear(min = 0.0, max = 10.0)
        assertEquals(0.0, curve.score(0.0), tolerance)
        assertEquals(0.5, curve.score(5.0), tolerance)
        assertEquals(1.0, curve.score(10.0), tolerance)
    }

    @Test
    fun `Linear clamps outside range`() {
        val curve = UtilityCurve.Linear(min = 0.0, max = 1.0)
        assertEquals(0.0, curve.score(-5.0), tolerance)
        assertEquals(1.0, curve.score(5.0), tolerance)
    }

    @Test
    fun `Linear with equal min max returns zero`() {
        val curve = UtilityCurve.Linear(min = 1.0, max = 1.0)
        assertEquals(0.0, curve.score(1.0), tolerance)
    }

    @Test
    fun `Exponential matches base power`() {
        val curve = UtilityCurve.Exponential(base = 0.9)
        assertEquals(1.0, curve.score(0.0), tolerance)
        assertEquals(0.9.pow(1.0), curve.score(1.0), tolerance)
        assertEquals(0.9.pow(10.0), curve.score(10.0), tolerance)
    }

    @Test
    fun `Sine eases 0 to 1 over unit interval`() {
        assertEquals(0.0, UtilityCurve.Sine.score(0.0), tolerance)
        assertEquals(0.5 * (1.0 - cos(PI * 0.25)), UtilityCurve.Sine.score(0.25), tolerance)
        assertEquals(0.5, UtilityCurve.Sine.score(0.5), tolerance)
        assertEquals(1.0, UtilityCurve.Sine.score(1.0), tolerance)
    }

    @Test
    fun `Cosine eases 1 to 0 over unit interval`() {
        assertEquals(1.0, UtilityCurve.Cosine.score(0.0), tolerance)
        assertEquals(0.5, UtilityCurve.Cosine.score(0.5), tolerance)
        assertEquals(0.0, UtilityCurve.Cosine.score(1.0), tolerance)
    }

    @Test
    fun `Logistic outputs sigmoid centered on midpoint`() {
        val curve = UtilityCurve.Logistic(midpoint = 0.5, steepness = 8.0)
        assertEquals(0.5, curve.score(0.5), tolerance)
        val expectedAtZero = 1.0 / (1.0 + exp(8.0 * 0.5))
        assertEquals(expectedAtZero, curve.score(0.0), tolerance)
    }

    @Test
    fun `Logit is inverse-style of logistic at midpoint`() {
        val curve = UtilityCurve.Logit(midpoint = 0.5, steepness = 1.0)
        assertEquals(0.5, curve.score(0.5), tolerance)
        val expected = 0.5 + ln(0.7 / 0.3) / 1.0
        assertEquals(expected, curve.score(0.7), tolerance)
    }

    @Test
    fun `SmoothStep matches 3t squared minus 2t cubed`() {
        val curve = UtilityCurve.SmoothStep()
        assertEquals(0.0, curve.score(0.0), tolerance)
        assertEquals(0.5, curve.score(0.5), tolerance)
        assertEquals(1.0, curve.score(1.0), tolerance)
        val t = 0.25
        val expected = t * t * (3.0 - 2.0 * t)
        assertEquals(expected, curve.score(0.25), tolerance)
    }

    @Test
    fun `SmootherStep matches 6t5 minus 15t4 plus 10t3`() {
        val curve = UtilityCurve.SmootherStep()
        assertEquals(0.0, curve.score(0.0), tolerance)
        assertEquals(0.5, curve.score(0.5), tolerance)
        assertEquals(1.0, curve.score(1.0), tolerance)
        val t = 0.3
        val expected = t * t * t * (t * (t * 6.0 - 15.0) + 10.0)
        assertEquals(expected, curve.score(0.3), tolerance)
    }
}
