package content.entity.player.stat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BonusExperienceTest {

    @Test
    fun `Multiplier starts at maximum`() {
        assertEquals(2.7, BonusExperience.multiplier(0))
        assertEquals(2.7, BonusExperience.multiplier(1))
        assertEquals(2.7, BonusExperience.multiplier(30))
    }

    @Test
    fun `Multiplier decreases every 30 minutes`() {
        assertEquals(2.55, BonusExperience.multiplier(31))
        assertEquals(2.55, BonusExperience.multiplier(60))
        assertEquals(2.4, BonusExperience.multiplier(61))
        assertEquals(2.0, BonusExperience.multiplier(151))
    }

    @Test
    fun `Multiplier caps at minimum`() {
        assertEquals(1.1, BonusExperience.multiplier(601))
        assertEquals(1.1, BonusExperience.multiplier(10_000))
    }
}
