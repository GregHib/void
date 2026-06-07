package content.skill.summoning

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class SummoningObeliskTest : WorldTest() {

    @Test
    fun `Renew points at obelisk`() {
        val player = createPlayer(Tile(2523, 3056))
        player.experience.set(Skill.Summoning, Level.experience(99))
        player.levels.set(Skill.Summoning, 99)
        player.levels.drain(Skill.Summoning, 10)

        val obelisk = GameObjects.find(Tile(2521, 3055), "obelisk")
        player.objectOption(obelisk, "Renew-points")
        tick()

        assertEquals(99, player.levels.get(Skill.Summoning))
    }

    @Test
    fun `Can't renew points when already full`() {
        val player = createPlayer(Tile(2523, 3056))
        player.experience.set(Skill.Summoning, Level.experience(99))
        player.levels.set(Skill.Summoning, 99)

        val obelisk = GameObjects.find(Tile(2521, 3055), "obelisk")
        player.objectOption(obelisk, "Renew-points")
        tick()

        assertEquals(99, player.levels.get(Skill.Summoning))
    }
}
