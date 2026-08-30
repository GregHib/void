package content.area.morytania.braindeath_island

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.type.Tile
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Swabs guard the blindweed patch and attack anyone who comes near, until they are either
 * intimidated into backing off or the quest is finished and there is nothing left to guard.
 */
class ZombieSwabTest : WorldTest() {

    @Test
    fun `Zombie swabs attack a nearby player during the quest`() {
        createPlayer(Tile(2152, 5074))
        val swab = createNPC("zombie_swab", Tile(2152, 5073))

        tickIf(limit = 20) { swab.mode !is CombatMovement }

        assertTrue(swab.mode is CombatMovement, "a swab should attack a player who walks up to it")
    }

    @Test
    fun `An intimidated zombie swab stops attacking`() {
        createPlayer(Tile(2152, 5074)) { it["rum_deal_swab_a"] = 1 }
        val swab = createNPC("zombie_swab", Tile(2152, 5073))

        tick(20)

        assertFalse(swab.mode is CombatMovement, "a swab that has been heckled should back off")
    }

    @Test
    fun `Zombie swabs ignore a player who has completed Rum Deal`() {
        createPlayer(Tile(2152, 5074)) { it["rum_deal"] = "completed" }
        val swab = createNPC("zombie_swab", Tile(2152, 5073))

        tick(20)

        assertFalse(swab.mode is CombatMovement, "a swab should leave a player who finished the quest alone")
    }
}
