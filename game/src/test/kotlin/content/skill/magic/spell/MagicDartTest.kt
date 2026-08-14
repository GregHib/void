package content.skill.magic.spell

import content.entity.combat.hit.hit
import content.skill.melee.CombatFormulaTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill

internal class MagicDartTest : CombatFormulaTest() {

    @Test
    fun `Magic dart damages the target`() {
        val player = createPlayer(Skill.Magic to 99)
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, offensiveType = "magic", spell = "magic_dart", damage = 100)
        tick(3)

        assertEquals(890, target.levels.get(Skill.Constitution))
    }
}
