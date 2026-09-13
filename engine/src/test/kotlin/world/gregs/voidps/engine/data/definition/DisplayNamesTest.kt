package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class DisplayNamesTest {

    @ParameterizedTest
    @ValueSource(strings = ["Bob", "bob 123", "A", "Twelve chars"])
    fun `Valid names`(name: String) {
        assertTrue(DisplayNames.valid(name))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " Bob", "Bob ", "Bob  Smith", "Bob_Smith", "Thirteen chars", "bob@x"])
    fun `Invalid names`(name: String) {
        assertFalse(DisplayNames.valid(name))
    }

    @ParameterizedTest
    @CsvSource(
        "bob, Bob",
        "bob.smith, Bob smith",
        "first_last+tag, First last t",
        "averyveryverylongname, Averyveryver",
        "'', Player",
        "..., Player",
        "'  spaced  out ', Spaced out",
    )
    fun `Sanitise email local parts`(local: String, expected: String) {
        assertEquals(expected, DisplayNames.sanitise(local))
    }

    @Test
    fun `Unique returns base when free`() {
        assertEquals("Bob", DisplayNames.unique("Bob") { false })
    }

    @Test
    fun `Unique appends numbers when taken`() {
        val taken = setOf("Bob", "Bob2", "Bob3")
        assertEquals("Bob4", DisplayNames.unique("Bob") { it in taken })
    }

    @Test
    fun `Unique truncates to fit suffix`() {
        val taken = setOf("Averyveryver", "Averyveryve2")
        assertEquals("Averyveryve3", DisplayNames.unique("Averyveryver") { it in taken })
    }

    @Test
    fun `Unique falls back to random suffix`() {
        val name = DisplayNames.unique("Bob") { !it.matches(Regex("Bob\\d{6}")) }
        assertTrue(name.matches(Regex("Bob\\d{6}")))
    }
}
