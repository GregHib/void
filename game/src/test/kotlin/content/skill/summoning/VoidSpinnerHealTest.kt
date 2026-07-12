package content.skill.summoning

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertTrue

/**
 * The Void spinner passively restores 100 of its owner's life points every 15 seconds while
 * summoned (driven by the `heal_lifepoints` npc-def property and the shared `familiar_heal` timer).
 */
class VoidSpinnerHealTest : WorldTest() {

    private fun summon(familiar: String): Player {
        val player = createPlayer(Tile(2523, 3056))
        player.levels.set(Skill.Summoning, 99)
        player.experience.set(Skill.Constitution, 14_000_000.0) // level 99, so there is headroom to heal
        player.summonFamiliar(NPCDefinitions.get(familiar), restart = false)
        tick(2) // let the summon queue assign the follower
        return player
    }

    @Test
    fun `Void spinner heals its owner 100 life points every 15 seconds`() {
        val player = summon("void_spinner_familiar")
        player.levels.drain(Skill.Constitution, 500)
        val before = player.levels.get(Skill.Constitution)

        tick(25) // 15 seconds

        // Only the spinner restores a 100-life-point jump in a single 15s window - natural regen heals
        // just a few points over that time - so a >= +100 gain proves the spinner's heal fired.
        assertTrue(player.levels.get(Skill.Constitution) >= before + 100, "the spinner healed ~100 life points")
    }

    @Test
    fun `A non-healing familiar leaves its owner to the slow natural regen`() {
        val player = summon("spirit_wolf_familiar")
        player.levels.drain(Skill.Constitution, 500)
        val before = player.levels.get(Skill.Constitution)

        tick(25)

        assertTrue(player.levels.get(Skill.Constitution) < before + 100, "no familiar heal for a spirit wolf")
    }

    @Test
    fun `The heal stops when the spinner is dismissed`() {
        val player = summon("void_spinner_familiar")
        player.dismissFamiliar()
        player.levels.drain(Skill.Constitution, 500)
        val before = player.levels.get(Skill.Constitution)

        tick(25)

        assertTrue(player.levels.get(Skill.Constitution) < before + 100, "a dismissed spinner no longer heals")
    }
}
