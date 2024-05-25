package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.maxLevelChange
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemChange
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.engine.inv.itemReplaced
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.attackStyle
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.killer
import world.gregs.voidps.world.interact.entity.death.npcDeath
import world.gregs.voidps.world.interact.entity.obj.teleportLand
import world.gregs.voidps.world.interact.entity.player.combat.prayer.prayerStart

move({ player.running && !player["on_the_run_task", false] }) {
    player["on_the_run_task"] = true
}

teleportLand("Climb-up", "lumbridge_castle_ladder") {
    player["master_of_all_i_survey_task"] = true
}

val areas: AreaDefinitions by inject()

itemAdded("copper_ore", inventory = "inventory") { player ->
    if (player.softTimers.contains("mining") && player.tile in areas["lumbridge_swamp_east_copper_mine"]) {
        player["take_your_pick_task"] = true
    }
}

itemAdded("logs", inventory = "inventory") { player ->
    if (player.softTimers.contains("woodcutting")) {
        player["adventurers_log_task"] = true
    }
}

itemAdded("crayfish", inventory = "inventory") { player ->
    if (player.softTimers.contains("fishing")) {
        player["arent_they_supposed_to_be_twins_task"] = true
    }
}

val objects: GameObjects by inject()

itemRemoved("logs", inventory = "inventory") { player ->
    if (player.softTimers.contains("firemaking") && !player["log_a_rhythm_task", false]) {
        player["burnt_regular_log"] = true
    }
}

timerStop("firemaking") { player ->
    val regular: Boolean = player.remove("burnt_regular_log") ?: return@timerStop
    if (regular) {
        val fire = objects.getShape(player.previousTile, ObjectShape.CENTRE_PIECE_STRAIGHT)
        if (fire != null && fire.id.startsWith("fire_")) {
            player["log_a_rhythm_task"] = true
        }
    }
}

itemReplaced("raw_crayfish", "crayfish", "inventory") { player ->
    if (player.softTimers.contains("cooking")) {
        player["shellfish_roasting_on_an_open_fire_task"] = true
    }
}

itemAdded("tin_ore", inventory = "inventory") { player ->
    if (player.softTimers.contains("mining")) {
        player["heavy_metal_task"] = true
    }
}

itemAdded("bronze_bar", inventory = "inventory") { player ->
    if (player.softTimers.contains("smelting")) {
        player["bar_one_task"] = true
    }
}

itemAdded("bronze_dagger", inventory = "inventory") { player ->
    if (player.softTimers.contains("smithing")) {
        player["cutting_edge_technology"] = true
    }
}

val styleDefinitions: WeaponStyleDefinitions by inject()

itemChange("worn_equipment") { player ->
    when (index) {
        EquipSlot.Feet.index, EquipSlot.Shield.index, EquipSlot.Legs.index, EquipSlot.Chest.index -> {
            if (item.id.contains("iron")) {
                player["alls_ferrous_in_love_and_war_task"] = true
            }
        }
        EquipSlot.Weapon.index -> {
            if (item.id.contains("iron")) {
                player["not_what_we_mean_by_irony_task"] = true
            } else if (item.id.contains("steel")) {
                player["steel_yourself_for_combat_task"] = true
            }
            val id = item.def["weapon_style", -1]
            when (val style = styleDefinitions.get(id).stringId) {
                "staff" -> player["just_cant_get_the_staff_task"] = true
                "axe", "pickaxe", "dagger", "sword", "2h", "mace", "claws", "hammer", "whip", "spear", "halberd", "ivandis_flail", "salamander" -> {
                    player["armed_and_dangerous_task"] = true
                }
                "bow", "crossbow", "thrown", "chinchompa", "sling" -> {
                    player["reach_out_and_touch_someone_task"] = true
                    if (!player["take_a_bow_task", false]) {
                        if (style == "bow") {
                            if (item.id.contains("longbow")) {
                                player["equip_longbow"] = true
                            } else {
                                player["equip_shortbow"] = true
                            }
                        } else if (style == "crossbow") {
                            player["equip_crossbow"] = true
                        }
                        if (player["equip_shortbow", false] || player["equip_longbow", false] || player["equip_crossbow", false]) {
                            player["take_a_bow_task"] = true
                            player.clear("equip_shortbow")
                            player.clear("equip_longbow")
                            player.clear("equip_crossbow")
                        }
                    }
                }
            }
        }
        EquipSlot.Ammo.index -> if (item.id == "steel_arrow") {
            player["ammo_ammo_ammo_task"] = true
        }
    }
}

