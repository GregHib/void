package content.skill.summoning.pet

import WorldTest
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class IncubatorUseEggTest : WorldTest() {

    // Taverley incubator tile (region 11573).
    private val taverleyIncubatorTile = Tile(2900, 3440, 0)

    @Test
    fun `using penguin egg on incubator places it`() {
        val incubator = createObject("incubator_taverley", taverleyIncubatorTile)
        val player = createPlayer(Tile(taverleyIncubatorTile.x + 1, taverleyIncubatorTile.y, 0))
        player.experience.set(Skill.Summoning, 14_000_000.0) // level requirements check the real level
        player.levels.set(Skill.Summoning, 99)
        player.inventory.add("penguin_egg")

        player.itemOnObject(incubator, 0)
        tick(5)

        assertFalse(player.inventory.contains("penguin_egg"), "egg should be consumed")
        assertEquals("penguin", player.get("incubator_egg_taverley", ""))
    }

    @Test
    fun `take-egg on finished incubator yields baby pet item`() {
        val activeIncubator = createObject("incubator_taverley", taverleyIncubatorTile)
        val player = createPlayer(Tile(taverleyIncubatorTile.x + 1, taverleyIncubatorTile.y, 0))
        // Pre-populate as if an egg was placed and has finished incubating.
        // "Finished" is no longer a discrete varbit value: the scenery stays
        // on the "incubating" morph (28359) which carries the Take-egg cache
        // option, and isFinished() is derived from the expired end-clock.
        player["incubator_egg_taverley"] = "penguin"
        player["incubator_state_taverley"] = "incubating"
        // end-clock unset → remaining() returns -1 → isFinished() == true.

        player.objectOption(activeIncubator, "Take-egg")
        tick(5)

        assertTrue(player.inventory.contains("pet_penguin_baby"), "product baby pet item should be added")
        assertEquals("", player.get("incubator_egg_taverley", ""), "egg state should clear")
        assertEquals("empty", player.get("incubator_state_taverley", "empty"), "state should reset to empty")
    }
}
