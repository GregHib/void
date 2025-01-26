package content.entity.player

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.WalkHandler
import world.gregs.voidps.network.client.instruction.Walk
import content.entity.player.effect.energy.runEnergy
import WorldTest
import interfaceOption

internal class PlayerTest : WorldTest() {

    @Test
    fun `Walk to location`() {
        val start = emptyTile
        val player = createPlayer("walker", start)
        val handler = WalkHandler()

        handler.validate(player, Walk(emptyTile.x, emptyTile.y + 10))
        tick(5)

        assertEquals(emptyTile.addY(5), player.tile)
    }

    @Test
    fun `Run to location`() {
        val start = emptyTile
        val player = createPlayer("runner", start)
        val handler = WalkHandler()

        player.interfaceOption("energy_orb", "", "Turn Run mode on")
        handler.validate(player, Walk(emptyTile.x, emptyTile.y + 10))
        tick(5)

        assertEquals(emptyTile.addY(10), player.tile)
    }

    @Test
    fun `Restore energy over time`() {
        val start = emptyTile
        val player = createPlayer("walker", start)
        player.runEnergy = 0

        tick(5)

        assertTrue(player.runEnergy > 0)
    }

    @Test
    fun `Restore energy faster when resting`() {
        val start = emptyTile
        val player = createPlayer("walker", start)
        player.runEnergy = 0
        tick(5)
        val energy = player.runEnergy
        player.runEnergy = 0

        player.interfaceOption("energy_orb", "", "Rest")
        tick(5)

        assertTrue(player.runEnergy > energy)
    }

}