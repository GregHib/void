package world.gregs.voidps.world.interact.entity.player.combat.barrows

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.Damage
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.combat.CombatFormulaTest

internal class VeracsSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No veracs set effect with one missing item`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(veracs())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer(Skill.Constitution to 990)

        val chance = Damage.chance(player, target, "magic", Item("veracs_flail"), false)
        player.hit(target, Item("veracs_flail"), "melee", damage = 10)

        assertNotEquals(1.0, chance)
        assertEquals(980, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `No magic veracs set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(veracs())
        val target = createPlayer(Skill.Constitution to 990)

        val chance = Damage.chance(player, target, "magic", Item("veracs_flail"), false)
        player.hit(target, Item("veracs_flail"), "magic", damage = 10)
        tick(2)

        assertNotEquals(1.0, chance)
        assertEquals(980, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Veracs melee set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(veracs())
        val target = createPlayer(Skill.Constitution to 990)

        val chance = Damage.chance(player, target, "melee", Item("veracs_flail"), false)
        player.hit(target, Item("veracs_flail"), "melee", damage = 0)
        var maxHit = Damage.maximum(player, target, "melee", Item("veracs_flail"))
        assertEquals(242, maxHit)
        tick()

        maxHit = Damage.maximum(player, target, "melee", Item("veracs_flail"))
        assertEquals(232, maxHit)
        assertEquals(1.0, chance)
        assertNotEquals(980, target.levels.get(Skill.Constitution))
    }

    private fun veracs(): Inventory.() -> Unit = {
        set(EquipSlot.Weapon.index, "veracs_flail")
        set(EquipSlot.Hat.index, "veracs_helm")
        set(EquipSlot.Chest.index, "veracs_brassard")
        set(EquipSlot.Legs.index, "veracs_plateskirt")
    }
}