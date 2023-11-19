package world.gregs.voidps.world.interact.entity.player.combat.barrows

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.Damage
import world.gregs.voidps.world.interact.entity.player.combat.CombatFormulaTest

internal class DharoksSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No dharoks set effect with one missing item`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(dharoks())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer()

        val maxHit = Damage.maximum(player, target, "melee", Item("dharoks_greataxe"))

        assertEquals(112, maxHit)
    }

    @Test
    fun `No magic dharoks set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(dharoks())
        val target = createPlayer()

        val maxHit = Damage.maximum(player, target, "magic", Item("dharoks_greataxe"))

        assertEquals(0, maxHit)
    }

    @Test
    fun `Dharoks melee set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 990)
        player.equipment.apply(dharoks())
        val target = createPlayer()

        val maxHit = Damage.maximum(player, target, "melee", Item("dharoks_greataxe"))

        assertEquals(287, maxHit)
    }

    @Test
    fun `Dharoks melee set full effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 10)
        player.equipment.apply(dharoks())
        val target = createPlayer()

        val maxHit = Damage.maximum(player, target, "melee", Item("dharoks_greataxe"))

        assertEquals(565, maxHit)
    }

    private fun dharoks(): Inventory.() -> Unit = {
        set(EquipSlot.Weapon.index, "dharoks_greataxe")
        set(EquipSlot.Hat.index, "dharoks_helm")
        set(EquipSlot.Chest.index, "dharoks_platebody")
        set(EquipSlot.Legs.index, "dharoks_platelegs")
    }
}