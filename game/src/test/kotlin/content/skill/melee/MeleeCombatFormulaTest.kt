package content.skill.melee

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.skill.prayer.PrayerConfigs
import content.entity.player.inv.InventoryOption
import kotlin.test.assertEquals

internal class MeleeCombatFormulaTest : CombatFormulaTest() {

    @Test
    fun `Maxed player punching a rat`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99)
        val npc = createNPC("rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(7040, offensiveRating)
        assertEquals(220, defensiveRating)
        assertEquals(112, maxHit)
        assertEquals(0.9842, chance, 0.0001)
    }

    @Test
    fun `80 strength player punching a cow`() {
        val player = createPlayer(Skill.Strength to 80)
        val npc = createNPC("cow")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(768, offensiveRating)
        assertEquals(430, defensiveRating)
        assertEquals(93, maxHit)
        assertEquals(0.7191, chance, 0.0001)
    }

    @Test
    fun `Level 13 bronze scimitar on a giant rat`() {
        val player = createPlayer(Skill.Attack to 20, Skill.Strength to 20)
        player.equipment.set(EquipSlot.Weapon.index, "bronze_scimitar")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("bronze_scimitar"))

        assertEquals(2201, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(35, maxHit)
        assertEquals(0.8397, chance, 0.0001)
    }

    @Test
    fun `Level 37 crush attack style`() {
        val player = createPlayer(Skill.Attack to 55, Skill.Strength to 60)
        player["attack_style_axe"] = 2
        player.equipment.set(EquipSlot.Weapon.index, "rune_battleaxe")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("rune_battleaxe"))

        assertEquals(6741, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(147, maxHit)
        assertEquals(0.9476, chance, 0.0001)
    }

    @Test
    fun `Melee super potion boost`() {
        val player = createPlayer(Skill.Attack to 60, Skill.Strength to 40)
        player["attack_style_axe"] = 2
        val weapon = Item("rune_battleaxe")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val attackPotion = Item("super_attack_4")
        val strengthPotion = Item("super_strength_4")
        player.inventory.add(attackPotion.id)
        player.inventory.add(strengthPotion.id)
        player.emit(InventoryOption(player, "inventory", attackPotion, 0, "Drink"))
        tick(2)
        player.emit(InventoryOption(player, "inventory", strengthPotion, 1, "Drink"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(8774, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(129, maxHit)
        assertEquals(0.9598, chance, 0.0001)
    }

    @Test
    fun `Low prayer boost`() {
        val player = createPlayer(Skill.Attack to 40, Skill.Strength to 40, Skill.Prayer to 40)
        val weapon = Item("rune_scimitar")
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "improved_reflexes")
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "superhuman_strength")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(5995, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(92, maxHit)
        assertEquals(0.9411, chance, 0.0001)
    }

    @Test
    fun `High prayer boost`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99, Skill.Prayer to 70)
        val weapon = Item("armadyl_godsword")
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "piety")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(19796, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(400, maxHit)
        assertEquals(0.9822, chance, 0.0001)
    }

    @Test
    fun `Turmoil prayer boost`() {
        val player = createPlayer(Skill.Attack to 99, Skill.Strength to 99, Skill.Prayer to 99)
        val weapon = Item("armadyl_godsword")
        player[PrayerConfigs.PRAYERS] = "curses"
        player.addVarbit(PrayerConfigs.ACTIVE_CURSES, "turmoil")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(26460, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(421, maxHit)
        assertEquals(0.8911, chance, 0.0001)
    }

    @Test
    fun `Special attack boost`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99, Skill.Prayer to 70)
        val weapon = Item("armadyl_godsword")
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "piety")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon, special = true)

        assertEquals(39592, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(550, maxHit)
        assertEquals(0.9911, chance, 0.0001)
    }

}