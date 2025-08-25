@file:Suppress("UnusedReceiverParameter")

package content.achievement

import world.gregs.voidps.engine.entity.character.mode.move.AreaEntered
import world.gregs.voidps.engine.event.handle.Area
import content.entity.npc.shop.OpenShop
import content.skill.prayer.PrayerStart
import content.entity.npc.shop.sell.SoldItem
import world.gregs.voidps.engine.event.handle.Handle
import world.gregs.voidps.engine.entity.character.player.skill.level.MaxLevelChanged
import world.gregs.voidps.engine.event.handle.LevelChange
import content.entity.combat.hit.CombatAttack
import world.gregs.voidps.engine.event.handle.Combat
import content.entity.death.Death
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.event.handle.Variable
import world.gregs.voidps.engine.inv.InventorySlotChanged
import world.gregs.voidps.engine.event.handle.Inventory
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.event.handle.On
import world.gregs.voidps.engine.inv.ItemRemoved
import world.gregs.voidps.engine.inv.ItemAdded
import content.entity.obj.ObjectTeleport
import world.gregs.voidps.engine.event.handle.Option
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.event.handle.Move
import content.entity.combat.killer
import content.skill.melee.weapon.attackStyle
import content.skill.ranged.ammo
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

@Move
fun Moved<Player>.playerRuns(player: Player) {
    if (player.running && !player["on_the_run_task", false]) {
        player["on_the_run_task"] = true
    }
}

@Option("Climb-up", "lumbridge_castle_ladder")
fun ObjectTeleport.climbCastleLadder() {
    player["master_of_all_i_survey_task"] = true
}

private val areas: AreaDefinitions by inject()

@Inventory("copper_ore", inventory = "inventory")
fun ItemAdded.addedCopperOre(player: Player) {
    if (player.softTimers.contains("mining") && player.tile in areas["lumbridge_swamp_east_copper_mine"]) {
        player["take_your_pick_task"] = true
    }
}

@Inventory("logs", inventory = "inventory")
fun ItemAdded.addedLogs(player: Player) {
    if (player.softTimers.contains("woodcutting")) {
        player["adventurers_log_task"] = true
    }
}

@Inventory("raw_crayfish", inventory = "inventory")
fun ItemAdded.addedRawCrayfish(player: Player) {
    if (player.softTimers.contains("fishing")) {
        player["arent_they_supposed_to_be_twins_task"] = true
    }
}

private val objects: GameObjects by inject()

@Inventory("logs", inventory = "inventory")
fun ItemRemoved.burnLogs(player: Player) {
    if (!player["log_a_rhythm_task", false]) {
        player["burnt_regular_log"] = true
        player["fire_tile"] = player.tile
    }
}

@On("firemaking")
fun TimerStop.stopFiremaking(player: Player) {
    val regular: Boolean = player.remove("burnt_regular_log") ?: return
    val tile: Tile = player.remove("fire_tile") ?: return
    if (regular) {
        val fire = objects.getShape(tile, ObjectShape.CENTRE_PIECE_STRAIGHT)
        if (fire != null && fire.id.startsWith("fire_")) {
            player["log_a_rhythm_task"] = true
        }
    }
}

@Inventory("raw_crayfish", inventory = "inventory")
fun ItemRemoved.cookRawCrayfish(player: Player) {
    if (player.inventory[index].id == "crayfish" && player.softTimers.contains("cooking")) {
        player["shellfish_roasting_on_an_open_fire_task"] = true
    }
}

@Inventory("tin_ore", inventory = "inventory")
fun ItemAdded.smeltTin(player: Player) {
    if (player.softTimers.contains("mining")) {
        player["heavy_metal_task"] = true
    }
}

@Inventory("bronze_bar", inventory = "inventory")
fun ItemAdded.smeltBronze(player: Player) {
    if (player.softTimers.contains("smelting")) {
        player["bar_one_task"] = true
    }
}

@Inventory("bronze_dagger", inventory = "inventory")
fun ItemAdded.smithBronzeDagger(player: Player) {
    if (player.softTimers.contains("smithing")) {
        player["cutting_edge_technology_task"] = true
    }
}

private val styleDefinitions: WeaponStyleDefinitions by inject()

