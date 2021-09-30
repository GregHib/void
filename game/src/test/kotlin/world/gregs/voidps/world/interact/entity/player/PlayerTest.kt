package world.gregs.voidps.world.interact.entity.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.handle.WalkHandler
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption

internal class PlayerTest : WorldMock() {

    @Test
    fun `Walk to location`() = runBlocking(Dispatchers.Default) {
        val start = Tile(100, 100)
        val player = createPlayer("walker", start)
        val handler = WalkHandler()

        handler.validate(player, Walk(100, 110))
        tick(5)

        assertEquals(Tile(100, 105), player.tile)
    }

    @Test
    fun `Run to location`() = runBlocking(Dispatchers.Default) {
        val start = Tile(100, 100)
        val player = createPlayer("walker", start)
        val handler = WalkHandler()

        player.interfaceOption("energy_orb", "", "Turn Run mode on")
        handler.validate(player, Walk(100, 110))
        tick(5)

        assertEquals(Tile(100, 110), player.tile)
    }

    @Test
    fun `Restore energy over time`() = runBlocking(Dispatchers.Default) {
        val start = Tile(100, 100)
        val player = createPlayer("walker", start)
        player.runEnergy = 0

        tick(5)

        assertTrue(player.runEnergy > 0)
    }

    @Test
    fun `Restore energy faster when resting`() = runBlocking(Dispatchers.Default) {
        val start = Tile(100, 100)
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