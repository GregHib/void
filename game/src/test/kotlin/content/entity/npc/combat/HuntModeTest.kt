package content.entity.npc.combat

import content.entity.combat.inCombat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.player.combatLevel
import WorldTest
import kotlin.test.assertEquals

internal class HuntModeTest : WorldTest() {

    @BeforeEach
    fun setup() {
        npcs.clear()
        players.clear()
    }

    @Test
    fun `Cowardly attack low level player when in range`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_spider", emptyTile.addY(2))
        assertFalse(player.inCombat)

        player.walkTo(emptyTile.addY(1))
        tick(6)

        assertTrue(npc.mode is CombatMovement)
        assertTrue(player.inCombat)
    }

    @Test
    fun `Cowardly doesn't attack high level player when in range`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_spider", emptyTile.addY(1))
        player.combatLevel = 5
        assertFalse(player.inCombat)

        tick(6)

        assertFalse(npc.mode is CombatMovement)
        assertFalse(player.inCombat)
    }

    @Test
    fun `Can't interact without line of sight`() {
        val npc = createNPC("ash_cleaner", emptyTile)
        createObject("crate", emptyTile.addY(2))
        createFloorItem("ashes", emptyTile.addY(3))

        tick(4)

        assertEquals(emptyTile, npc.tile)
    }

    @Test
    fun `Will try to interact with line of sight`() {
        val npc = createNPC("ash_cleaner", emptyTile)
        createObject("15516", emptyTile.addY(2)) // fence
        createFloorItem("ashes", emptyTile.addY(3))

        tick(6)

        assertEquals(emptyTile.addY(1), npc.tile)
    }

    @Test
    fun `Interact with floor item`() {
        val npc = createNPC("ash_cleaner", emptyTile)
        createFloorItem("ashes", emptyTile.addY(3))

        tick(9)

        assertEquals(emptyTile.addY(3), npc.tile)
        assertTrue(floorItems[emptyTile.addY(3)].isEmpty())
    }
}