@Inventory("worn_equipment")
fun InventorySlotChanged.wornTaskItems(player: Player) {
    when (index) {
        EquipSlot.Feet.index, EquipSlot.Shield.index, EquipSlot.Legs.index, EquipSlot.Chest.index -> {
            if (item.id.contains("iron")) {
                player["alls_ferrous_in_love_and_war_task"] = true
            } else if (item.id.contains("steel")) {
                player["steel_yourself_for_combat_task"] = true
            }
        }
        EquipSlot.Weapon.index -> {
            if (item.id.contains("iron")) {
                player["not_what_we_mean_by_irony_task"] = true
            } else if (item.id.contains("steel")) {
                player["temper_temper_task"] = true
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
                    if (item.id == "oak_shortbow" || item.id == "oak_longbow") {
                        player["heart_of_oak_task"] = true
                    }
                }
            }
        }
        EquipSlot.Ammo.index -> if (item.id == "iron_arrow") {
            player["ammo_ammo_ammo_task"] = true
        }
    }
}

@Inventory("worn_equipment", slots = [EquipSlot.Weapon])
fun InventorySlotChanged.wornWeapon(player: Player) {
    // FIXME
    if (player["armed_and_dangerous_task", false] && player["just_cant_get_the_staff_task", false] && player["reach_out_and_touch_someone_task", false]) {
        return
    }
}

@Variable("task_progress_overall")
fun VariableSet.taskProcessIncreased(player: Player) {
    if ((from == null || from is Int && (from as Int) < 10) && to is Int && (to as Int) >= 10) {
        player["on_your_way_task"] = true
    }
}

@Inventory(inventory = "bank")
fun ItemAdded.toBank(player: Player) {
    val millis = System.currentTimeMillis() - player["creation", 0L]
    if (millis > 1000 && !player["hang_on_to_something_task", false]) {
        player["hang_on_to_something_task"] = true
        player.addVarbit("task_reward_items", "magic_staff")
    }
}

@On("cow*")
fun Death.cowDeath(cow: NPC) {
    val killer = cow.killer
    if (killer is Player) {
        killer["bovine_intervention_task"] = true
    }
}

@Inventory("cowhide", inventory = "inventory")
fun ItemRemoved.tanCowhide(player: Player) {
    if (player.inventory[index].id == "leather") {
        player["tan_your_hide_task"] = true
    }
}

@Inventory("leather_gloves", inventory = "inventory")
fun ItemAdded.craftLeatherGloves(player: Player) {
    if (player.softTimers.contains("item_on_item")) {
        player["handicrafts_task"] = true
    }
}

@Inventory("leather_gloves", slots = [EquipSlot.Hands], inventory = "worn_equipment")
fun ItemAdded.equipLeatherGloves(player: Player) {
    player["handy_dandy_task"] = true
}

@Combat(spell = "wind_strike")
fun CombatAttack.castWindStrike(player: Player) {
    player["death_from_above_task"] = true
}

@Inventory("bread", inventory = "inventory")
fun ItemAdded.cookBread(player: Player) {
    if (player.softTimers.contains("cooking")) {
        player["a_labour_of_loaf_task"] = true
    }
}

// maxLevelChange { player ->
@LevelChange
fun MaxLevelChanged.reachBaseLevels(player: Player) {
    if (!player["on_the_level_task", false] || !player["quarter_centurion_task", false]) {
        val total = Skill.all.sumOf { (if (it == Skill.Constitution) player.levels.getMax(it) / 10 - 10 else player.levels.getMax(it) - 1) }
        if (total == 10) {
            player["on_the_level_task"] = true
        } else if (total == 25) {
            player["quarter_centurion_task"] = true
        }
    }
}

@Inventory("pure_essence", inventory = "inventory")
fun ItemAdded.minePureEssence(player: Player) {
    if (player.softTimers.contains("mining")) {
        player["so_thats_what_ess_stands_for_task"] = true
    }
}

@Inventory("rune_essence", inventory = "inventory")
fun ItemAdded.mineRuneEssence(player: Player) {
    if (player.softTimers.contains("mining")) {
        player["so_thats_what_ess_stands_for_task"] = true
    }
}

@Inventory("air_rune", inventory = "inventory")
fun ItemAdded.craftAirRune(player: Player) {
    if (player.softTimers.contains("runecrafting")) {
        player["air_craft_task"] = true
    }
}

@Handle("item_sold", "*", "*")
fun SoldItem.sellItemInShop(player: Player) {
    player["greasing_the_wheels_of_commerce_task"] = true
}

@On
fun PrayerStart.startAnyPrayer(player: Player) {
    player["put_your_hands_together_for_task"] = true
}

