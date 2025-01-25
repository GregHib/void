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
import content.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.CombatFormulaTest

internal class GuthansSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No guthans set effect with one missing item`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(guthans())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, Item("guthans_warspear"), "melee", damage = 10)
        tick()

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(500, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `No magic guthans set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(guthans())
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, Item("guthans_warspear"), "magic", damage = 10)
        tick(3)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(500, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Guthans melee set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(guthans())
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, Item("guthans_warspear"), "melee", damage = 10)
        tick()

        assertNotEquals(990, target.levels.get(Skill.Constitution))
        assertEquals(510, player.levels.get(Skill.Constitution))
    }

    private fun guthans(): Inventory.() -> Unit = {
        set(EquipSlot.Weapon.index, "guthans_warspear")
        set(EquipSlot.Hat.index, "guthans_helm")
        set(EquipSlot.Chest.index, "guthans_platebody")
        set(EquipSlot.Legs.index, "guthans_chainskirt")
    }
}