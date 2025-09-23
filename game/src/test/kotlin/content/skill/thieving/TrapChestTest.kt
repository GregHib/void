package content.skill.thieving

import FakeRandom
import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals

internal class TrapChestTest : WorldTest() {

    @Test
    fun `Successfully steal from a stall`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(Tile(2675, 3310))
        player.levels.set(Skill.Thieving, 15)
        val stall = objects[Tile(2676, 3310), "coin_chest"]!!

        player.objectOption(stall, "Search for traps")
        tick(6)

        assertEquals(player.inventory.count("coins"), 10)
        assertEquals(player.experience.get(Skill.Thieving), 7.8)
    }
}
