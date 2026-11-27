package content.bot

import WorldTest
import content.bot.allocation.BotAllocations
import content.bot.allocation.BotProvisioner
import content.bot.allocation.WeightedAllocator
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotInventorySetup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory

/**
 * Clan Wars bots are given the same levels and kit as the old CombatTier/applyTier path gave them.
 * Expected values are a snapshot of applyTier, except multi-item slots which now use the best equippable undegraded item.
 */
class ClanWarsBotsTest : WorldTest() {

    private data class Expected(val role: String, val levels: String, val style: String, val equipment: String, val inventory: String)

    private val expected = listOf(
        Expected(
            "clan_wars_ffa_safe_zerker",
            levels = "Attack=60, Defence=45, Strength=80, Constitution=750, Prayer=95",
            style = "slash",
            equipment = "0:helm_of_neitiznotx1, 1:obsidian_capex1, 2:amulet_of_furyx1, 3:abyssal_whipx1, 4:fighter_torsox1, 7:rune_platelegsx1, 10:dragon_bootsx1, 12:berserker_ringx1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_restore_4x1, super_attack_4x1, super_strength_4x1, super_defence_4x1, dragon_dagger_p++x1, granite_maulx1, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_safe_dharoker",
            levels = "Attack=70, Defence=70, Strength=70, Constitution=700, Prayer=95",
            style = "slash",
            equipment = "0:dharoks_helmx1, 1:obsidian_capex1, 3:dharoks_greataxex1, 4:dharoks_platebodyx1, 7:dharoks_platelegsx1, 10:dragon_bootsx1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_restore_4x1, super_attack_4x1, super_strength_4x1, super_defence_4x1, dwarven_rock_cakex1, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_safe_ags_main",
            levels = "Attack=75, Defence=75, Strength=85, Constitution=850, Prayer=95",
            style = "slash",
            equipment = "0:helm_of_neitiznotx1, 1:obsidian_capex1, 2:amulet_of_furyx1, 3:armadyl_godswordx1, 4:bandos_chestplatex1, 7:bandos_tassetsx1, 10:bandos_bootsx1, 12:berserker_ringx1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_restore_4x1, saradomin_brew_4x1, saradomin_brew_4x1, overload_4x1, abyssal_whipx1, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_safe_obby_pure",
            levels = "Strength=80, Constitution=700",
            style = "crush",
            equipment = "0:bearheadx1, 1:obsidian_capex1, 2:berserker_necklacex1, 3:tzhaar_ket_omx1, 4:iron_platebodyx1, 7:iron_platelegsx1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_strength_4x1, super_strength_4x1, granite_maulx1, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_safe_msb_pure",
            levels = "Constitution=700, Ranged=70, Prayer=95",
            style = "",
            equipment = "0:archer_helmx1, 1:avas_accumulatorx1, 3:magic_shortbowx1, 4:black_dragonhide_bodyx1, 7:black_dragonhide_chapsx1, 9:black_dragonhide_vambracesx1, 10:ranger_bootsx1, 13:rune_arrowx10000",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_ranging_potion_4x1, super_ranging_potion_4x1, rune_arrowx10000, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_safe_karils_tank",
            levels = "Defence=70, Constitution=750, Ranged=75, Prayer=95",
            style = "",
            equipment = "0:karils_coifx1, 1:avas_accumulatorx1, 2:amulet_of_furyx1, 3:rune_crossbowx1, 4:karils_topx1, 7:karils_skirtx1, 9:black_dragonhide_vambracesx1, 10:ranger_bootsx1, 13:runite_boltsx10000",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_restore_4x1, super_ranging_potion_4x1, super_ranging_potion_4x1, runite_boltsx10000, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_safe_ancient_tank",
            levels = "Defence=70, Constitution=800, Prayer=95, Magic=94",
            style = "crush",
            equipment = "0:ahrims_hoodx1, 1:obsidian_capex1, 2:amulet_of_furyx1, 3:ancient_staffx1, 4:ahrims_robe_topx1, 5:zamoraks_book_of_chaosx1, 7:ahrims_robe_skirtx1, 10:infinity_bootsx1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, saradomin_brew_4x1, saradomin_brew_4x1, super_restore_4x1, super_restore_4x1, super_magic_potion_4x1, super_magic_potion_4x1, blood_runex10000, death_runex10000, water_runex10000, chaos_runex10000, air_runex10000, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_safe_ancient_hybrid",
            levels = "Attack=75, Defence=70, Strength=80, Constitution=850, Ranged=75, Prayer=95, Magic=94",
            style = "crush",
            equipment = "0:ahrims_hoodx1, 1:obsidian_capex1, 2:amulet_of_furyx1, 3:ancient_staffx1, 4:ahrims_robe_topx1, 5:zamoraks_book_of_chaosx1, 7:ahrims_robe_skirtx1, 10:infinity_bootsx1, 12:berserker_ringx1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, saradomin_brew_4x1, saradomin_brew_4x1, super_restore_4x1, super_restore_4x1, blood_runex10000, death_runex10000, water_runex10000, chaos_runex10000, air_runex10000, armadyl_godswordx1, dark_bowx1, dragon_arrowx50, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_dangerous_melee",
            levels = "Attack=60, Defence=40, Strength=70, Constitution=700, Prayer=95, Magic=94",
            style = "slash",
            equipment = "0:helm_of_neitiznotx1, 1:obsidian_capex1, 2:amulet_of_glory_4x1, 3:abyssal_whipx1, 4:fighter_torsox1, 7:rune_platelegsx1, 10:dragon_bootsx1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_restore_4x1, super_attack_4x1, super_strength_4x1, super_defence_4x1, death_runex400, astral_runex400, earth_runex400, dragon_dagger_p++x1, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_dangerous_ranged",
            levels = "Defence=40, Constitution=700, Ranged=70, Prayer=95",
            style = "",
            equipment = "0:archer_helmx1, 1:avas_accumulatorx1, 2:amulet_of_glory_4x1, 3:magic_shortbowx1, 4:black_dragonhide_bodyx1, 7:black_dragonhide_chapsx1, 9:black_dragonhide_vambracesx1, 10:snakeskin_bootsx1, 13:rune_arrowx10000",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_ranging_potion_4x1, super_ranging_potion_4x1, rune_arrowx10000, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_dangerous_magic",
            levels = "Defence=40, Constitution=700, Prayer=95, Magic=94",
            style = "crush",
            equipment = "0:mystic_hat_bluex1, 1:saradomin_capex1, 2:amulet_of_glory_4x1, 3:ancient_staffx1, 4:mystic_robe_top_bluex1, 5:zamoraks_book_of_chaosx1, 7:mystic_robe_bottom_bluex1, 9:mystic_gloves_bluex1, 10:mystic_boots_bluex1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_restore_4x1, super_magic_potion_4x1, super_magic_potion_4x1, blood_runex2500, death_runex2500, water_runex10000, chaos_runex2500, air_runex10000, games_necklace_6x1",
        ),
        Expected(
            "clan_wars_ffa_dangerous_hybrid",
            levels = "Attack=75, Defence=60, Strength=80, Constitution=800, Ranged=75, Prayer=95, Magic=94",
            style = "slash",
            equipment = "0:helm_of_neitiznotx1, 1:obsidian_capex1, 2:amulet_of_glory_4x1, 3:abyssal_whipx1, 4:fighter_torsox1, 7:rune_platelegsx1, 10:dragon_bootsx1",
            inventory = "coinsx10000, sharkx1, sharkx1, sharkx1, sharkx1, sharkx1, super_restore_4x1, super_restore_4x1, saradomin_brew_4x1, saradomin_brew_4x1, games_necklace_6x1, ancient_staffx1, mystic_hat_bluex1, mystic_boots_bluex1, mystic_robe_bottom_bluex1, mystic_robe_top_bluex1, blood_runex2000, death_runex2000, water_runex5000",
        ),
    ).associateBy { it.role }

