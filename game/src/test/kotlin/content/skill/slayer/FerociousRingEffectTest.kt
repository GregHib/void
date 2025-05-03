package content.skill.slayer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import content.skill.melee.CombatFormulaTest

internal class FerociousRingEffectTest : CombatFormulaTest() {

    @Test
    fun `Ring target outside of dungeon no effect`() {
        val player = createPlayer()
        player.levels.set(Skill.Strength, 99)
        player.equipment.set(EquipSlot.Ring.index, "ferocious_ring_5")
        val target = createPlayer()

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, target, "melee")

        assertEquals(768, offensiveRating)
        assertEquals(576, defensiveRating)
        assertEquals(112, maxHit)
        assertEquals(0.6241, chance, 0.0001)
    }

    @Test
    fun `Ring against target in dungeon multiplies max hit`() {
        val player = createPlayer(Tile(1660, 5257), "source")
        player.levels.set(Skill.Strength, 99)
        player.equipment.set(EquipSlot.Ring.index, "ferocious_ring_5")
        val target = createPlayer(Tile(1660, 5257), "target")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, target, "melee")

        assertEquals(768, offensiveRating)
        assertEquals(576, defensiveRating)
        assertEquals(116, maxHit)
        assertEquals(0.6241, chance, 0.0001)
    }

}