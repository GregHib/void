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
        tickIf(limit = 10) { player.levels.get(Skill.Summoning) != 99 }

        assertEquals(99, player.levels.get(Skill.Summoning))
    }

    @Test
    fun `Renew animation only plays after walking to obelisk`() {
        val player = createPlayer(Tile(2527, 3059))
        player.experience.set(Skill.Summoning, Level.experience(99))
        player.levels.set(Skill.Summoning, 99)
        player.levels.drain(Skill.Summoning, 10)

        val obelisk = GameObjects.find(Tile(2521, 3055), "obelisk")
        player.objectOption(obelisk, "Renew-points")

        // Walk until the points are renewed, asserting the animation never plays mid-walk.
        tickIf(limit = 30) {
            if (player.steps.isNotEmpty()) {
                assertEquals(-1, player.visuals.animation.stand)
            }
            player.levels.get(Skill.Summoning) != 99
        }

        assertEquals(0, player.steps.size)
        assertEquals(99, player.levels.get(Skill.Summoning))
    }

    @Test
    fun `Obelisk charges before the player animates`() {
        val player = createPlayer(Tile(2523, 3056))
        player.experience.set(Skill.Summoning, Level.experience(99))
        player.levels.set(Skill.Summoning, 99)
        player.levels.drain(Skill.Summoning, 10)

        val obelisk = GameObjects.find(Tile(2521, 3055), "obelisk")
        player.objectOption(obelisk, "Renew-points")

        // While the obelisk graphic charges, the player has not yet animated or renewed points.
        tick()
        assertEquals(-1, player.visuals.animation.stand)
        assertEquals(89, player.levels.get(Skill.Summoning))

        // Once the charge finishes the player animates and points are renewed together.
        tickIf(limit = 10) { player.levels.get(Skill.Summoning) != 99 }
        assertEquals(8502, player.visuals.animation.stand)
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
