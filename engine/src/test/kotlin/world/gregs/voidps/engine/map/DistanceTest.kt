package world.gregs.voidps.engine.map

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Distance.levenshtein

internal class DistanceTest {
    @Test
    fun `Test Levenshtein distance`() {
        assertEquals(0, levenshtein("kitten", "kitten"))
        assertEquals(1, levenshtein("kitten", "sitten"))
        assertEquals(2, levenshtein("kitten", "sittes"))
        assertEquals(3, levenshtein("kitten", "sityteng"))
        assertEquals(4, levenshtein("kitten", "sittYing"))
        assertEquals(17, levenshtein("kitten", "kittenaaaaaaaaaaaaaaaaa"))
        assertEquals(17, levenshtein("kittenaaaaaaaaaaaaaaaaa", "kitten"))
    }
}