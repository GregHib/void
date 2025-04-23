package content.achievement

import FakeRandom
import WorldTest
import dialogueContinue
import dialogueOption
import interfaceOption
import itemOnItem
import itemOnObject
import itemOption
import kotlinx.coroutines.test.runTest
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import walk
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterfaceNPC
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LumbridgeBeginnerTasksTest : WorldTest() {

    override var loadNpcs = true

    @Test
    fun `On the Run`() = runTest {
        val player = createPlayer("adventurer", emptyTile)

        player.running = true
        player.instructions.send(Walk(emptyTile.x, emptyTile.y + 2))
        tick()

        assertTrue(player["on_the_run_task", false])
    }

    @Test
    fun `A World in Microcosm`() = runTest {
        val player = createPlayer("adventurer", emptyTile)

        player.instructions.send(Walk(emptyTile.x + 1, emptyTile.y + 1, minimap = true))
        tick()

        assertTrue(player["a_world_in_microcosm_task", false])
    }

    @Test
    fun `Master of All I survey`() = runTest {
        val player = createPlayer("adventurer", Tile(3207, 3224, 2))
        val ladder = objects[Tile(3207, 3223, 2), "lumbridge_castle_ladder"]!!

        player.objectOption(ladder, "Climb-up")
        tick(4)

        assertTrue(player["master_of_all_i_survey_task", false])
    }

    @Test
    fun `Raise the Roof`() = runTest {
        val player = createPlayer("adventurer", Tile(3209, 3217, 3))
        val ladder = objects[Tile(3210, 3218, 3), "lumbridge_flag"]!!

        player.objectOption(ladder, "Raise")
        tick(25)

        assertTrue(player["raise_the_roof_task", false])
    }

    @Test
    fun `Take Your Pick`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 256) until else 0
        })
        val player = createPlayer("adventurer", Tile(3229, 3147))
        player.levels.set(Skill.Mining, 100)
        val rocks = objects[Tile(3230, 3147), "copper_rocks_rock_1"]!!
        player.inventory.add("bronze_pickaxe")

        player.objectOption(rocks, "Mine")
        tick(9)

        assertTrue(player["take_your_pick_task", false])
    }

    @Test
    fun `Adventurer's Log`() {
        val player = createPlayer("adventurer", Tile(3233, 3215))
        player.levels.set(Skill.Woodcutting, 100)
        val tree = objects[Tile(3233, 3216), "tree_4"]!!
        player.inventory.add("bronze_hatchet")

        player.objectOption(tree, "Chop down")
        tick(5)

        assertTrue(player["adventurers_log_task", false])
    }

    @Test
    fun `Aren't they supposed to be twins`() {
        val player = createPlayer("adventurer", Tile(3258, 3205))
        val fishingSpot = createNPC("fishing_spot_crayfish_lumbridge", Tile(3259, 3205))
        player.inventory.add("crayfish_cage")

        player.npcOption(fishingSpot, "Cage")
        tick(7)

        assertTrue(player["arent_they_supposed_to_be_twins_task", false])
    }

    @Test
    fun `Log-a-rhythm`() {
        val player = createPlayer("adventurer", Tile(3235, 3220))
        player.levels.set(Skill.Firemaking, 100)
        player.inventory.add("tinderbox", "logs")

        player.itemOnItem(0, 1)
        tick(6)

        assertTrue(player["log_a_rhythm_task", false])
    }

    @Test
    fun `Shellfish Roasting on an Open Fire`() {
        val player = createPlayer("adventurer", Tile(3079, 3444))
        player.inventory.add("raw_crayfish")
        val fire = objects[Tile(3079, 3445), "fire_orange"]!!

        player.itemOnObject(fire, 0)
        tick(4)

        assertTrue(player["shellfish_roasting_on_an_open_fire_task", false])
    }

    @Test
    fun `Heavy Metal`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 256) until else 0
        })
        val player = createPlayer("adventurer", Tile(3225, 3147))
        player.levels.set(Skill.Mining, 100)
        val rocks = objects[Tile(3225, 3148), "tin_rocks_rock_1"]!!
        player.inventory.add("bronze_pickaxe")

        player.objectOption(rocks, "Mine")
        tick(9)

        assertTrue(player["heavy_metal_task", false])
    }

    @Test
    fun `Bar One`() {
        val player = createPlayer("adventurer", Tile(3227, 3255))
        player.levels.set(Skill.Smithing, 100)
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!
        player.inventory.add("copper_ore", "tin_ore")

        player.itemOnObject(furnace, 0)
        tick()
        player.interfaceOption("skill_creation_amount", "increment")
        player.dialogueOption(id = "dialogue_skill_creation", component = "choice1")
        tick(4)

        assertTrue(player["bar_one_task", false])
    }

    @Test
    fun `Cutting Edge Technology`() {
        val player = createPlayer("adventurer", Tile(3228, 3254))
        val anvil = objects[Tile(3229, 3254), "anvil_lumbridge"]!!
        player.inventory.add("bronze_bar", "hammer")

        player.itemOnObject(anvil, 0)
        tick()
        player.interfaceOption("smithing", "dagger_1", "Make 1 Dagger")
        tick(3)

        assertTrue(player["cutting_edge_technology_task", false])
    }

    @Test
    fun `Armed and Dangerous`() {
        val player = createPlayer("adventurer")
        player.inventory.add("bronze_dagger")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("bronze_dagger"), 0)

        assertTrue(player["armed_and_dangerous_task", false])
    }

    @Test
    fun `On Your Way`() {
        val player = createPlayer("adventurer")
        player["a_world_in_microcosm_task"] = true
        player["master_of_all_i_survey_task"] = true
        player["raise_the_roof_task"] = true
        player["take_your_pick_task"] = true
        player["adventurers_log_task"] = true
        player["arent_they_supposed_to_be_twins_task"] = true
        player["log_a_rhythm_task"] = true
        player["shellfish_roasting_on_an_open_fire_task"] = true
        player["heavy_metal_task"] = true
        player["bar_one_task"] = true
        player["armed_and_dangerous_task"] = true

        assertTrue(player["on_your_way_task", false])
    }

    @Test
    fun `You Can Bank on Us`() {
        val player = createPlayer("adventurer", Tile(3208, 3220, 2))

        val banker = npcs[Tile(3208, 3222, 2)].first { it.id.startsWith("banker") }

        player.npcOption(banker, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption("line5")
        player.dialogueContinue()
        player.dialogueOption("line1")
        player.dialogueContinue()

        assertTrue(player["you_can_bank_on_us_task", false])
        assertTrue(player.containsVarbit("task_reward_items", "red_dye"))
    }

    @Test
    fun `Hang on to Something`() {
        val player = createPlayer("adventurer", Tile(3208, 3220, 2))
        player.inventory.add("coins", 1000)
        val bank = objects[Tile(3208, 3221, 2), "bank_booth_lumbridge"]!!

        player.objectOption(bank, "Use-quickly")
        tick(5)
        player.interfaceOption("bank_side", "inventory", "Deposit-10", item = Item("coins"), slot = 0)

        assertTrue(player["hang_on_to_something_task", false])
        assertTrue(player.containsVarbit("task_reward_items", "magic_staff"))
    }

    @Test
    fun `Bovine Intervention`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("adventurer", Tile(3257, 3260))
        val npc = npcs[Tile(3258, 3260)].first { it.id.startsWith("cow") }

        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.levels.set(Skill.Attack, 100)
        player.levels.set(Skill.Strength, 100)
        player.levels.set(Skill.Defence, 100)

        player.npcOption(npc, "Attack")
        tick(10)

        assertTrue(player["bovine_intervention_task", false])
    }

    @Test
    fun `Tan Your Hide`() {
        val player = createPlayer("adventurer", Tile(3276, 3192))
        val npc = npcs[Tile(3276, 3193)].first { it.id == "ellis" }

        player.inventory.add("cowhide", "coins")

        player.npcOption(npc, "Trade")
        tick()
        player.interfaceOption("tanner", "cowhide", "Tan <col=FF981F>1")

        assertTrue(player["tan_your_hide_task", false])
    }

    @Test
    fun `Handi-crafts`() {
        val player = createPlayer("adventurer", Tile(3208, 3220, 2))

        player.inventory.add("leather", "needle", "thread")

        player.itemOnItem(0, 1)
        tick()
        player.dialogueOption(id = "dialogue_skill_creation", component = "choice1")
        tick(2)

        assertTrue(player["handicrafts_task", false])
    }

    @Test
    fun `Handy Dandy`() {
        val player = createPlayer("adventurer")
        player.inventory.add("leather_gloves")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("leather_gloves"), 0)

        assertTrue(player["handy_dandy_task", false])
    }

    @Test
    fun `Just Can't Get the Staff`() {
        val player = createPlayer("adventurer")
        player.inventory.add("staff_of_air")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("staff_of_air"), 0)

        assertTrue(player["just_cant_get_the_staff_task", false])
    }

    @Test
    fun `Click Your Heels Three Times`() {
        val player = createPlayer("adventurer")

        player.interfaceOption("modern_spellbook", "lumbridge_home_teleport", "Cast")

        tick(19)

        assertTrue(player["click_your_heels_three_times_task", false])
    }

    @Test
    fun `Reach Out and Touch Someone`() {
        val player = createPlayer("adventurer")
        player.inventory.add("shortbow")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("shortbow"), 0)

        assertTrue(player["reach_out_and_touch_someone_task", false])
    }

    @Test
    fun `Death From Above`() = runTest {
        val player = createPlayer("adventurer", Tile(3211, 3253))
        player.inventory.add("air_rune", "mind_rune")

        val npc = npcs[player.tile.zone].first { it.id == "magic_dummy" }

        player.instructions.send(InteractInterfaceNPC(npc.index, 192, 25, -1, -1))
        tick(1)

        assertTrue(player["death_from_above_task", false])
    }

    @Test
    fun `Om Nom Nom Nom`() {
        val player = createPlayer("adventurer")
        player.levels.set(Skill.Constitution, 12)
        player.inventory.add("crayfish")

        player.itemOption("Eat", "crayfish")

        assertTrue(player["om_nom_nom_nom_task", false])
    }

    @Test
    fun `On the Level`() {
        val player = createPlayer("adventurer")

        player.exp(Skill.Ranged, 1358.0)

        assertTrue(player["on_the_level_task", false])
    }

    @Test
    fun `So That's What Ess Stands For`() {
        val player = createPlayer("adventurer", Tile(2893, 4846))
        player.levels.set(Skill.Mining, 100)
        player.inventory.add("bronze_pickaxe")
        val essence = objects[Tile(2891, 4847), "rune_essence_rocks"]!!

        player.objectOption(essence, "Mine")
        tick(9)

        assertTrue(player["so_thats_what_ess_stands_for_task", false])
    }

    @Test
    fun `Air Craft`() {
        val player = createPlayer("player", Tile(2844, 4832))
        player.levels.set(Skill.Runecrafting, 99)
        player.inventory.add("rune_essence")

        val altar = objects[Tile(2843, 4833 ), "air_altar"]!!
        player.objectOption(altar, "Craft-rune")
        tick(2)

        assertTrue(player["air_craft_task", false])
    }

    @Test
    fun `Greasing the Wheels of Commerce`() {
        val player = createPlayer("shopper", Tile(3214, 3242))
        val npc = npcs[Tile(3214, 3243)].first { it.id == "shop_assistant_lumbridge"}
        player.inventory.add("bronze_dagger", 1)

        player.npcOption(npc, "Trade")
        tick()
        player.interfaceOption("shop_side", "inventory", "Sell 1", item = Item("bronze_dagger"), slot = 0)

        assertTrue(player["greasing_the_wheels_of_commerce_task", false])
    }

    @Test
    fun `I Wonder If It'll Sprout`() {
        val player = createPlayer("adventurer")
        player.inventory.add("bones")

        player.interfaceOption("inventory", "inventory", "Bury", 0, Item("bones"), 0)

        assertTrue(player["i_wonder_if_itll_sprout_task", false])
    }

    @Test
    fun `Put Your Hands Together For`() {
        val player = createPlayer("player")

        player.interfaceOption("prayer_list", "regular_prayers", optionIndex = 0, slot = 0)

        assertTrue(player["put_your_hands_together_for_task", false])
    }

    @Test
    fun `Prayer Point Power`() {
        val player = createPlayer("player", Tile(3244, 3207))
        player.levels.drain(Skill.Prayer, 1)

        val altar = objects[Tile(3243, 3206), "prayer_altar_lumbridge"]!!
        player.objectOption(altar, "Pray")
        tick()

        assertTrue(player["prayer_point_power_task", false])
    }

    @Test
    fun `Not What We Mean By Irony`() {
        val player = createPlayer("adventurer")
        player.inventory.add("iron_dagger")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("iron_dagger"), 0)

        assertTrue(player["not_what_we_mean_by_irony_task", false])
    }

    @Test
    fun `Alls Ferrous in Love and War`() {
        val player = createPlayer("adventurer")
        player.inventory.add("iron_boots")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("iron_boots"), 0)

        assertTrue(player["alls_ferrous_in_love_and_war_task", false])
    }

    @Test
    fun `First Blood`() {
        val player = createPlayer("adventurer")

        player.exp(Skill.Attack, 388.0)
        player.exp(Skill.Defence, 388.0)

        assertTrue(player["first_blood_task", false])
    }

    @Test
    fun `Temper Temper`() {
        val player = createPlayer("adventurer")
        player.levels.set(Skill.Attack, 5)
        player.inventory.add("steel_sword")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("steel_sword"), 0)

        assertTrue(player["temper_temper_task", false])
    }

    @Test
    fun `Steel Yourself For Combat`() {
        val player = createPlayer("adventurer")
        player.levels.set(Skill.Defence, 5)
        player.inventory.add("steel_platebody")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("steel_platebody"), 0)

        assertTrue(player["steel_yourself_for_combat_task", false])
    }

    @Test
    fun `Ammo Ammo Ammo`() {
        val player = createPlayer("adventurer")
        player.inventory.add("iron_arrow")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("iron_arrow"), 0)

        assertTrue(player["ammo_ammo_ammo_task", false])
    }

    @Test
    fun `Take a Bow`() {
        val player = createPlayer("adventurer")
        player.inventory.add("shortbow")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("shortbow"), 0)

        assertTrue(player["take_a_bow_task", false])
    }

    @Test
    fun `Don't Bury This One`() {
        val player = createPlayer("adventurer")

        player.inventory.add("iron_hatchet")

        assertTrue(player["dont_bury_this_one_task", false])
    }

    @Test
    fun `Mace Invaders`() {
        val player = createPlayer("adventurer", Tile(3228, 3254))
        player.levels.set(Skill.Smithing, 2)
        player.inventory.add("bronze_bar", "hammer")

        val anvil = objects[Tile(3229, 3254), "anvil_lumbridge"]!!
        player.itemOnObject(anvil, 0)
        tick()
        player.interfaceOption("smithing", "mace_1", "Make 1 Mace")
        tick(3)


        assertTrue(player["mace_invaders_task", false])
    }

    @Test
    fun `Capital Protection, What`() {
        val player = createPlayer("adventurer", Tile(3228, 3254))
        player.levels.set(Skill.Smithing, 7)
        player.inventory.add("bronze_bar", "bronze_bar", "hammer")

        val anvil = objects[Tile(3229, 3254), "anvil_lumbridge"]!!
        player.itemOnObject(anvil, 0)
        tick()
        player.interfaceOption("smithing", "full_helm_1", "Make 1 Full helm")
        tick(3)


        assertTrue(player["capital_protection_what_task", false])
    }

    @Test
    fun `Hack and Smash`() {
        val player = createPlayer("adventurer")

        player.exp(Skill.Mining, 512.0)

        assertTrue(player["hack_and_smash_task", false])
    }

    @Test
    fun `Shrimpin' Ain't Easy`() {
        val player = createPlayer("adventurer", Tile(3245, 3155))
        player.levels.set(Skill.Fishing, 20)
        val fishingSpot = createNPC("fishing_spot_small_net_bait_lumbridge", Tile(3246, 3155))
        player.inventory.add("small_fishing_net")

        player.npcOption(fishingSpot, "Net")
        tick(7)

        assertTrue(player["shrimpin_aint_easy_task", false])
    }

    @Test
    fun `The Fruit of the Sea`() {
        val player = createPlayer("shopper", Tile(3194, 3254))
        val npc = npcs[Tile(3195, 3254)].first { it.id == "hank" }
        player.inventory.add("raw_shrimps")

        player.npcOption(npc, "Trade")
        tick()
        player.interfaceOption("shop_side", "inventory", "Sell 1", item = Item("raw_shrimps"), slot = 0)

        assertTrue(player["the_fruit_of_the_sea_task", false])
    }

    @Test
    fun `Made For Walking`() {
        val player = createPlayer("adventurer", Tile(3208, 3220, 2))
        player.levels.set(Skill.Crafting, 7)
        player.inventory.add("leather", "needle", "thread")

        player.itemOnItem(0, 1)
        tick()
        player.dialogueOption(id = "dialogue_skill_creation", component = "choice2")
        tick(2)

        assertTrue(player["made_for_walking_task", false])
    }

    @Test
    fun `Did Anyone Bring Any Toast`() {
        val player = createPlayer("adventurer", Tile(3086, 3230))
        player.levels.set(Skill.Fishing, 5)
        val fishingSpot = createNPC("fishing_spot_small_net_bait_draynor", Tile(3085, 3230))
        player.inventory.add("fishing_rod", "fishing_bait")

        player.npcOption(fishingSpot, "Bait")
        tick(7)

        assertTrue(player["did_anyone_bring_any_toast_task", false])
    }

    @Test
    fun `It's Not a Red One`() {
        val player = createPlayer("adventurer", Tile(3079, 3444))
        player.levels.set(Skill.Cooking, 100)
        player.inventory.add("raw_herring")
        val fire = objects[Tile(3079, 3445), "fire_orange"]!!

        player.itemOnObject(fire, 0)
        tick(4)

        assertTrue(player["its_not_a_red_one_task", false])
    }

    @Test
    fun `Not So Confusing After All`() = runTest {
        val player = createPlayer("adventurer", Tile(3211, 3253))
        player.levels.set(Skill.Magic, 3)
        player.inventory.add("water_rune", 3)
        player.inventory.add("earth_rune", 2)
        player.inventory.add("body_rune")

        val npc = npcs[player.tile.zone].first { it.id == "magic_dummy" }

        player.instructions.send(InteractInterfaceNPC(npc.index, 192, 26, -1, -1))
        tick(1)

        assertTrue(player["not_so_confusing_after_all_task", false])
    }

    @Test
    fun `Heart of Oak`() {
        val player = createPlayer("adventurer")
        player.levels.set(Skill.Ranged, 5)
        player.inventory.add("oak_longbow")

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("oak_longbow"), 0)

        assertTrue(player["heart_of_oak_task", false])
    }

    @Test
    fun `Get the Point`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int) = until / 2

            override fun nextBits(bitCount: Int) = 100
        })
        val player = createPlayer("player", Tile(3206, 3205))
        val npc = npcs[Tile(3206, 3204)].first { it.id == "rat" }

        player.levels.set(Skill.Ranged, 50)
        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "steel_arrow", 100)

        player.npcOption(npc, "Attack")
        tick(20)

        assertTrue(player["get_the_point_task", false])
    }

    @Test
    fun `Berry Tasty`() {
        val player = createPlayer("adventurer", Tile(3231, 3197))
        player.levels.set(Skill.Cooking, 10)
        player.inventory.add("uncooked_berry_pie")

        val oven = objects[Tile(3230, 3196), "cooking_range_lumbridge"]!!
        player.itemOnObject(oven, 0)
        tick(4)

        assertTrue(player["berry_tasty_task", false])
    }

    @Test
    fun `Dish water`() {
        val player = createPlayer("adventurer", Tile(3231, 3197))
        player.inventory.add("beer")

        player.itemOption("Drink", "beer")

        assertTrue(player["dishwater_task", false])
    }

    @Test
    fun `Quarter Centurion`() {
        val player = createPlayer("adventurer")

        player.exp(Skill.Attack, 8740.0)

        assertTrue(player["quarter_centurion_task", false])
    }

    @Test
    fun `Fledgeling Adventurer`() {
        val player = createPlayer("adventurer")

        player["quest_points"] = 5

        assertTrue(player["fledgeling_adventurer_task", false])
    }

    @Test
    fun `Hail to the Duke, Baby`() {
        val player = createPlayer("adventurer", Tile(3211, 3220, 1))
        val duke = npcs[Tile(3212, 3220, 1)].first { it.id == "duke_horacio" }

        player.npcOption(duke, "Talk-to")
        tick()

        assertTrue(player["hail_to_the_duke_baby_task", false])
    }

    @Test
    fun `Window Shopping`() {
        val player = createPlayer("shopper", Tile(3215, 3243))
        val npc = npcs[Tile(3214, 3243)].first { it.id == "shop_assistant_lumbridge" }

        player.npcOption(npc, "Trade")
        tick()

        assertTrue(player["window_shopping_task", false])
    }

    @Test
    fun `Wait, That's Not a Sheep`() {
        val player = createPlayer("adventurer")

        player.tele(3189, 3275)
        tick()

        assertTrue(player["wait_thats_not_a_sheep_task", false])
    }

    @Test
    fun `In the Countyard`() {
        val player = createPlayer("adventurer", Tile(3109, 3330))

        player.walk(Tile(3109, 3331))
        tick(2)

        assertTrue(player["in_the_countyard_task", false])
    }

    @Test
    fun `Beware of Pigzilla`() {
        val player = createPlayer("adventurer", Tile(3081, 3258))

        player.walk(Tile(3081, 3257))
        tick(2)

        assertTrue(player["beware_of_pigzilla_task", false])
    }

    @Test
    fun `Tower Power`() {
        val player = createPlayer("adventurer", Tile(3104, 3161, 1))

        val stairs = objects[Tile(3103, 3159, 1), "wizards_tower_staircase"]!!
        player.objectOption(stairs, "Climb-up")
        tick(2)

        assertTrue(player["tower_power_task", false])
    }

    @Test
    fun `Tinkle the Ivories`() {
        val player = createPlayer("adventurer", Tile(3243, 3213))

        val stairs = objects[Tile(3243, 3214), "lumbridge_organ"]!!
        player.objectOption(stairs, "Play")
        tick()

        assertTrue(player["tinkle_the_ivories_task", false])
    }

    @Test
    fun `Passing Out with money`() {
        val player = createPlayer("adventurer", Tile(3267, 3227))
        player.inventory.add("coins", 10)

        val guard = npcs[Tile(3267, 3226)].first { it.id == "border_guard_al_kharid" }
        player.npcOption(guard, "Talk-to")
        tick()
        player.dialogueContinue(2)
        player.dialogueOption("line1")
        player.dialogueContinue()

        assertTrue(player["passing_out_task", false])
    }

    @Test
    fun `Passing Out without money`() {
        val player = createPlayer("adventurer", Tile(3267, 3227))
        player.inventory.add("coins", 9)

        val guard = npcs[Tile(3267, 3226)].first { it.id == "border_guard_al_kharid" }
        player.npcOption(guard, "Talk-to")
        tick()
        player.dialogueContinue(2)
        player.dialogueOption("line1")
        player.dialogueContinue()

        assertFalse(player["passing_out_task", false])
    }

    @Test
    fun `What is This Place`() {
        val player = createPlayer("adventurer", Tile(3104, 9571))

        val guard = npcs[Tile(3103, 9571)].first { it.id == "sedridor" }
        player.npcOption(guard, "Teleport")
        tick(2)

        assertTrue(player["what_is_this_place_task", false])
    }

}