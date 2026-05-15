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
        val incubator = createObject("incubator_empty", taverleyIncubatorTile)
        val player = createPlayer(Tile(taverleyIncubatorTile.x + 1, taverleyIncubatorTile.y, 0))
        player.levels.set(Skill.Summoning, 99)
        player.inventory.add("penguin_egg")

        player.itemOnObject(incubator, 0)
        tick(5)

        assertFalse(player.inventory.contains("penguin_egg"), "egg should be consumed")
        assertEquals("penguin", player.get("incubator_egg_taverley", ""))
    }

    @Test
    fun `take-egg on finished incubator yields baby pet item`() {
        val activeIncubator = createObject("incubator_active", taverleyIncubatorTile)
        val player = createPlayer(Tile(taverleyIncubatorTile.x + 1, taverleyIncubatorTile.y, 0))
        // Pre-populate as if an egg was placed and has finished incubating.
        player.set("incubator_egg_taverley", "penguin")
        player.set("incubator_end_taverley", 1L) // long in the past
        player.set("taverley_incubator_state", 1) // INCUBATING; currentState() will see it as FINISHED via end-time check

        player.objectOption(activeIncubator, "Take-egg")
        tick(5)

        assertTrue(player.inventory.contains("pet_penguin_baby"), "product baby pet item should be added")
        assertEquals("", player.get("incubator_egg_taverley", ""), "egg state should clear")
        assertEquals(0, player.get("taverley_incubator_state", -1), "varbit should reset to EMPTY")
    }
}
