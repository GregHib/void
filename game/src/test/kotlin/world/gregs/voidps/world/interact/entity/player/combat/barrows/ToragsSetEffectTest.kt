package world.gregs.voidps.world.interact.entity.player.combat.barrows

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.CombatFormulaTest
import world.gregs.voidps.world.interact.entity.player.energy.energyPercent

internal class ToragsSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No torags set effect with one missing item`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(torags())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, Item("torags_hammers"), "melee", damage = 10)
        tick()

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(100, target.energyPercent())
    }

    @Test
    fun `No magic torags set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(torags())
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, Item("torags_hammers"), "magic", damage = 10)
        tick(3)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(100, target.energyPercent())
    }

    @Test
    fun `Torags melee set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(torags())
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, Item("torags_hammers"), "melee", damage = 10)
        tick()

        assertNotEquals(990, target.levels.get(Skill.Constitution))
        assertEquals(80, target.energyPercent())
    }

    private fun torags(): Inventory.() -> Unit = {
        set(EquipSlot.Weapon.index, "torags_hammers")
        set(EquipSlot.Hat.index, "torags_helm")
        set(EquipSlot.Chest.index, "torags_platebody")
        set(EquipSlot.Legs.index, "torags_platelegs")
    }
}