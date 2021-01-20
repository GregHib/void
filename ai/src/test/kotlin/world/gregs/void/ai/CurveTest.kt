package world.gregs.void.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.E

internal class CurveTest {

    @Test
    fun `Linear constant`() {
        assertEquals(0.2, 0.2.linear(1.0, 0.0))
        assertEquals(0.8, 0.8.linear(1.0, 0.0))
    }

    @Test
    fun `Sloped linear`() {
        assertEquals(0.1, 0.2.linear(2.0, 0.0))
        assertEquals(0.4, 0.8.linear(2.0, 0.0))
    }

    @Test
    fun `Offset linear`() {
        assertEquals(0.6, 0.2.linear(1.0, -0.4), 0.001)
        assertEquals(1.2, 0.8.linear(1.0, -0.4), 0.001)
    }

    @Test
    fun `Exponential curve`() {
        assertEquals(0.04, 0.2.exponential(2.0, 0.0), 0.001)
        assertEquals(0.36, 0.6.exponential(2.0, 0.0), 0.001)
        assertEquals(0.64, 0.8.exponential(2.0, 0.0), 0.001)
    }

    @Test
    fun `High exponent exponential`() {
        assertEquals(0.006, 0.6.exponential(10.0, 0.0), 0.0001)
        assertEquals(0.1, 0.8.exponential(10.0, 0.0), 0.01)
        assertEquals(0.59, 0.95.exponential(10.0, 0.0), 0.01)
    }

    @Test
    fun `Offset exponential`() {
        assertEquals(0.4, 0.4.exponential(2.0, 0.24), 0.001)
        assertEquals(0.6, 0.6.exponential(2.0, 0.24), 0.001)
        assertEquals(0.88, 0.8.exponential(2.0, 0.24), 0.01)
    }

    @Test
    fun `Sine curve`() {
        assertEquals(0.3, 0.2.sine(0.5, 0.0), 0.01)
        assertEquals(0.8, 0.6.sine(0.5, 0.0), 0.01)
        assertEquals(1.0, 1.0.sine(0.5, 0.0), 0.01)
    }

    @Test
    fun `Steep sine curve`() {
        assertEquals(0.58, 0.2.sine(1.0, 0.0), 0.01)
        assertEquals(1.0, 0.5.sine(1.0, 0.0), 0.01)
        assertEquals(0.58, 0.8.sine(1.0, 0.0), 0.01)
    }

    @Test
    fun `Offset sine curve`() {
        assertEquals(-0.04, 0.2.sine(0.5, -0.35), 0.01)
        assertEquals(0.459, 0.6.sine(0.5, -0.35), 0.01)
        assertEquals(0.6, 0.8.sine(0.5, -0.35), 0.01)
    }

    @Test
    fun `Cosine curve`() {
        assertEquals(0.048, 0.2.cosine(0.5, 0.0), 0.001)
        assertEquals(0.41, 0.6.cosine(0.5, 0.0), 0.01)
        assertEquals(0.69, 0.8.cosine(0.5, 0.0), 0.01)
    }

    @Test
    fun `Steep cosine curve`() {
        assertEquals(0.19, 0.2.cosine(1.0, 0.0), 0.01)
        assertEquals(0.69, 0.4.cosine(1.0, 0.0), 0.01)
        assertEquals(1.3, 0.6.cosine(1.0, 0.0), 0.1)
    }

    @Test
    fun `Offset cosine curve`() {
        assertEquals(0.25, 0.2.cosine(0.5, 0.2), 0.01)
        assertEquals(0.61, 0.6.cosine(0.5, 0.2), 0.01)
        assertEquals(0.89, 0.8.cosine(0.5, 0.2), 0.01)
    }

    @Test
    fun `Logistic curve`() {
        assertEquals(0.1, 0.3.logistic(1.0, 0.0), 0.01)
        assertEquals(0.5, 0.5.logistic(1.0, 0.0), 0.01)
        assertEquals(0.96, 0.8.logistic(1.0, 0.0), 0.01)
    }

    @Test
    fun `Steep logistic curve`() {
        assertEquals(0.0369, 0.4.logistic(3.0, 0.0), 0.0001)
        assertEquals(0.5, 0.5.logistic(3.0, 0.0), 0.01)
        assertEquals(0.99, 0.7.logistic(3.0, 0.0), 0.01)
    }

    @Test
    fun `Midpoint logistic curve`() {
        assertEquals(0.25, 0.7.logistic(1.0, 0.3), 0.01)
        assertEquals(0.5, 0.8.logistic(1.0, 0.3), 0.01)
        assertEquals(0.89, 1.0.logistic(1.0, 0.3), 0.01)
    }

    @Test
    fun `Logit curve`() {
        assertEquals(0.298, 0.1.logit(E), 0.01)
        assertEquals(0.53, 0.6.logit(E), 0.01)
        assertEquals(0.7, 0.9.logit(E), 0.01)
    }

    @Test
    fun `Based logit curve`() {
        assertEquals(0.44, 0.2.logit(10.0), 0.01)
        assertEquals(0.48, 0.4.logit(10.0), 0.01)
        assertEquals(0.655, 0.98.logit(10.0), 0.001)
    }

    @Test
    fun `Smooth step curve`() {
        assertEquals(0.1, 0.2.smoothStep(), 0.01)
        assertEquals(0.5, 0.5.smoothStep(), 0.01)
        assertEquals(0.972, 0.9.smoothStep(), 0.001)
    }

    @Test
    fun `Smoother step curve`() {
        assertEquals(0.057, 0.2.smootherStep(), 0.001)
        assertEquals(0.32, 0.4.smootherStep(), 0.01)
        assertEquals(0.94, 0.8.smootherStep(), 0.01)
    }
}