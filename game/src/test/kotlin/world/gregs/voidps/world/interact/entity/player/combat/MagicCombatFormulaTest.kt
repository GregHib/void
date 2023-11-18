package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.test.assertEquals

internal class MagicCombatFormulaTest : CombatFormulaTest() {

    @Test
    fun `Maxed player fire surge a rat`() {
        val player = createPlayer(Skill.Magic to 99)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "fire_wave")
        assertEquals(6912, offensiveRating)
        assertEquals(640, defensiveRating)

        assertEquals(200, maxHit)
        assertEquals(0.9536, chance, 0.0001)
    }

}