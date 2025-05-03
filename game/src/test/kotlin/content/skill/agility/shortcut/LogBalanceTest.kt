package content.skill.agility.shortcut

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class LogBalanceTest : WorldTest() {

    @Test
    fun `Walk west across coal truck log`() {
        val player = createPlayer(Tile(2602, 3478))
        player.levels.set(Skill.Agility, 20)
        val log = objects[Tile(2602, 3477), "coal_truck_log_balance"]!!

        player.objectOption(log, "Walk-across")
        tick(7)

        assertEquals(Tile(2598, 3477), player.tile)
        assertEquals(8.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Walk east across coal truck log`() {
        val player = createPlayer(Tile(2598, 3477))
        player.levels.set(Skill.Agility, 20)
        val log = objects[Tile(2599, 3477), "coal_truck_log_balance"]!!

        player.objectOption(log, "Walk-across")
        tick(7)

        assertEquals(Tile(2603, 3477), player.tile)
        assertEquals(8.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't walk across coal truck log without level`() {
        val player = createPlayer(Tile(2598, 3477))
        player.levels.set(Skill.Agility, 19)
        val log = objects[Tile(2599, 3477), "coal_truck_log_balance"]!!

        player.objectOption(log, "Walk-across")
        tick(2)

        assertTrue(player.containsMessage("You need at least 20 Agility"))
    }

    @Test
    fun `Walk west across ardougne log`() {
        val player = createPlayer(Tile(2602, 3336))
        player.levels.set(Skill.Agility, 33)
        val log = objects[Tile(2601, 3336), "ardougne_log_balance_east"]!!

        player.objectOption(log, "Walk-across")
        tick(7)

        assertEquals(Tile(2598, 3336), player.tile)
        assertEquals(4.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Walk east across ardougne log`() {
        val player = createPlayer(Tile(2598, 3336))
        player.levels.set(Skill.Agility, 33)
        val log = objects[Tile(2599, 3336), "ardougne_log_balance_west"]!!

        player.objectOption(log, "Walk-across")
        tick(7)

        assertEquals(Tile(2602, 3336), player.tile)
        assertEquals(4.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't walk across ardougne log without level`() {
        val player = createPlayer(Tile(2602, 3336))
        player.levels.set(Skill.Agility, 32)
        val log = objects[Tile(2601, 3336), "ardougne_log_balance_east"]!!

        player.objectOption(log, "Walk-across")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 33"))
    }
}