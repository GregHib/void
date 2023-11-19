package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class EnchantedBoltsEffectTest : CombatFormulaTest() {


    @Disabled
    @Test
    fun `God arrows have chance of hitting extra damage`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "opal_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(2)

        assertTrue(target.hasClock("lucky_lightning"))
        assertNotEquals(990, target.levels.get(Skill.Constitution))
        assertNotEquals(980, target.levels.get(Skill.Constitution))
    }

}