package content.skill.magic

import content.skill.melee.CombatFormulaTest
import content.skill.prayer.PrayerConfigs
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.InterfaceApi
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.test.assertEquals

internal class MagicCombatFormulaTest : CombatFormulaTest() {

    @Test
    fun `Maxed player fire surge a rat`() {
        val player = createPlayer(Skill.Magic to 99)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "fire_wave")

        assertEquals(6912, offensiveRating)
        assertEquals(640, defensiveRating)
        assertEquals(200, maxHit)
        assertEquals(0.9536, chance, 0.0001)
    }

    @Test
    fun `83 magic ice blitz on cow`() {
        val player = createPlayer(Skill.Magic to 82)
        val npc = createNPC("cow_default")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "ice_blitz")

        assertEquals(5824, offensiveRating)
        assertEquals(430, defensiveRating)
        assertEquals(260, maxHit)
        assertEquals(0.9629, chance, 0.0001)
    }

    @Test
    fun `Cast on player`() {
        val player = createPlayer(Skill.Magic to 99)
        val target = createPlayer(Skill.Constitution to 990)

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, target, "magic", spell = "fire_wave")

        assertEquals(6912, offensiveRating)
        assertEquals(512, defensiveRating)
        assertEquals(200, maxHit)
        assertEquals(0.9628, chance, 0.0001)
    }

    @Test
    fun `Water staff blast on rat`() {
        val player = createPlayer(Skill.Magic to 50)
        val staff = Item("mystic_steam_staff")
        player.equipment.set(EquipSlot.Weapon.index, staff.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", staff, "water_blast")

        assertEquals(4366, offensiveRating)
        assertEquals(640, defensiveRating)
        assertEquals(140, maxHit)
        assertEquals(0.9265, chance, 0.0001)
    }

    @Test
    fun `Magic potion boost`() {
        val player = createPlayer(Skill.Magic to 99)
        val weapon = Item("mystic_steam_staff")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val potion = Item("super_magic_potion_4")
        player.inventory.add(potion.id)
        InterfaceApi.itemOption(player, "Drink", potion)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", weapon, "water_blast")

        assertEquals(9398, offensiveRating)
        assertEquals(640, defensiveRating)
        assertEquals(140, maxHit)
        assertEquals(0.9658, chance, 0.0001)
    }

    @Test
    fun `Low prayer boost`() {
        val player = createPlayer(Skill.Magic to 25, Skill.Prayer to 27)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "mystic_lore")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "earth_strike")

        assertEquals(2304, offensiveRating)
        assertEquals(640, defensiveRating)
        assertEquals(60, maxHit)
        assertEquals(0.8607, chance, 0.0001)
    }

    @Test
    fun `High prayer and potion boost`() {
        val player = createPlayer(Skill.Magic to 99, Skill.Prayer to 99)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "augury")
        val weapon = Item("staff_of_light")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val potion = Item("super_magic_potion_4")
        player.inventory.add(potion.id)
        InterfaceApi.itemOption(player, "Drink", potion)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", weapon, "fire_wave")

        assertEquals(12636, offensiveRating)
        assertEquals(640, defensiveRating)
        assertEquals(230, maxHit)
        assertEquals(0.9746, chance, 0.0001)
    }

    @Test
    fun `Chaos gauntlet max hit`() {
        val player = createPlayer(Skill.Magic to 75)
        player.equipment.set(EquipSlot.Hands.index, "chaos_gauntlets")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "earth_bolt")

        assertEquals(5376, offensiveRating)
        assertEquals(540, defensiveRating)
        assertEquals(140, maxHit)
        assertEquals(0.9496, chance, 0.0001)
    }
}
