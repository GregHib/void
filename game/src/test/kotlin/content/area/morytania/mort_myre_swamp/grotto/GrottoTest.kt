package content.area.morytania.mort_myre_swamp.grotto

import FakeRandom
import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GrottoTest : WorldTest() {

    @Test
    fun `Jump bridge south`() {
        val player = createPlayer(Tile(3441, 3331))
        val bridge = objects.find(Tile(3441, 3331), "grotto_bridge")

        player.objectOption(bridge, "Jump")
        tick(3)

        assertEquals(Tile(3441, 3329), player.tile)
    }

    @Test
    fun `Jump bridge north`() {
        val player = createPlayer(Tile(3440, 3329))
        val bridge = objects.find(Tile(3440, 3329), "grotto_bridge")

        player.objectOption(bridge, "Jump")
        tick(3)

        assertEquals(Tile(3440, 3331), player.tile)
    }

    @Test
    fun `Fail jump bridge south`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 255
        })
        val player = createPlayer(Tile(3441, 3331))
        val bridge = objects.find(Tile(3441, 3331), "grotto_bridge")

        player.objectOption(bridge, "Jump")
        tick(6)

        assertEquals(Tile(3438, 3328), player.tile)
        assertTrue(player.levels.get(Skill.Constitution) < 100)
    }

    @Test
    fun `Fail bridge jump north`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 255
        })
        val player = createPlayer(Tile(3440, 3329))
        val bridge = objects.find(Tile(3440, 3329), "grotto_bridge")

        player.objectOption(bridge, "Jump")
        tick(6)

        assertEquals(Tile(3438, 3332), player.tile)
        assertTrue(player.levels.get(Skill.Constitution) < 100)
    }
}
