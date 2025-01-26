package content.skill.melee.armour.barrows

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.entity.combat.hit.Hit
import content.entity.combat.hit.hit
import content.skill.melee.CombatFormulaTest

internal class VeracsSetEffectTest : CombatFormulaTest() {

    @Test
    fun `No veracs set effect with one missing item`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(veracs())
        player.equipment.clear(EquipSlot.Weapon.index)
        val target = createPlayer(Skill.Constitution to 990)

        val chance = Hit.chance(player, target, "magic", Item("veracs_flail"), false)
        player.hit(target, Item("veracs_flail"), "melee", damage = 10)
        tick()

        assertNotEquals(1.0, chance)
        assertEquals(980, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `No magic veracs set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(veracs())
        val target = createPlayer(Skill.Constitution to 990)

        val chance = Hit.chance(player, target, "magic", Item("veracs_flail"), false)
        player.hit(target, Item("veracs_flail"), "magic", damage = 10)
        tick(3)

        assertNotEquals(1.0, chance)
        assertEquals(980, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Veracs melee set effect`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        player.equipment.apply(veracs())
        val target = createPlayer(Skill.Constitution to 990)

        val type = "melee"
        val weapon = Item("veracs_flail")
        val chance = Hit.chance(player, target, type, weapon, false)
        player.hit(target, weapon, type, damage = 0)
        tick()

        assertEquals(1.0, chance)
        assertEquals(980, target.levels.get(Skill.Constitution))
    }

    private fun veracs(): Inventory.() -> Unit = {
        set(EquipSlot.Weapon.index, "veracs_flail")
        set(EquipSlot.Hat.index, "veracs_helm")
        set(EquipSlot.Chest.index, "veracs_brassard")
        set(EquipSlot.Legs.index, "veracs_plateskirt")
    }
}