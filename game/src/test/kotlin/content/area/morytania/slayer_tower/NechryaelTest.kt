package content.area.morytania.slayer_tower

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals

class NechryaelTest : WorldTest() {

    @Test
    fun `Nechryael death spawns`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
            override fun nextInt(from: Int, until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 80)

        val nechryael = npcs.add("nechryael", emptyTile.addY(1))
        tick()

        player.npcOption(nechryael, "Attack")

        tick(3)

        assertEquals(1, player["death_spawns", 0])
    }

    @Test
    fun `Death spawns can attack even when in combat`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
            override fun nextInt(from: Int, until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 80)

        val nechryael = npcs.add("nechryael", emptyTile.addY(1))
        tick()

        player.npcOption(nechryael, "Attack")

        tick(3)

        assertEquals(1, player["death_spawns", 0])
    }
}
