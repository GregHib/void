package content.area.morytania.port_phasmatys

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertTrue

class PortPhasmatysBarrierTest : WorldTest() {

    private val outside = Tile(3659, 3509)

    private fun barrier() = GameObjects.find(Tile(3659, 3508), "phasmatys_barrier_base")

    @Test
    fun `Paying the toll costs two ecto-tokens`() {
        val player = createPlayer(outside)
        player.inventory.add("ecto_token", 5)

        player.objectOption(barrier(), "Pay-toll(2-Ecto)")
        tick(6)

        assertEquals(3, player.inventory.count("ecto_token"))
        assertTrue(player.containsMessage("You pay the toll charge of 2 Ectotokens."))
    }

    @Test
    fun `Paying the toll without tokens is refused`() {
        val player = createPlayer(outside)

        player.objectOption(barrier(), "Pay-toll(2-Ecto)")
        tick(6)

        assertTrue(player.containsMessage("You need to go to the Temple and earn some."))
        assertEquals(outside, player.tile)
    }

    @Test
    fun `Passing without the quest asks for the toll`() {
        val player = createPlayer(outside)

        player.objectOption(barrier(), "Pass")
        tick(6)

        assertTrue(player.containsMessage("must pay a toll charge of 2 Ectotokens"))
        assertEquals(outside, player.tile)
    }

    @Test
    fun `Completing Ghosts Ahoy makes passage free`() {
        val player = createPlayer(outside)
        player.set("ghosts_ahoy", "completed")

        player.objectOption(barrier(), "Pass")
        tick(6)

        assertTrue(player.containsMessage("you may pass without charge"))
    }

    @Test
    fun `Leaving the city is free`() {
        val player = createPlayer(Tile(3659, 3506))

        player.objectOption(barrier(), "Pass")
        tick(6)

        assertEquals(Tile(3659, 3508), player.tile)
        assertEquals(0, player.inventory.count("ecto_token"))
    }
}
