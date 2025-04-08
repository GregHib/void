package content.area.kandarin.seers_village

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class CoalTruckLogBalanceTest : WorldTest() {

    @Test
    fun `Walk west across log`() {
        val player = createPlayer(tile = Tile(2602, 3478))
        player.levels.set(Skill.Agility, 20)
        val monkeyBars = objects[Tile(2602, 3477), "log_balance"]!!

        player.objectOption(monkeyBars, "Walk-across")
        tick(7)

        assertEquals(Tile(2598, 3477), player.tile)
        assertEquals(8.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Walk east across log`() {
        val player = createPlayer(tile = Tile(2598, 3477))
        player.levels.set(Skill.Agility, 20)
        val monkeyBars = objects[Tile(2599, 3477), "log_balance"]!!

        player.objectOption(monkeyBars, "Walk-across")
        tick(7)

        assertEquals(Tile(2603, 3477), player.tile)
        assertEquals(8.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't walk across log without level`() {
        val player = createPlayer(tile = Tile(2598, 3477))
        player.levels.set(Skill.Agility, 19)
        val monkeyBars = objects[Tile(2599, 3477), "log_balance"]!!

        player.objectOption(monkeyBars, "Walk-across")
        tick(2)

        assertTrue(player.containsMessage("You need at least 20 Agility"))
    }
}