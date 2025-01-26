package content.skill.magic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.entity.combat.hit.hit
import content.skill.melee.CombatFormulaTest
import interfaceOption

internal class AddChargeGodSpellEffectTest : CombatFormulaTest() {

    @Test
    fun `God spell and matching cape`() {
        val player = createPlayer(Skill.Magic to 99)
        player.equipment.set(EquipSlot.Cape.index, "saradomin_cape")
        player.equipment.set(EquipSlot.Weapon.index, "saradomin_staff")
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, type = "magic", spell = "saradomin_strike", damage = 100)
        tick(3)

        assertEquals(890, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Charge with god spell and matching cape increases hit`() {
        val player = createPlayer(Skill.Magic to 99)
        player.equipment.set(EquipSlot.Cape.index, "saradomin_cape")
        player.equipment.set(EquipSlot.Weapon.index, "saradomin_staff")
        player.inventory.add("fire_rune", 3)
        player.inventory.add("blood_rune", 3)
        player.inventory.add("air_rune", 3)
        val target = createPlayer(Skill.Constitution to 990)

        player.interfaceOption("modern_spellbook", "charge")
        assertTrue(player.hasClock("charge"))
        player.hit(target, type = "magic", spell = "saradomin_strike", damage = 100)
        tick(3)

        assertEquals(790, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Charge with god spell and different cape does nothing`() {
        val player = createPlayer(Skill.Magic to 99)
        player.equipment.set(EquipSlot.Cape.index, "zamorak_cape")
        player.equipment.set(EquipSlot.Weapon.index, "saradomin_staff")
        player.inventory.add("fire_rune", 3)
        player.inventory.add("blood_rune", 3)
        player.inventory.add("air_rune", 3)
        val target = createPlayer(Skill.Constitution to 990)

        player.interfaceOption("modern_spellbook", "charge")
        assertTrue(player.hasClock("charge"))
        player.hit(target, type = "magic", spell = "saradomin_strike", damage = 100)
        tick(3)

        assertEquals(890, target.levels.get(Skill.Constitution))
    }
}