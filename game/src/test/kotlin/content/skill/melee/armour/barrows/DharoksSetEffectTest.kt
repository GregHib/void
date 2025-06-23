package content.skill.melee.armour.barrows

import content.skill.melee.CombatFormulaTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

internal class DharoksSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No dharoks set effect with one missing item`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(dharoks())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer()

        val weapon = Item("dharoks_greataxe")
        val (_, _, maxHit, _) = calculate(player, target, "melee", weapon)

        assertEquals(112, maxHit)
    }

    @Test
    fun `No magic dharoks set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(dharoks())
        val target = createPlayer()

        val weapon = Item("dharoks_greataxe")
        val (_, _, maxHit, _) = calculate(player, target, "magic", weapon)

        assertEquals(0, maxHit)
    }

    @Test
    fun `Dharoks melee set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 990)
        player.equipment.apply(dharoks())
        val target = createPlayer()

        val weapon = Item("dharoks_greataxe")
        val (_, _, maxHit, _) = calculate(player, target, "melee", weapon)

        assertEquals(287, maxHit)
    }

    @Test
    fun `Dharoks melee set full effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 10)
        player.equipment.apply(dharoks())
        val target = createPlayer()

        val weapon = Item("dharoks_greataxe")
        val (_, _, maxHit, _) = calculate(player, target, "melee", weapon)

        assertEquals(565, maxHit)
    }

    private fun dharoks(): Inventory.() -> Unit = {
        set(EquipSlot.Weapon.index, "dharoks_greataxe")
        set(EquipSlot.Hat.index, "dharoks_helm")
        set(EquipSlot.Chest.index, "dharoks_platebody")
        set(EquipSlot.Legs.index, "dharoks_platelegs")
    }
}
