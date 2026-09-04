package content.area.morytania.braindeath_island

import WorldTest
import content.skill.prayer.PrayerConfigs
import content.skill.prayer.protectMelee
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Fever spiders bite through bare hands for heavy damage and always inflict disease. Protecting
 * from melee stops the bite but not the disease, matching the original's FeverSpiderScript.
 */
class FeverSpiderTest : WorldTest() {

    @Test
    fun `Protecting from melee stops the fever spider's bite`() {
        val player = createPlayer(Tile(2140, 5100))

        assertFalse(player.protectMelee(), "no protection prayer by default")

        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee")

        assertTrue(player.protectMelee(), "protect from melee should register")
    }

    @Test
    fun `Deflect melee also counts as melee protection`() {
        val player = createPlayer(Tile(2140, 5100))
        player[PrayerConfigs.PRAYERS] = "curses"
        player.addVarbit(PrayerConfigs.ACTIVE_CURSES, "deflect_melee")

        assertTrue(player.protectMelee(), "deflect melee should register")
    }
}
