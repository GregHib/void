package content.skill.summoning

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.add
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Covers [castFamiliarSpecial]'s validation + resource-consumption chain: a successful cast spends
 * exactly one scroll and the scroll's points, while a blocked cast (no scroll, too few points,
 * soft-failed effect, or on cooldown) spends nothing. Spirit wolf is used as the fixture; its scroll
 * is `howl_scroll` (cost 3).
 */
class FamiliarSpecialMoveTest : WorldTest() {

    private fun summonSpiritWolf(): Player {
        val player = createPlayer(Tile(2523, 3056))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("spirit_wolf_familiar"), restart = false)
        tick(2) // let the summon queue assign the follower
        player.set("summoning_special_points_remaining", 60)
        player.inventory.transaction { add("howl_scroll", 5) }
        return player
    }

    @Test
    fun `Successful cast spends one scroll and the points`() {
        val player = summonSpiritWolf()

        player.castFamiliarSpecial { true }

        assertEquals(4, player.inventory.count("howl_scroll"))
        assertEquals(57, player.get("summoning_special_points_remaining", 0))
    }

    @Test
    fun `Soft-failed effect spends nothing`() {
        val player = summonSpiritWolf()

        player.castFamiliarSpecial { false }

        assertEquals(5, player.inventory.count("howl_scroll"))
        assertEquals(60, player.get("summoning_special_points_remaining", 0))
    }

    @Test
    fun `Cast blocked without enough points`() {
        val player = summonSpiritWolf()
        player.set("summoning_special_points_remaining", 2)

        player.castFamiliarSpecial { true }

        assertEquals(5, player.inventory.count("howl_scroll"))
        assertEquals(2, player.get("summoning_special_points_remaining", 0))
    }

    @Test
    fun `Cast blocked without a scroll`() {
        val player = summonSpiritWolf()
        player.inventory.transaction { remove("howl_scroll", 5) }

        var ran = false
        player.castFamiliarSpecial {
            ran = true
            true
        }

        assertFalse(ran)
        assertEquals(60, player.get("summoning_special_points_remaining", 0))
    }

    @Test
    fun `Second cast is blocked by the cooldown`() {
        val player = summonSpiritWolf()

        player.castFamiliarSpecial { true }
        player.castFamiliarSpecial { true }

        // Only the first cast was allowed through the 3-tick cooldown.
        assertEquals(4, player.inventory.count("howl_scroll"))
        assertEquals(57, player.get("summoning_special_points_remaining", 0))
    }
}
