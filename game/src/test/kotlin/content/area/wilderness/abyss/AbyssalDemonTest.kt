package content.area.wilderness.abyss

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.setRandom
import kotlin.test.assertNotEquals

class AbyssalDemonTest : WorldTest() {

    @Test
    fun `Abyssal demon teleports target around in combat`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 85)

        val demon = NPCs.add("abyssal_demon", emptyTile.addY(1))
        tick()

        player.npcOption(demon, "Attack")

        tick(3)

        assertNotEquals(emptyTile.addY(1), demon.tile)
    }

    @Test
    fun `Abyssal demon teleports around in combat`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 6) 6 else 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 85)

        val demon = NPCs.add("abyssal_demon", emptyTile.addY(1))
        tick()

        player.npcOption(demon, "Attack")

        tick(3)

        assertNotEquals(emptyTile, player.tile)
    }
}