@On("giant_rat*")
fun Death.ratDeath(npc: NPC) {
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

@LevelChange(Skill.Attack, Skill.Defence)
fun MaxLevelChanged.levelUpCombatSkill(player: Player) {
    if (from < 5 && to >= 5 && player.levels.getMax(Skill.Attack) >= 5 && player.levels.getMax(Skill.Defence) >= 5) {
        player["first_blood_task"] = true
    }
}

@Inventory("iron_hatchet", inventory = "inventory")
fun ItemAdded.pickupIronHatchet(player: Player) {
    player["dont_bury_this_one_task"] = true
}

@Inventory("bronze_mace", inventory = "inventory")
fun ItemAdded.smithBronzeMace(player: Player) {
    if (player.softTimers.contains("smithing")) {
        player["mace_invaders_task"] = true
    }
}

@Inventory("bronze_med_helm", inventory = "inventory")
fun ItemAdded.smithBronzeMedHelm(player: Player) {
    if (player.softTimers.contains("smithing")) {
        player["capital_protection_what_task"] = true
    }
}

@Inventory("bronze_full_helm", inventory = "inventory")
fun ItemAdded.smithBronzeFullHelm(player: Player) {
    if (player.softTimers.contains("smithing")) {
        player["capital_protection_what_task"] = true
    }
}

@Inventory("empty_pot", inventory = "inventory")
fun ItemAdded.smithEmptyPotDraynor(player: Player) {
    if (player.softTimers.contains("pottery") && player.tile in areas["draynor"]) {
        player["hotpot_task"] = true
    }
}

@LevelChange(Skill.Mining)
fun MaxLevelChanged.levelFiveMining(player: Player) {
    if (from < 5 && to >= 5) {
        player["hack_and_smash_task"] = true
    }
}

@Inventory("raw_shrimps", inventory = "inventory")
fun ItemAdded.fishLumbridgeShrimps(player: Player) {
    if (player.softTimers.contains("fishing") && player.tile in areas["lumbridge_swamp_fishing_area"]) {
        player["shrimpin_aint_easy_task"] = true
    }
}

@Handle("item_sold", "raw_shrimps", "lumbridge_fishing_supplies")
fun SoldItem.sellRawShrimps(player: Player) {
    player["the_fruit_of_the_sea_task"] = true
}

@Inventory("leather_boots", inventory = "inventory")
fun ItemAdded.craftLeatherBoots(player: Player) {
    if (player.softTimers.contains("item_on_item")) {
        player["made_for_walking_task"] = true
    }
}

@Inventory("raw_sardine", inventory = "inventory")
fun ItemAdded.fishSardine(player: Player) {
    if (player.softTimers.contains("fishing")) {
        player["did_anyone_bring_any_toast_task"] = true
    }
}

@Inventory("raw_herring", inventory = "inventory")
fun ItemRemoved.cookHerring(player: Player) {
    if (player.inventory[index].id == "herring" && player.softTimers.contains("cooking")) {
        player["its_not_a_red_one_task"] = true
    }
}

@Inventory("uncooked_berry_pie", inventory = "inventory")
fun ItemRemoved.cookBerryPie(player: Player) {
    if (player.inventory[index].id == "redberry_pie" && player.softTimers.contains("cooking")) {
        player["berry_tasty_task"] = true
    }
}

@Combat(spell = "confuse")
fun CombatAttack.confuseSpell(player: Player) {
    player["not_so_confusing_after_all_task"] = true
}

@Combat(style = "range")
fun CombatAttack.rangeSteelArrow(player: Player) {
    if (player.ammo == "steel_arrow") {
        player["get_the_point_task"] = true
    }
}

@Variable("quest_points")
fun VariableSet.fourQuestPoints(player: Player) {
    if ((from == null || from is Int && (from as Int) < 4) && to != null && to is Int && to as Int >= 4) {
        player["fledgeling_adventurer_task"] = true
    }
}

@Handle("open_shop", "lumbridge_general_store")
fun OpenShop.openLumbridgeGeneralStore(player: Player) {
    player["window_shopping_task"] = true
}

@Area("freds_farmhouse")
fun AreaEntered.enterFredsFarmhouse() {
    player["wait_thats_not_a_sheep_task"] = true
}

@Area("draynor_manor_courtyard")
fun AreaEntered.enterDraynorManorCourtyard() {
    player["in_the_countyard_task"] = true
}

@Area("draynor_village_market")
fun AreaEntered.enterDraynorVillageMarket() {
    player["beware_of_pigzilla_task"] = true
}

@Area("wizards_tower_top_floor")
fun AreaEntered.enterWizardTowerTopFloor() {
    player["tower_power_task"] = true
}