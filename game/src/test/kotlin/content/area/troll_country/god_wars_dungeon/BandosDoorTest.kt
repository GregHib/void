package content.area.troll_country.god_wars_dungeon

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BandosDoorTest : WorldTest() {

    @Test
    fun `Bang on door enter`() {
        val player = createPlayer(Tile(2851, 5333, 2))
        player.levels.set(Skill.Strength, 70)
        player.inventory.add("hammer")
        val door = objects[Tile(2851, 5333, 2), "godwars_bandos_big_door"]!!

        player.objectOption(door, "Bang")
        tick(4)

        assertEquals(Tile(2850, 5333, 2), player.tile)
    }

    @Test
    fun `Bang on door exit`() {
        val player = createPlayer(Tile(2850, 5333, 2))
        val door = objects[Tile(2851, 5333, 2), "godwars_bandos_big_door"]!!

        player.objectOption(door, "Bang")
        tick(4)

        assertEquals(Tile(2851, 5333, 2), player.tile)
    }

    @Test
    fun `Can't bang on door without level 70 hp`() {
        val player = createPlayer(Tile(2851, 5333, 2))
        player.levels.set(Skill.Strength, 69)
        player.inventory.add("hammer")
        val door = objects[Tile(2851, 5333, 2), "godwars_bandos_big_door"]!!

        player.objectOption(door, "Bang")
        tick(2)

        assertTrue(player.containsMessage("You need to have a Strength level of 70"))
        assertEquals(Tile(2851, 5333, 2), player.tile)
    }

    @Test
    fun `Can't bang on door without a hammer`() {
        val player = createPlayer(Tile(2851, 5333, 2))
        player.levels.set(Skill.Strength, 70)
        val door = objects[Tile(2851, 5333, 2), "godwars_bandos_big_door"]!!

        player.objectOption(door, "Bang")
        tick(2)

        assertTrue(player.containsMessage("You need a suitable hammer"))
        assertEquals(Tile(2851, 5333, 2), player.tile)
    }
}
