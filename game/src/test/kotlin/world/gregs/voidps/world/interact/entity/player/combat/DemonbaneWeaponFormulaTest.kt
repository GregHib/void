package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import kotlin.test.assertEquals

internal class DemonbaneWeaponFormulaTest : CombatFormulaTest() {

    @Test
    fun `Darklight on greater demon`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Weapon.index, "darklight")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("darklight"))

        assertEquals(6880, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(212, maxHit)
        assertEquals(0.5813, chance, 0.0001)

        val (_, _, specMaxHit, _) = calculate(player, npc, "melee", Item("darklight"), special = true)
        assertEquals(133, specMaxHit)
    }

}