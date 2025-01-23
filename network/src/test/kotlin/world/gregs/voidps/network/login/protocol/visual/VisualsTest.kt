package world.gregs.voidps.network.login.protocol.visual

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VisualsTest {

    private lateinit var visuals: Visuals

    @BeforeEach
    fun setup() {
        visuals = object : Visuals() {
        }
    }

    @Test
    fun `Not flagged`() {
        assertFalse(visuals.flagged(0x100))
    }

    @Test
    fun `Set flagged`() {
        // When
        visuals.flag(0x100)
        // Then
        assertEquals(0x100, visuals.flag)
        assertTrue(visuals.flagged(0x100))
    }

    @Test
    fun `Clear flag`() {
        // When
        visuals.flag(0x100)
        visuals.reset()
        // Then
        assertFalse(visuals.flagged(0x100))
    }
}