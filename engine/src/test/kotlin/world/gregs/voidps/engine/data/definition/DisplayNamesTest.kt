package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random

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

    @Test
    fun `Suggestions are valid unique and available`() {
        val taken = setOf("Bob12", "Bob123")
        val names = DisplayNames.suggestions("bob.smith", 6, { it in taken }, Random(42))

        assertEquals(6, names.size)
        assertEquals(6, names.toSet().size)
        for (name in names) {
            assertTrue(DisplayNames.valid(name), name)
            assertFalse(name in taken, name)
        }
        assertTrue(names.any { it.startsWith("Bobsmith") && it.last().isDigit() }, names.toString())
        assertTrue(names.any { it.startsWith("Bobsmith") && it.last().isLetter() }, names.toString())
        assertTrue(names.any { it.endsWith("Bobsmith") }, names.toString())
    }

    @Test
    fun `Suggestions strip trailing numbers from the base`() {
        val names = DisplayNames.suggestions("seth2", 4, { false }, Random(7))

        assertTrue(names.any { it.startsWith("Seth") && it != "Seth2" }, names.toString())
        assertFalse(names.any { it.startsWith("Seth2") }, names.toString())
    }

    @Test
    fun `Same seed gives the same suggestions`() {
        assertEquals(DisplayNames.suggestions("bob", 6, { false }, Random(3)), DisplayNames.suggestions("bob", 6, { false }, Random(3)))
    }

    @Test
    fun `Suggestions fit within the maximum length`() {
        val names = DisplayNames.suggestions("averyveryverylongname", 6, { false }, Random(1))

        assertEquals(6, names.size)
        for (name in names) {
            assertTrue(name.length <= DisplayNames.MAX_LENGTH, name)
        }
    }
}
