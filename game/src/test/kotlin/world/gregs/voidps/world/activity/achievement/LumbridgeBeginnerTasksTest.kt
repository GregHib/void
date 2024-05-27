package world.gregs.voidps.world.activity.achievement

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.entity.character.move.running
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
import world.gregs.voidps.world.script.*
import kotlin.test.assertTrue

internal class LumbridgeBeginnerTasksTest : WorldTest() {

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
        tick(3)

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
        tick(4)

        assertTrue(player["adventurers_log_task", false])
    }

    @Test
    fun `Aren't they supposed to be twins`() {
        val player = createPlayer("adventurer", Tile(3258, 3205))
        player.levels.set(Skill.Fishing, 20)
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
        tick(5)

        assertTrue(player["log_a_rhythm_task", false])
    }

    @Test
    fun `Shellfish Roasting on an Open Fire`() {
        val player = createPlayer("adventurer", Tile(3079, 3444))
        player.levels.set(Skill.Cooking, 100)
        player.inventory.add("raw_crayfish")
        val fire = createObject("fire_orange", Tile(3079, 3445))

        player.itemOnObject(fire, 0, "")
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

        player.itemOnObject(furnace, 0, "")
        tick()
        player.interfaceOption("skill_creation_amount", "increment")
        player.dialogueOption(id = "dialogue_skill_creation", component = "choice1")
        tick(4)

        assertTrue(player["bar_one_task", false])
    }

    @Test
    fun `Cutting Edge Technology`() {
        val player = createPlayer("adventurer", Tile(3228, 3254))
        player.levels.set(Skill.Smithing, 100)
        val furnace = objects[Tile(3229, 3254), "anvil_lumbridge"]!!
        player.inventory.add("bronze_bar", "hammer")

        player.itemOnObject(furnace, 0, "")
        tick()
        player.interfaceOption("smithing", "dagger_1")
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
        player.interfaceOption("tanner", "cowhide", "Tan ${Colours.ORANGE.toTag()}1")

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
}