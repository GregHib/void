package content.achievement

import content.entity.combat.killer
import content.entity.npc.shop.shop
import content.skill.melee.weapon.attackStyle
import content.skill.prayer.PrayerApi
import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class LumbridgeBeginnerTasks(
    val styleDefinitions: WeaponStyleDefinitions,
) : Script,
    PrayerApi {

    init {
        timerStop("firemaking") {
            val regular: Boolean = remove("burnt_regular_log") ?: return@timerStop
            val tile: Tile = remove("fire_tile") ?: return@timerStop
            if (regular) {
                val fire = GameObjects.getShape(tile, ObjectShape.CENTRE_PIECE_STRAIGHT)
                if (fire != null && fire.id.startsWith("fire_")) {
                    this["log_a_rhythm_task"] = true
                }
            }
        }

        variableSet("task_progress_overall,quest_points") { key, from, to ->
            if (key == "task_progress_overall" && (from == null || from is Int && from < 10) && to is Int && to >= 10) {
                set("on_your_way_task", true)
            } else if (key == "quest_points" && (from == null || from is Int && from < 4) && to != null && to is Int && to >= 4) {
                set("fledgeling_adventurer_task", true)
            }
        }

        moved { _ ->
            if (running && !get("on_the_run_task", false)) {
                set("on_the_run_task", true)
            }
        }

        objTeleportLand("Climb-up", "lumbridge_castle_ladder") { _, _ ->
            set("master_of_all_i_survey_task", true)
        }

        itemAdded("copper_ore", inventory = "inventory") {
            if (softTimers.contains("mining") && tile in Areas["lumbridge_swamp_east_copper_mine"]) {
                set("take_your_pick_task", true)
            }
        }

        itemAdded("logs", inventory = "inventory") {
            if (softTimers.contains("woodcutting")) {
                set("adventurers_log_task", true)
            }
        }

        itemAdded("raw_crayfish", inventory = "inventory") {
            if (softTimers.contains("fishing")) {
                set("arent_they_supposed_to_be_twins_task", true)
            }
        }

        itemRemoved("logs", inventory = "inventory") {
            if (!get("log_a_rhythm_task", false)) {
                set("burnt_regular_log", true)
                set("fire_tile", tile)
            }
        }

        itemRemoved("raw_crayfish", inventory = "inventory") {
            if (inventory[it.index].id == "crayfish" && softTimers.contains("cooking")) {
                set("shellfish_roasting_on_an_open_fire_task", true)
            }
        }

        itemAdded("tin_ore", inventory = "inventory") {
            if (softTimers.contains("mining")) {
                set("heavy_metal_task", true)
            }
        }

        itemAdded("bronze_bar", inventory = "inventory") {
            if (softTimers.contains("smelting")) {
                set("bar_one_task", true)
            }
        }

        itemAdded("bronze_dagger", inventory = "inventory") {
            if (softTimers.contains("smithing")) {
                set("cutting_edge_technology_task", true)
            }
        }

        slotChanged("worn_equipment") {
            val (_, index, item) = it
            when (index) {
                EquipSlot.Feet.index, EquipSlot.Shield.index, EquipSlot.Legs.index, EquipSlot.Chest.index -> {
                    if (item.id.contains("iron")) {
                        set("alls_ferrous_in_love_and_war_task", true)
                    } else if (item.id.contains("steel")) {
                        set("steel_yourself_for_combat_task", true)
                    }
                }
                EquipSlot.Weapon.index -> {
                    if (item.id.contains("iron")) {
                        set("not_what_we_mean_by_irony_task", true)
                    } else if (item.id.contains("steel")) {
                        set("temper_temper_task", true)
                    }
                    val id = item.def["weapon_style", -1]
                    when (val style = styleDefinitions.get(id).stringId) {
                        "staff" -> set("just_cant_get_the_staff_task", true)
                        "axe", "pickaxe", "dagger", "sword", "2h", "mace", "claws", "hammer", "whip", "spear", "halberd", "ivandis_flail", "salamander" -> {
                            set("armed_and_dangerous_task", true)
                        }
                        "bow", "crossbow", "thrown", "chinchompa", "sling" -> {
                            set("reach_out_and_touch_someone_task", true)
                            if (!get("take_a_bow_task", false)) {
                                if (style == "bow") {
                                    if (item.id.contains("longbow")) {
                                        set("equip_longbow", true)
                                    } else {
                                        set("equip_shortbow", true)
                                    }
                                } else if (style == "crossbow") {
                                    set("equip_crossbow", true)
                                }
                                if (get("equip_shortbow", false) || get("equip_longbow", false) || get("equip_crossbow", false)) {
                                    set("take_a_bow_task", true)
                                    clear("equip_shortbow")
                                    clear("equip_longbow")
                                    clear("equip_crossbow")
                                }
                            }
                            if (item.id == "oak_shortbow" || item.id == "oak_longbow") {
                                set("heart_of_oak_task", true)
                            }
                        }
                    }
                }
                EquipSlot.Ammo.index -> if (item.id == "iron_arrow") {
                    set("ammo_ammo_ammo_task", true)
                }
            }
        }

        itemAdded(inventory = "bank") {
            val millis = System.currentTimeMillis() - get("creation", 0L)
            if (millis > 1000 && !get("hang_on_to_something_task", false)) {
                set("hang_on_to_something_task", true)
                addVarbit("task_reward_items", "magic_staff")
            }
        }

        npcDeath("cow*") {
            val killer = killer
            if (killer is Player) {
                killer["bovine_intervention_task"] = true
            }
        }

        itemRemoved("cowhide", inventory = "inventory") {
            if (inventory[it.index].id == "leather") {
                set("tan_your_hide_task", true)
            }
        }

        itemAdded("leather_gloves", inventory = "inventory") {
            if (softTimers.contains("item_on_item")) {
                set("handicrafts_task", true)
            }
        }

        itemAdded("leather_gloves", "worn_equipment", EquipSlot.Hands) {
            set("handy_dandy_task", true)
        }

        combatAttack("magic") {
            if (it.spell == "wind_strike") {
                set("death_from_above_task", true)
            }
        }

        itemAdded("bread", inventory = "inventory") {
            if (softTimers.contains("cooking")) {
                set("a_labour_of_loaf_task", true)
            }
        }

        maxLevelChanged { _, _, _ ->
            if (!get("on_the_level_task", false) || !get("quarter_centurion_task", false)) {
                val total = Skill.all.sumOf { (if (it == Skill.Constitution) levels.getMax(it) / 10 - 10 else levels.getMax(it) - 1) }
                AuditLog.event(this, "total_level_up", total)
                if (total == 10) {
                    set("on_the_level_task", true)
                } else if (total == 25) {
                    set("quarter_centurion_task", true)
                }
            }
        }

        itemAdded("pure_essence", inventory = "inventory") {
            if (softTimers.contains("mining")) {
                set("so_thats_what_ess_stands_for_task", true)
            }
        }

        itemAdded("rune_essence", inventory = "inventory") {
            if (softTimers.contains("mining")) {
                set("so_thats_what_ess_stands_for_task", true)
            }
        }

        itemAdded("air_rune", inventory = "inventory") {
            if (softTimers.contains("runecrafting")) {
                set("air_craft_task", true)
            }
        }

        sold {
            set("greasing_the_wheels_of_commerce_task", true)
        }

        prayerStart {
            set("put_your_hands_together_for_task", true)
        }

        npcDeath("giant_rat*") {
            val killer = killer
            if (killer is Player && !killer["am_i_a_blademaster_yet_task", false]) {
                when (val style = killer.attackStyle) {
                    "aggressive" -> killer["giant_rat_$style"] = true
                    "controlled" -> killer["giant_rat_$style"] = true
                    "defensive" -> killer["giant_rat_$style"] = true
                }
                if (killer["giant_rat_aggressive", false] && killer["giant_rat_controlled", false] && killer["giant_rat_defensive", false]) {
                    killer["am_i_a_blademaster_yet_task"] = true
                    killer.clear("giant_rat_aggressive")
                    killer.clear("giant_rat_controlled")
                    killer.clear("giant_rat_defensive")
                }
            }
        }

        maxLevelChanged(Skill.Attack, ::firstBlood)
        maxLevelChanged(Skill.Defence, ::firstBlood)

        itemAdded("iron_hatchet", inventory = "inventory") {
            set("dont_bury_this_one_task", true)
        }

        itemAdded("bronze_mace", inventory = "inventory") {
            if (softTimers.contains("smithing")) {
                set("mace_invaders_task", true)
            }
        }

        itemAdded("bronze_med_helm", inventory = "inventory") {
            if (softTimers.contains("smithing")) {
                set("capital_protection_what_task", true)
            }
        }

        itemAdded("bronze_full_helm", inventory = "inventory") {
            if (softTimers.contains("smithing")) {
                set("capital_protection_what_task", true)
            }
        }

        itemAdded("empty_pot", inventory = "inventory") {
            if (softTimers.contains("pottery") && tile in Areas["draynor"]) {
                set("hotpot_task", true)
            }
        }

        maxLevelChanged(Skill.Mining) { _, from, to ->
            if (from < 5 && to >= 5) {
                set("hack_and_smash_task", true)
            }
        }

        itemAdded("raw_shrimps", inventory = "inventory") {
            if (softTimers.contains("fishing") && tile in Areas["lumbridge_swamp_fishing_area"]) {
                set("shrimpin_aint_easy_task", true)
            }
        }

        sold("raw_shrimps") {
            if (shop() == "lumbridge_fishing_supplies") {
                set("the_fruit_of_the_sea_task", true)
            }
        }

        itemAdded("leather_boots", inventory = "inventory") {
            if (softTimers.contains("item_on_item")) {
                set("made_for_walking_task", true)
            }
        }

        itemAdded("raw_sardine", inventory = "inventory") {
            if (softTimers.contains("fishing")) {
                set("did_anyone_bring_any_toast_task", true)
            }
        }

        itemRemoved("raw_herring", inventory = "inventory") {
            if (inventory[it.index].id == "herring" && softTimers.contains("cooking")) {
                set("its_not_a_red_one_task", true)
            }
        }

        itemRemoved("uncooked_berry_pie", inventory = "inventory") {
            if (inventory[it.index].id == "redberry_pie" && softTimers.contains("cooking")) {
                set("berry_tasty_task", true)
            }
        }

        combatAttack("magic") {
            if (it.spell == "confuse") {
                set("not_so_confusing_after_all_task", true)
            }
        }

        combatAttack("range") {
            if (ammo == "steel_arrow") {
                set("get_the_point_task", true)
            }
        }

        shopOpen("lumbridge_general_store") {
            set("window_shopping_task", true)
        }

        entered("freds_farmhouse") {
            set("wait_thats_not_a_sheep_task", true)
        }

        entered("draynor_manor_courtyard") {
            set("in_the_countyard_task", true)
        }

        entered("draynor_village_market") {
            set("beware_of_pigzilla_task", true)
        }

        entered("wizards_tower_top_floor") {
            set("tower_power_task", true)
        }
    }

    fun firstBlood(player: Player, skill: Skill, from: Int, to: Int) {
        if (from < 5 && to >= 5 && player.levels.getMax(Skill.Attack) >= 5 && player.levels.getMax(Skill.Defence) >= 5) {
            player["first_blood_task"] = true
        }
    }
}
