package content.area.remennik_province.lighthouse

import FakeRandom
import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertTrue

class BasaltRockTest : WorldTest() {

    @Test
    fun `Jump to the lighthouse`() {
        val player = createPlayer(tile = Tile(2523, 3595))

        val rock1 = Tile(2522, 3597)
        var rocks = objects[rock1, "basalt_rock_start"]!!
        player.objectOption(rocks, "Jump-across")
        tick(5)
        assertEquals(rock1, player.tile)

        val rock2 = Tile(2522, 3602)
        rocks = objects[rock2, "basalt_rock_3"]!!
        player.objectOption(rocks, "Jump-across")
        tick(7)
        assertEquals(rock2, player.tile)

        val rock3 = Tile(2516, 3611)
        rocks = objects[rock3, "basalt_rock_5"]!!
        player.objectOption(rocks, "Jump-across")
        tick(17)
        assertEquals(rock3, player.tile)

        val rock4 = Tile(2514, 3615)
        rocks = objects[rock4, "basalt_rock_7"]!!
        player.objectOption(rocks, "Jump-across")
        tick(8)
        assertEquals(rock4, player.tile)

        val shore = Tile(2514, 3619)
        rocks = objects[shore, "rocky_shore"]!!
        player.objectOption(rocks, "Jump-to")
        tick(5)
        assertEquals(shore, player.tile)
    }

    @Test
    fun `Jump from the lighthouse`() {
        val player = createPlayer(tile = Tile(2514, 3619))

        val rock1 = Tile(2514, 3617)
        var rocks = objects[rock1, "basalt_rock_end"]!!
        player.objectOption(rocks, "Jump-across")
        tick(5)
        assertEquals(rock1, player.tile)

        val rock2 = Tile(2514, 3613)
        rocks = objects[rock2, "basalt_rock_6"]!!
        player.objectOption(rocks, "Jump-across")
        tick(6)
        assertEquals(rock2, player.tile)

        val rock3 = Tile(2518, 3611)
        rocks = objects[rock3, "basalt_rock_4"]!!
        player.objectOption(rocks, "Jump-across")
        tick(8)
        assertEquals(rock3, player.tile)

        val rock4 = Tile(2522, 3600)
        rocks = objects[rock4, "basalt_rock_2"]!!
        player.objectOption(rocks, "Jump-across")
        tick(17)
        assertEquals(rock4, player.tile)

        val shore = Tile(2522, 3595)
        rocks = objects[shore, "beach"]!!
        player.objectOption(rocks, "Jump-to")
        tick(5)
        assertEquals(shore, player.tile)
    }

    @Test
    fun `Jump to the lighthouse clicking nearest`() {
        val player = createPlayer(tile = Tile(2523, 3595))

        val beach = objects[Tile(2522, 3595), "beach"]!!
        player.objectOption(beach, "Jump-to")
        tick(5)
        assertEquals(Tile(2522, 3597), player.tile)

        var rocks = objects[Tile(2522, 3600), "basalt_rock_2"]!!
        player.objectOption(rocks, "Jump-across")
        tick(6)
        assertEquals(Tile(2522, 3602), player.tile)

        rocks = objects[Tile(2518, 3611), "basalt_rock_4"]!!
        player.objectOption(rocks, "Jump-across")
        tick(16)
        assertEquals(Tile(2516, 3611), player.tile)

        rocks = objects[Tile(2514, 3613), "basalt_rock_6"]!!
        player.objectOption(rocks, "Jump-across")
        tick(7)
        assertEquals(Tile(2514, 3615), player.tile)

        rocks = objects[Tile(2514, 3617), "basalt_rock_end"]!!
        player.objectOption(rocks, "Jump-across")
        tick(4)
        assertEquals(Tile(2514, 3619), player.tile)
    }

    @Test
    fun `Jump from the lighthouse clicking nearest`() {
        val player = createPlayer(tile = Tile(2514, 3619))

        val shore = objects[Tile(2514, 3619), "rocky_shore"]!!
        player.objectOption(shore, "Jump-to")
        tick(3)
        assertEquals(Tile(2514, 3617), player.tile)

        var rocks = objects[Tile(2514, 3615), "basalt_rock_7"]!!
        player.objectOption(rocks, "Jump-across")
        tick(5)
        assertEquals(Tile(2514, 3613), player.tile)

        rocks = objects[Tile(2516, 3611), "basalt_rock_5"]!!
        player.objectOption(rocks, "Jump-across")
        tick(7)
        assertEquals(Tile(2518, 3611), player.tile)

        rocks = objects[Tile(2522, 3602), "basalt_rock_3"]!!
        player.objectOption(rocks, "Jump-across")
        tick(16)
        assertEquals(Tile(2522, 3600), player.tile)

        rocks = objects[Tile(2522, 3597), "basalt_rock_start"]!!
        player.objectOption(rocks, "Jump-across")
        tick(4)
        assertEquals(Tile(2522, 3595), player.tile)
    }

    @Test
    fun `Fail jump by beach`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 255
        })
        val player = createPlayer(tile = Tile(2522, 3599))
        player.levels.set(Skill.Constitution, 12)

        val rocks = objects[Tile(2522, 3602), "basalt_rock_3"]!!
        player.objectOption(rocks, "Jump-across")
        tick(10)
        assertEquals(Tile(2521, 3595), player.tile)
        assertTrue(player.levels.get(Skill.Constitution) < 12)
    }

    @Test
    fun `Fail jump by rocky shore`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 255
        })
        val player = createPlayer(tile = Tile(2514, 3615))
        player.levels.set(Skill.Constitution, 12)

        val rocks = objects[Tile(2514, 3615), "basalt_rock_7"]!!
        player.objectOption(rocks, "Jump-across")
        tick(10)
        assertEquals(Tile(2515, 3619), player.tile)
        assertTrue(player.levels.get(Skill.Constitution) < 12)
    }
}