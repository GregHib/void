package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import kotlin.math.max
import kotlin.math.min
import kotlin.test.assertEquals

internal class SpiritShieldFormulaTest : CombatFormulaTest() {

    @Test
    fun `Divine drains prayer`() {
        val player = createPlayer(Skill.Prayer to 99)
        player.equipment.set(EquipSlot.Shield.index, "divine_spirit_shield")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(npc, player, "melee")

        assertEquals(97, player.levels.get(Skill.Prayer))
        assertEquals(5440, offensiveRating)
        assertEquals(1251, defensiveRating)
        assertEquals(62, maxHit)
        assertEquals(0.8848, chance, 0.0001)
    }

    @Test
    fun `Elysian randomly reduces damage`() {
        val player = createPlayer()
        player.equipment.set(EquipSlot.Shield.index, "elysian_spirit_shield")
        val npc = createNPC("greater_demon")

        var maxHitMin = Int.MAX_VALUE
        var maxHitMax = -1
        for (attempt in 0 until 10) {
            val (_, _, maxHit, _) = calculate(npc, player, "melee")
            maxHitMax = max(maxHitMax, maxHit)
            maxHitMin = min(maxHitMin, maxHit)
            if (maxHitMin != Int.MAX_VALUE && maxHitMax != -1 && maxHitMin != maxHitMax) {
                break
            }
        }

        val (offensiveRating, defensiveRating, _, chance) = calculate(npc, player, "melee")
        assertEquals(5440, offensiveRating)
        assertEquals(1251, defensiveRating)
        assertEquals(67, maxHitMin)
        assertEquals(90, maxHitMax)
        assertEquals(0.8848, chance, 0.0001)
    }
}