package world.gregs.voidps.world.interact.entity.death

import content.entity.death.Distribution
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class DistributionTest {

    private lateinit var random: Random

    @BeforeEach
    fun setup() {
        random = mockk(relaxed = true)
    }

    @Test
    fun `Sample a single value`() {
        val dist = Distribution(arrayOf("one"), random = random)
        assertEquals("one", dist.sample())
    }

    @Test
    fun `Sample two values evenly`() {
        val dist = Distribution(arrayOf("one", "two"), random = random)
        every { random.nextDouble() } returns 0.4
        assertEquals("one", dist.sample())
        every { random.nextDouble() } returns 0.6
        assertEquals("two", dist.sample())
    }

    @Test
    fun `Sample duplicate values weighted`() {
        val dist = Distribution(arrayOf("one", "two", "one"), random = random)
        every { random.nextDouble() } returns 0.3
        assertEquals("two", dist.sample())
        every { random.nextDouble() } returns 0.4
        assertEquals("one", dist.sample())
        every { random.nextDouble() } returns 0.8
        assertEquals("one", dist.sample())
    }

    @Test
    fun `Invert weighted values`() {
        val dist = Distribution(arrayOf("one", "two", "one"), invert = true, random = random)
        every { random.nextDouble() } returns 0.3
        assertEquals("two", dist.sample())
        every { random.nextDouble() } returns 0.6
        assertEquals("two", dist.sample())
        every { random.nextDouble() } returns 0.8
        assertEquals("one", dist.sample())
    }

    @Test
    fun `Provide weighted values greater than 1`() {
        val dist = Distribution(listOf(
            "one" to 1.0,
            "two" to 3.0,
            "three" to 5.0
        ), random = random)
        every { random.nextDouble() } returns 0.1
        assertEquals("one", dist.sample())
        every { random.nextDouble() } returns 0.4
        assertEquals("two", dist.sample())
        every { random.nextDouble() } returns 0.6
        assertEquals("three", dist.sample())
    }

    @Test
    fun `Invert weighted values greater than 1`() {
        val dist = Distribution(listOf(
            "one" to 1.0,
            "two" to 3.0,
            "three" to 5.0
        ), invert = true, random = random)
        every { random.nextDouble() } returns 0.5
        assertEquals("one", dist.sample())
        every { random.nextDouble() } returns 0.8
        assertEquals("two", dist.sample())
        every { random.nextDouble() } returns 0.9
        assertEquals("three", dist.sample())
    }

    @Test
    fun `Zero and negative weights are ignored`() {
        val dist = Distribution(listOf(
            "one" to -1.0,
            "two" to 0.0,
            "three" to 1.0
        ), random = random)
        every { random.nextDouble() } returns 0.5
        assertEquals("three", dist.sample())
    }

    @Test
    fun `Inclusive random defaults to last item`() {
        val dist = Distribution(arrayOf("one"), random = random)
        every { random.nextDouble() } returns 0.0
        assertEquals("one", dist.sample())
        every { random.nextDouble() } returns 1.0
        assertEquals("one", dist.sample())
    }
}