    @Test
    fun `Clan Wars roles are provisioned with the same levels and kit as before`() {
        val manager = scripts.filterIsInstance<BotCommands>().single().manager
        val provisioner = BotProvisioner(manager::activity)
        val seen = mutableSetOf<String>()
        for (id in listOf("clan_wars_ffa_safe", "clan_wars_ffa_dangerous")) {
            val allocation = BotAllocations.get(id) as WeightedAllocator
            for (role in allocation.roles) {
                val player = createPlayer(name = role.id.takeLast(12))
                val bot = Bot(player)
                player["bot"] = bot
                provisioner.provision(bot, role, allocation.config.starterItems)
                val exp = expected[role.activity] ?: error("No snapshot for ${role.activity}")
                seen.add(role.activity)
                assertEquals(exp.levels, Skill.all.filter { player.levels.getMax(it) > 1 || it == Skill.Constitution }.joinToString { "${it.name}=${player.levels.getMax(it)}" }, role.id)
                assertEquals(exp.style, player["combat_style", ""], role.id)
                assertEquals(exp.equipment, player.equipment.items.withIndex().filter { it.value.isNotEmpty() }.joinToString { "${it.index}:${it.value.id}x${it.value.amount}" }, role.id)
                assertEquals(exp.inventory, player.inventory.items.filter { it.isNotEmpty() }.joinToString { "${it.id}x${it.amount}" }, role.id)
                if (role.activity == "clan_wars_ffa_dangerous_hybrid") {
                    assertEquals("melee", player["current_loadout", ""])
                }
                // Pinned bots must not need to bank: every setup row except the arena should already pass
                val activity = manager.bind(bot, manager.activity(role.activity)!!)
                for (condition in activity.setup) {
                    if (condition is BotInArea) continue
                    // Obby pures carry a granite maul they don't have the attack level to wield (same as before the move to loadouts)
                    if (role.id == "clan_wars_safe_obby_pure" && condition is BotInventorySetup) continue
                    assertTrue(condition.check(player)) { "${role.id} setup $condition not met" }
                }
            }
        }
        assertEquals(expected.keys, seen)
    }

    @Test
    fun `Only dangerous bots restock on teleporting out`() {
        assertEquals(setOf("clan_wars_teleport"), BotAllocations.get("clan_wars_ffa_dangerous").config.restockAreas)
        assertTrue(BotAllocations.get("clan_wars_ffa_safe").config.restockAreas.isEmpty())
    }
}
