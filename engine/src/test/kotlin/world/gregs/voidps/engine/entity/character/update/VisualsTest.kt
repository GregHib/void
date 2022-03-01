package world.gregs.voidps.engine.entity.character.update

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("UNCHECKED_CAST")
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
        visuals.reset(mockk())
        // Then
        assertFalse(visuals.flagged(0x100))
    }
}