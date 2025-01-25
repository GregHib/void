package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.effect.frozen
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned

internal class AncientSpellEffectsTest : CombatFormulaTest() {

    @Test
    fun `Smoke spells poison target`() {
        val player = createPlayer(Skill.Magic to 99)
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, type = "magic", spell = "smoke_blitz", damage = 100)
        tick(3)

        assertEquals(890, target.levels.get(Skill.Constitution))
        assertTrue(target.poisoned)
    }

    @Test
    fun `Shadow spells drain target`() {
        val player = createPlayer(Skill.Magic to 99)
        val target = createPlayer(Skill.Constitution to 990, Skill.Attack to 99)

        player.hit(target, type = "magic", spell = "shadow_blitz", damage = 100)
        tick(3)

        assertEquals(890, target.levels.get(Skill.Constitution))
        assertEquals(90, target.levels.get(Skill.Attack))
    }

    @Test
    fun `Blood spells heal caster`() {
        val player = createPlayer(Skill.Magic to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        val target = createPlayer(Skill.Constitution to 990)
        player.hit(target, type = "magic", spell = "blood_blitz", damage = 100)
        tick(3)

        assertEquals(890, target.levels.get(Skill.Constitution))
        assertEquals(525, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Ice spells freeze target`() {
        val player = createPlayer(Skill.Magic to 99)
        val target = createPlayer(Skill.Constitution to 990)
        player.hit(target, type = "magic", spell = "ice_blitz", damage = 100)
        tick(3)

        assertEquals(890, target.levels.get(Skill.Constitution))
        assertTrue(target.frozen)
    }

    @Test
    fun `Miasmic spells start half attack speed of target`() {
        val player = createPlayer(Skill.Magic to 99)
        val target = createPlayer(Skill.Constitution to 990)
        player.hit(target, type = "magic", spell = "miasmic_blitz", damage = 100)
        tick(3)

        assertEquals(890, target.levels.get(Skill.Constitution))
        assertTrue(target.hasClock("miasmic"))
    }
}