itemChange("worn_equipment", EquipSlot.Weapon.index) { player ->
    if (player["armed_and_dangerous_task", false] && player["just_cant_get_the_staff_task", false] && player["reach_out_and_touch_someone_task", false]) {
        return@itemChange
    }
}

variableSet("task_progress_overall", from = 9, to = 10) { player ->
    player["on_your_way_task"] = true
}

itemAdded(inventory = "bank") { player ->
    player["hang_on_to_something_task"] = true
}

npcDeath("cow*") { cow ->
    val killer = cow.killer
    if (killer is Player) {
        killer["bovine_intervention_task"] = true
    }
}

itemReplaced("cowhide", "leather") { player ->
    player["tan_your_hide_task"] = true
}

itemAdded("leather_gloves", inventory = "inventory") { player ->
    if (player.softTimers.contains("item_on_item")) {
        player["handicrafts_task"] = true
    }
}

itemChange(item = "leather_gloves", index = EquipSlot.Hands.index, inventory = "worn_equipment") { player ->
    player["handy_dandy_task"] = true
}

combatAttack(spell = "wind_strike") { player ->
    player["death_from_above_task"] = true
}

itemReplaced(to = "bread", inventory = "inventory") { player ->
    if (player.softTimers.contains("cooking")) {
        player["a_labour_of_loaf_task"] = true
    }
}

maxLevelChange { player ->
    if (!player["on_the_level_task", false]) {
        val total = Skill.all.sumOf { if (it == Skill.Constitution) player.levels.getMax(it) / 10 else player.levels.getMax(it) }
        if (total == 10) {
            player["on_the_level_task"] = true
        }
    }
}

itemAdded("pure_essence", inventory = "inventory") { player ->
    if (player.softTimers.contains("mining")) {
        player["so_thats_what_ess_stands_for_task"] = true
    }
}

itemAdded("rune_essence", inventory = "inventory") { player ->
    if (player.softTimers.contains("mining")) {
        player["so_thats_what_ess_stands_for_task"] = true
    }
}

itemAdded("air_rune", inventory = "inventory") { player ->
    if (player.softTimers.contains("runecrafting")) {
        player["air_craft_task"] = true
    }
}

prayerStart { player ->
    player["put_your_hands_together_for_task"] = true
}

npcDeath("giant_rat*") { npc ->
    val killer = npc.killer
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

maxLevelChange(Skill.Attack, Skill.Defence, to = 5) { player ->
    if (player.levels.getMax(Skill.Attack) == 5 && player.levels.getMax(Skill.Defence) == 5) {
        player["first_blood_task"] = true
    }
}

itemAdded("iron_hatchet", inventory = "inventory") { player ->
    player["dont_bury_this_one_task"] = true
}

itemAdded("bronze_mace", inventory = "inventory") { player ->
    if (player.softTimers.contains("smithing")) {
        player["mace_invaders_task"] = true
    }
}

itemAdded("bronze_med_helm", inventory = "inventory") { player ->
    if (player.softTimers.contains("smithing")) {
        player["capital_protection_what_task"] = true
    }
}

itemAdded("bronze_full_helm", inventory = "inventory") { player ->
    if (player.softTimers.contains("smithing")) {
        player["capital_protection_what_task"] = true
    }
}

itemAdded("empty_pot", inventory = "inventory") { player ->
    if (player.softTimers.contains("pottery") && player.tile in areas["draynor"]) {
        player["hotpot_task"] = true
    }
}

maxLevelChange(Skill.Mining, to = 5) { player ->
    player["hack_and_smash_task"] = true
}

itemAdded("raw_shrimps", inventory = "inventory") { player ->
    if (player.softTimers.contains("fishing") && player.tile in areas["lumbridge_swamp_fishing_area"]) {
        player["shrimpin_aint_easy_task"] = true
    }
}

itemAdded("raw_shrimps", inventory = "lumbridge_fishing_supplies") { player ->
    player["the_fruit_of_the_sea_task"] = true
}

itemAdded("leather_boots", inventory = "inventory") { player ->
    if (player.softTimers.contains("item_on_item")) {
        player["made_for_walking_task"] = true
    }
}

itemAdded("raw_sardine", inventory = "inventory") { player ->
    if (player.softTimers.contains("fishing")) {
        player["did_anyone_bring_any_toast_task"] = true
    }
}

itemReplaced("raw_herring", "herring", "inventory") { player ->
    if (player.softTimers.contains("cooking")) {
        player["its_not_a_red_one_task"] = true
    }
}

combatAttack(spell = "confuse") { player ->
    player["not_so_confusing_after_all_task"] = true
}