package content.skill.thieving

import FakeRandom
import WorldTest
import content.entity.effect.stunned
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class StallsTest : WorldTest() {

    @Test
    fun `Successfully steal from a stall`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(Tile(2666, 3310))
        player.levels.set(Skill.Thieving, 15)
        val stall = GameObjects.find(Tile(2667, 3310), "bakers_stall")

        player.objectOption(stall, "Steal-from")
        tick(3)

        assertEquals(player.inventory.count("bread"), 1)
        assertEquals(player.experience.get(Skill.Thieving), 16.0)
        assertFalse(player.stunned)
    }
}
