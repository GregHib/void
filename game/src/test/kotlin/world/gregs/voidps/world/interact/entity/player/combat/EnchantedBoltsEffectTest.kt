package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import content.entity.effect.frozen
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import content.entity.player.effect.antifire
import content.entity.effect.toxin.poisoned
import kotlin.test.assertTrue

internal class EnchantedBoltsEffectTest : CombatFormulaTest() {

    @Test
    fun `Opal bolts have chance of hitting extra damage`() {
        val player = createPlayer(Skill.Ranged to 100)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "opal_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(970, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Jade bolts have chance of freezing target`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "jade_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertTrue(target.frozen)
    }

    @Test
    fun `Pearl bolts have chance of hitting extra damage`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "pearl_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(976, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Topaz bolts have chance of draining magic`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "topaz_bolts_e"
        val target = createPlayer(Skill.Constitution to 990, Skill.Magic to 99)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(98, target.levels.get(Skill.Magic))
    }

    @Test
    fun `Sapphire bolts have chance stealing prayer`() {
        val player = createPlayer(Skill.Ranged to 99, Skill.Prayer to 99)
        player.levels.set(Skill.Prayer, 50)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "sapphire_bolts_e"
        val target = createPlayer(Skill.Constitution to 990, Skill.Prayer to 99)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertEquals(95, target.levels.get(Skill.Prayer))
        assertEquals(52, player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Emerald bolts have chance of poisoning`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "emerald_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(980, target.levels.get(Skill.Constitution))
        assertTrue(target.poisoned)
    }

    @Test
    fun `Ruby bolts have chance of forfeiting health`() {
        val player = createPlayer(Skill.Ranged to 99, Skill.Constitution to 990)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "ruby_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(792, target.levels.get(Skill.Constitution))
        assertEquals(891, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Diamond bolts have chance of hitting extra damage`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "diamond_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(979, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Dragon bolts have chance of hitting dragonfire damage`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "dragon_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(782, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Dragon bolts don't hit if immune to dragonfire`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "dragon_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)
        target.antifire(1)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(980, target.levels.get(Skill.Constitution))
    }

    @Test
    fun `Onyx bolts have chance of stealing health`() {
        val player = createPlayer(Skill.Ranged to 99, Skill.Constitution to 990)
        player.levels.set(Skill.Constitution, 500)
        val weapon = Item("rune_crossbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.ammo = "onyx_bolts_e"
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(3)

        assertEquals(978, target.levels.get(Skill.Constitution))
        assertEquals(503, player.levels.get(Skill.Constitution))
    }

}