package content.skill.melee.armour.barrows

import content.entity.combat.hit.hit
import content.skill.melee.CombatFormulaTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

internal class AhrimsSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No ahrims set effect with one missing item`() {
        val player = createPlayer(Skill.Magic to 99)
        player.equipment.apply(ahrims())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer(Skill.Strength to 99, Skill.Constitution to 990)

        player.hit(target, Item("ahrims_staff"), "magic", damage = 100)
        tick(3)

        assertEquals(880, target.levels.get(Skill.Constitution))
        assertEquals(99, target.levels.get(Skill.Strength))
    }

    @Test
    fun `No melee ahrims set effect`() {
        val player = createPlayer(Skill.Magic to 99)
        player.equipment.apply(ahrims())
        val target = createPlayer(Skill.Strength to 99, Skill.Constitution to 990)

        player.hit(target, Item("ahrims_staff"), "melee", damage = 100)
        tick(2)

        assertEquals(890, target.levels.get(Skill.Constitution))
        assertEquals(99, target.levels.get(Skill.Strength))
    }

    @Test
    fun `Ahrims magic set effect`() {
        val player = createPlayer(Skill.Magic to 75)
        player.equipment.apply(ahrims())
        val target = createPlayer(Skill.Strength to 99, Skill.Constitution to 990)

        player.hit(target, Item("ahrims_staff"), "magic", damage = 100)
        tick(3)

        assertEquals(880, target.levels.get(Skill.Constitution))
        assertEquals(94, target.levels.get(Skill.Strength))
    }

    private fun ahrims(): Inventory.() -> Unit = {
        set(EquipSlot.Weapon.index, "ahrims_staff")
        set(EquipSlot.Hat.index, "ahrims_hood")
        set(EquipSlot.Chest.index, "ahrims_robe_top")
        set(EquipSlot.Legs.index, "ahrims_robe_skirt")
    }
}
