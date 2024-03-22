package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

internal class CastleWarsBraceEffectTest : CombatFormulaTest() {

    @Test
    fun `Brace against normal target no effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player["castle_wars_brace"] = true
        val target = createPlayer()

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, target, "melee")

        assertEquals(5504, offensiveRating)
        assertEquals(576, defensiveRating)
        assertEquals(112, maxHit)
        assertEquals(0.9475, chance, 0.0001)
    }

    @Test
    fun `No brace against flag holder`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        val target = createPlayer()
        target.equipment.set(EquipSlot.Weapon.index, "zamorak_flag")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, target, "melee")

        assertEquals(5504, offensiveRating)
        assertEquals(640, defensiveRating)
        assertEquals(112, maxHit)
        assertEquals(0.9416, chance, 0.0001)
    }

    @Test
    fun `Brace against flag holder increases hits 20 percent`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player["castle_wars_brace"] = true
        val target = createPlayer()
        target.equipment.set(EquipSlot.Weapon.index, "zamorak_flag")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, target, "melee")

        assertEquals(5504, offensiveRating)
        assertEquals(640, defensiveRating)
        assertEquals(134, maxHit)
        assertEquals(0.9416, chance, 0.0001)
    }
}