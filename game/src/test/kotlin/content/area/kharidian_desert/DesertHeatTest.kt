package content.area.kharidian_desert

import FakeRandom
import WorldTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class DesertHeatTest : WorldTest() {

    @Test
    fun `Desert heat kills over time`() {
        val player = createPlayer(Tile(3383, 2872))
        tick(1)

        assertTrue(player.timers.contains("desert_heat"))
        tick(150)

        assertEquals(90, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Desert heat usees waterskins`() {
        val player = createPlayer(Tile(3383, 2872))
        player.inventory.add("waterskin_2")
        tick(1)

        assertTrue(player.timers.contains("desert_heat"))
        tick(150)

        assertTrue(player.inventory.contains("waterskin_1"))
        assertFalse(player.inventory.contains("waterskin_2"))
        assertEquals(100, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Refill waterskin with cactus juice`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        // It'll quench ya!
        val player = createPlayer(Tile(3383, 2872))
        player.inventory.add("waterskin_0")
        player.inventory.add("knife")
        tick(1)

        val cactus = GameObjects.find(Tile(3382, 2872), "desert_cactus_full")

        player.interactObject(cactus, "Cut")
        tick(2)

        assertTrue(player.inventory.contains("waterskin_1"))
        assertFalse(player.inventory.contains("waterskin_0"))
    }
}
