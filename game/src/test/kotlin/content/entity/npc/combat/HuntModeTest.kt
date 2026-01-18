package content.entity.npc.combat

import WorldTest
import content.entity.combat.attacking
import content.entity.combat.underAttack
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import kotlin.test.assertEquals

internal class HuntModeTest : WorldTest() {

    @BeforeEach
    fun setup() {
        NPCs.clear()
        Players.clear()
    }

    @Test
    fun `Cowardly attack low level player when in range`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_spider", emptyTile.addY(2))
        assertFalse(player.underAttack)
        assertTrue(Settings["world.npcs.aggression", false])

        player.walkTo(emptyTile.addY(1))
        tick(6)

        assertTrue(npc.attacking)
        assertTrue(player.underAttack)
    }

    @Test
    fun `Cowardly doesn't attack high level player when in range`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_spider", emptyTile.addY(1))
        player.combatLevel = 5
        assertFalse(player.underAttack)

        tick(6)

        assertFalse(npc.attacking)
        assertFalse(player.underAttack)
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
        assertTrue(FloorItems.at(emptyTile.addY(3)).isEmpty())
    }
}
