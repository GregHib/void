package rs.dusk.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.ai.Curve.cosine
import rs.dusk.ai.Curve.exponential
import rs.dusk.ai.Curve.linear
import rs.dusk.ai.Curve.logistic
import rs.dusk.ai.Curve.logit
import rs.dusk.ai.Curve.sine
import rs.dusk.ai.Curve.smoothStep
import rs.dusk.ai.Curve.smootherStep
import kotlin.math.E

internal class CurveTest {

    @Test
    fun `Linear constant`() {
        assertEquals(0.2, linear(0.2, 1.0, 0.0))
        assertEquals(0.8, linear(0.8, 1.0, 0.0))
    }

    @Test
    fun `Sloped linear`() {
        assertEquals(0.1, linear(0.2, 2.0, 0.0))
        assertEquals(0.4, linear(0.8, 2.0, 0.0))
    }

    @Test
    fun `Offset linear`() {
        assertEquals(0.6, linear(0.2, 1.0, -0.4), 0.001)
        assertEquals(1.2, linear(0.8, 1.0, -0.4), 0.001)
    }

    @Test
    fun `Exponential curve`() {
        assertEquals(0.04, exponential(0.2, 2.0, 0.0), 0.001)
        assertEquals(0.36, exponential(0.6, 2.0, 0.0), 0.001)
        assertEquals(0.64, exponential(0.8, 2.0, 0.0), 0.001)
    }

    @Test
    fun `High exponent exponential`() {
        assertEquals(0.006, exponential(0.6, 10.0, 0.0), 0.0001)
        assertEquals(0.1, exponential(0.8, 10.0, 0.0), 0.01)
        assertEquals(0.59, exponential(0.95, 10.0, 0.0), 0.01)
    }

    @Test
    fun `Offset exponential`() {
        assertEquals(0.4, exponential(0.4, 2.0, 0.24), 0.001)
        assertEquals(0.6, exponential(0.6, 2.0, 0.24), 0.001)
        assertEquals(0.88, exponential(0.8, 2.0, 0.24), 0.01)
    }

    @Test
    fun `Sine curve`() {
        assertEquals(0.3, sine(0.2, 0.5, 0.0), 0.01)
        assertEquals(0.8, sine(0.6, 0.5, 0.0), 0.01)
        assertEquals(1.0, sine(1.0, 0.5, 0.0), 0.01)
    }

    @Test
    fun `Steep sine curve`() {
        assertEquals(0.58, sine(0.2, 1.0, 0.0), 0.01)
        assertEquals(1.0, sine(0.5, 1.0, 0.0), 0.01)
        assertEquals(0.58, sine(0.8, 1.0, 0.0), 0.01)
    }

    @Test
    fun `Offset sine curve`() {
        assertEquals(-0.04, sine(0.2, 0.5, -0.35), 0.01)
        assertEquals(0.459, sine(0.6, 0.5, -0.35), 0.01)
        assertEquals(0.6, sine(0.8, 0.5, -0.35), 0.01)
    }

    @Test
    fun `Cosine curve`() {
        assertEquals(0.048, cosine(0.2, 0.5, 0.0), 0.001)
        assertEquals(0.41, cosine(0.6, 0.5, 0.0), 0.01)
        assertEquals(0.69, cosine(0.8, 0.5, 0.0), 0.01)
    }

    @Test
    fun `Steep cosine curve`() {
        assertEquals(0.19, cosine(0.2, 1.0, 0.0), 0.01)
        assertEquals(0.69, cosine(0.4, 1.0, 0.0), 0.01)
        assertEquals(1.3, cosine(0.6, 1.0, 0.0), 0.1)
    }

    @Test
    fun `Offset cosine curve`() {
        assertEquals(0.25, cosine(0.2, 0.5, 0.2), 0.01)
        assertEquals(0.61, cosine(0.6, 0.5, 0.2), 0.01)
        assertEquals(0.89, cosine(0.8, 0.5, 0.2), 0.01)
    }

    @Test
    fun `Logistic curve`() {
        assertEquals(0.1, logistic(0.3, 1.0, 0.0), 0.01)
        assertEquals(0.5, logistic(0.5, 1.0, 0.0), 0.01)
        assertEquals(0.96, logistic(0.8, 1.0, 0.0), 0.01)
    }

    @Test
    fun `Steep logistic curve`() {
        assertEquals(0.0369, logistic(0.4, 3.0, 0.0), 0.0001)
        assertEquals(0.5, logistic(0.5, 3.0, 0.0), 0.01)
        assertEquals(0.99, logistic(0.7, 3.0, 0.0), 0.01)
    }

    @Test
    fun `Midpoint logistic curve`() {
        assertEquals(0.25, logistic(0.7, 1.0, 0.3), 0.01)
        assertEquals(0.5, logistic(0.8, 1.0, 0.3), 0.01)
        assertEquals(0.89, logistic(1.0, 1.0, 0.3), 0.01)
    }

    @Test
    fun `Logit curve`() {
        assertEquals(0.298, logit(0.1, E), 0.01)
        assertEquals(0.53, logit(0.6, E), 0.01)
        assertEquals(0.7, logit(0.9, E), 0.01)
    }

    @Test
    fun `Based logit curve`() {
        assertEquals(0.44, logit(0.2, 10.0), 0.01)
        assertEquals(0.48, logit(0.4, 10.0), 0.01)
        assertEquals(0.655, logit(0.98, 10.0), 0.001)
    }

    @Test
    fun `Smooth step curve`() {
        assertEquals(0.1, smoothStep(0.2), 0.01)
        assertEquals(0.5, smoothStep(0.5), 0.01)
        assertEquals(0.972, smoothStep(0.9), 0.001)
    }

    @Test
    fun `Smoother step curve`() {
        assertEquals(0.057, smootherStep(0.2), 0.001)
        assertEquals(0.32, smootherStep(0.4), 0.01)
        assertEquals(0.94, smootherStep(0.8), 0.01)
    }
}