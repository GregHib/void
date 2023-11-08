package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit

internal class KarilsSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No karils set effect with one missing item`() {
        val player = createPlayer(Skill.Ranged to 99)
        player.equipment.apply(karils())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer(Skill.Agility to 99, Skill.Constitution to 990)

        player.hit(target, Item("karils_crossbow"), "range", damage = 10)
        tick(2)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(99, target.levels.get(Skill.Agility))
    }

    @Test
    fun `No melee karils set effect`() {
        val player = createPlayer(Skill.Ranged to 99)
        player.equipment.apply(karils())
        val target = createPlayer(Skill.Agility to 99, Skill.Constitution to 990)

        player.hit(target, Item("karils_crossbow"), "melee", damage = 10)
        tick(2)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(99, target.levels.get(Skill.Agility))
    }

    @Test
    fun `Karils range set effect`() {
        val player = createPlayer(Skill.Ranged to 75)
        player.equipment.apply(karils())
        val target = createPlayer(Skill.Agility to 99, Skill.Constitution to 990)

        var count = 0
        while (target.levels.get(Skill.Agility) == 99) {
            player.hit(target, Item("karils_crossbow"), "range", damage = 10)
            if (count++ > 20) {
                throw IllegalStateException("Random effect not applied within attempt limit.")
            }
        }
        tick(2)

        assertNotEquals(990, target.levels.get(Skill.Constitution))
        assertEquals(80, target.levels.get(Skill.Agility))
    }

    private fun karils(): Inventory.() -> Unit = {
        set(EquipSlot.Chest.index, "karils_top")
        set(EquipSlot.Legs.index, "karils_skirt")
        set(EquipSlot.Weapon.index, "karils_crossbow")
        set(EquipSlot.Hat.index, "karils_coif")
    }
}