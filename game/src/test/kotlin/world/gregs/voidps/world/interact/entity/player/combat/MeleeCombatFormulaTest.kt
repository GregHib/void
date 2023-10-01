package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.combat.prayer.PrayerStart
import world.gregs.voidps.world.interact.entity.combat.combatStyle
import world.gregs.voidps.world.interact.entity.combat.getMaximumHit
import world.gregs.voidps.world.interact.entity.combat.getRating
import world.gregs.voidps.world.interact.entity.combat.hitChance
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption
import world.gregs.voidps.world.script.WorldTest
import kotlin.test.assertEquals

internal class MeleeCombatFormulaTest : WorldTest() {

    private data class Results(
        val offensiveRating: Int,
        val defensiveRating: Int,
        val maxHit: Int,
        val chance: Double
    )

    private fun calculate(player: Player, target: Character, type: String, weapon: Item? = null, spell: String = "", special: Boolean = false): Results {
        val offensiveRating = getRating(player, player, type, weapon, special)
        val defensiveRating = getRating(player, target, type, weapon, special)
        val maxHit = getMaximumHit(player, target, type, weapon, spell, special)
        val chance = hitChance(player, target, type, weapon, special)
        return Results(offensiveRating, defensiveRating, maxHit, chance)
    }

    private fun createPlayer(vararg pairs: Pair<Skill, Int>): Player {
        val player = createPlayer("player")
        for ((skill, level) in pairs) {
            player.levels.set(skill, level)
            player.experience.set(skill, PlayerLevels.getExperience(level, skill))
        }
        return player
    }

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
        println(player.combatStyle)

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
        player.equipment.set(EquipSlot.Weapon.index, "rune_battleaxe")
        player.inventory.add("super_attack_4")
        player.inventory.add("super_strength_4")
        player.events.emit(InventoryOption(player, "inventory", Item("super_attack_4"), 0, "Drink"))
        tick(2)
        player.events.emit(InventoryOption(player, "inventory", Item("super_strength_4"), 1, "Drink"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("rune_battleaxe"))

        assertEquals(8774, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(129, maxHit)
        assertEquals(0.9598, chance, 0.0001)
    }

    @Test
    fun `Low prayer boost`() {
        val player = createPlayer(Skill.Attack to 40, Skill.Strength to 40, Skill.Prayer to 40)
        player.events.emit(PrayerStart("improved_reflexes"))
        player.events.emit(PrayerStart("superhuman_strength"))
        player.equipment.set(EquipSlot.Weapon.index, "rune_scimitar")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("rune_scimitar"))

        assertEquals(5995, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(92, maxHit)
        assertEquals(0.9411, chance, 0.0001)
    }

    @Test
    fun `High prayer boost`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99, Skill.Prayer to 70)
        player.events.emit(PrayerStart("piety"))
        player.equipment.set(EquipSlot.Weapon.index, "armadyl_godsword")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("armadyl_godsword"))

        assertEquals(19796, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(400, maxHit)
        assertEquals(0.9822, chance, 0.0001)
    }

    @Test
    fun `Special attack boost`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99, Skill.Prayer to 70)
        player.events.emit(PrayerStart("piety"))
        player.equipment.set(EquipSlot.Weapon.index, "armadyl_godsword")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("armadyl_godsword"), special = true)

        assertEquals(39592, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(550, maxHit)
        assertEquals(0.9911, chance, 0.0001)
    }

    @Test
    fun `Void boost`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Hat.index, "void_melee_helm")
        player.equipment.set(EquipSlot.Legs.index, "void_knight_robe")
        player.equipment.set(EquipSlot.Chest.index, "void_knight_top")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("armadyl_godsword"), special = true)

        assertEquals(39592, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(550, maxHit)
        assertEquals(0.9911, chance, 0.0001)
    }

    @Test
    fun `Darklight on greater demon`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Weapon.index, "darklight")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("darklight"))

        assertEquals(6880, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(212, maxHit)
        assertEquals(0.5813, chance, 0.0001)
    }

}