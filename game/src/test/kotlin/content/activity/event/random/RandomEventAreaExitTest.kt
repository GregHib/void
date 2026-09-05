package content.activity.event.random

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RandomEventAreaExitTest : WorldTest() {

    @Test
    fun `Kidnap into an instance closes the barrows overlay`() {
        val player = createPlayer(Tile(3565, 3298), "re_barrows")
        player.tele(Tile(3550, 9700))
        tick()
        assertTrue(player.interfaces.contains("barrows_overlay"), "Expected the overlay in the crypts")

        player["random_event_origin"] = Tile(3550, 9700).id
        RandomEvents.start(player, "beekeeper")
        tick(10)

        assertFalse(player.interfaces.contains("barrows_overlay"), "Overlay should close on leaving the crypts")
    }

    @Test
    fun `Kidnap out of mort myre stops the swamp decay timer`() {
        val player = createPlayer(Tile(3440, 3350), "re_swamp")
        player.tele(Tile(3441, 3350))
        tick()
        assertTrue(player.softTimers.contains("swamp_decay"), "Expected the decay timer in the swamp")

        player["random_event_origin"] = Tile(3441, 3350).id
        RandomEvents.start(player, "beekeeper")
        tick(10)

        assertFalse(player.softTimers.contains("swamp_decay"), "Decay timer should stop on leaving the swamp")
    }
}
