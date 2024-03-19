package world.gregs.voidps.world.interact.entity.combat

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.npcOption

internal class CombatFlinchTest : WorldTest() {

    private val tile = Tile(3259, 3255)
    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int) = 10
        })
    }

    @Test
    fun `Npc doesn't fight back`() {
        val player = createPlayer("player", tile)
        val npc = createNPC("cow", tile.addY(2))

        player.npcOption(npc, "Attack")
        tick()
        player.walkTo(tile)
        tick()
        assertTrue(npc.inCombat)
        assertFalse(player.inCombat)
        tick(7)
        assertFalse(npc.inCombat)

        player.npcOption(npc, "Attack")
        tick()
        player.walkTo(tile)
        tick()
        assertTrue(npc.inCombat)
        assertFalse(player.inCombat)
    }

    @Test
    fun `Npc does fight back`() {
        val player = createPlayer("player", tile)
        val npc = createNPC("cow", tile.addY(2))

        player.npcOption(npc, "Attack")
        tick()
        player.walkTo(tile)
        tick()
        assertTrue(npc.inCombat)
        assertFalse(player.inCombat)
        tick(5) // Don't wait long enough

        player.npcOption(npc, "Attack")
        tick()
        player.walkTo(tile)
        tick(4)
        assertTrue(npc.inCombat)
        assertTrue(player.inCombat)
    }
}