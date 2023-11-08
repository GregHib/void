package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.energy.energyPercent

internal class GuthansSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No guthans set effect with one missing item`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(guthans())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, Item("guthans_warspear"), "melee", damage = 10)

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
        tick(2)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(500, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Guthans melee set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        player.equipment.apply(guthans())
        val target = createPlayer(Skill.Constitution to 990)

        var count = 0
        while (player.levels.get(Skill.Constitution) == 500) {
            player.hit(target, Item("guthans_warspear"), "melee", damage = 10)
            if (count++ > 20) {
                throw IllegalStateException("Random effect not applied within attempt limit.")
            }
        }
        tick()

        assertNotEquals(990, target.levels.get(Skill.Constitution))
        assertEquals(510, player.levels.get(Skill.Constitution))
    }

    private fun guthans(): Inventory.() -> Unit = {
        set(EquipSlot.Chest.index, "guthans_platebody")
        set(EquipSlot.Legs.index, "guthans_chainskirt")
        set(EquipSlot.Weapon.index, "guthans_warspear")
        set(EquipSlot.Hat.index, "guthans_helm")
    }
}