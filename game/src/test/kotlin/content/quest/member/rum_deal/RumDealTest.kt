package content.quest.member.rum_deal

import WorldTest
import containsMessage
import content.entity.combat.hit.damage
import content.skill.farming.Farming
import dialogueContinue
import dialogueOption
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Braindeath Island object locations:
 *  blindweed patch  (2162, 5069, 0)   tool cupboard   (2157, 5093, 0)
 *  gate             (2120, 5098, 0)   stagnant lake   (2134, 5162, 0)
 *  output tap       (2142, 5093, 1)   brewing control (2143, 5100, 1)
 *  intake hopper    (2142, 5102, 2)   pressure barrel (2141, 5102, 2)
 *  pressure lever   (2141, 5103, 2)
 */
class RumDealTest : WorldTest() {

    @Test
    fun `Pirate Pete talks the player into hunting Barrelor the Destroyer`() {
        val player = createPlayer(Tile(3672, 3538))
        player["zogre_flesh_eaters"] = "completed"
        player.experience.set(Skill.Slayer, Level.experience(42))
        val pete = createNPC("pirate_pete", Tile(3673, 3538))

        player.npcOption(pete, "Talk-to")
        tick(2)
        player.skipDialogues() // greeting through "Will you help me find my family sword?"
        player.dialogueOption("line1") // Yes!
        player.dialogueContinue()
        assertEquals("agreed_to_help", player["rum_deal", "unstarted"])

        player.skipDialogues() // the Barrelor build-up
        player.dialogueOption("line1") // Of course, I fear no demon!
        player.dialogueContinue()
        assertEquals("slay_barrelor", player["rum_deal", "unstarted"])
    }

    @Test
    fun `Pirate Pete turns the player away without Zogre Flesh Eaters`() {
        val player = createPlayer(Tile(3672, 3538))
        val pete = createNPC("pirate_pete", Tile(3673, 3538))

        player.npcOption(pete, "Talk-to")
        tick(2)
        player.skipDialogues()
        assertEquals("unstarted", player["rum_deal", "unstarted"])
    }

    @Test
    fun `The tool cupboard hands out farming equipment`() {
        val player = createPlayer(Tile(2159, 5092))
        val cupboard = GameObjects.find(Tile(2157, 5093), "braindeath_island_tool_cupboard")
        tick(2)

        player.objectOption(cupboard, "Open")
        tick(4)
        val open = GameObjects.find(Tile(2157, 5093), "braindeath_island_tool_cupboard_open")
        player.objectOption(open, "Search")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line4") // All of the above!
        tick(2)

        assertTrue(player.inventory.contains("rake"))
        assertTrue(player.inventory.contains("seed_dibber"))
        assertTrue(player.inventory.contains("watering_can"))
    }

    @Test
    fun `Blindweed goes into the intake hopper`() {
        val player = createPlayer(Tile(2142, 5103, 2))
        player["rum_deal"] = "show_blindweed"
        player.inventory.add("blindweed")
        val hopper = GameObjects.find(Tile(2142, 5102, 2), "braindeath_island_intake_hopper")

        player.itemOnObject(hopper, player.inventory.indexOf("blindweed"))
        tick(4)

        assertEquals("blindweed_added", player["rum_deal", "unstarted"])
        assertTrue(player.containsMessage("You stuff the Blindweed into the Hopper."))
        assertEquals(0, player.inventory.count("blindweed"))
    }

    @Test
    fun `Stagnant water is scooped into an empty bucket`() {
        val player = createPlayer(Tile(2134, 5161))
        player["rum_deal"] = "fetch_water"
        player.inventory.add("bucket")
        val lake = GameObjects.find(Tile(2134, 5162), "braindeath_island_stagnant_lake")

        player.itemOnObject(lake, player.inventory.indexOf("bucket"))
        tick(5)

        assertEquals("collected_water", player["rum_deal", "unstarted"])
        assertTrue(player.inventory.contains("bucket_of_water_stagnant"))
    }

    @Test
    fun `Sea creatures are stuffed into the barrel and pressurised`() {
        val player = createPlayer(Tile(2140, 5102, 2))
        player["rum_deal"] = "catch_creatures"
        player.inventory.add("sluglings", 3)
        player.inventory.add("karamthulhu", 2)
        val barrel = GameObjects.find(Tile(2141, 5102, 2), "braindeath_island_pressure_barrel")
        tick(2)

        repeat(3) {
            player.itemOnObject(barrel, player.inventory.indexOf("sluglings"))
            tick(5)
        }
        repeat(2) {
            player.itemOnObject(barrel, player.inventory.indexOf("karamthulhu"))
            tick(5)
        }
        assertEquals(5, player["rum_deal_pressure_count", 0])

        player.objectOption(barrel, "Count")
        tick(2)
        assertTrue(player.containsMessage("There are 3 loads of Sluglings and 2 Karamthulhu in this barrel."))

        // Count 5 transforms the lever to its ready state
        val lever = GameObjects.find(Tile(2141, 5103, 2), "braindeath_island_pressure_lever_multi")
        player.objectOption(lever, "Pull")
        tick(6)

        assertEquals("pressurised", player["rum_deal", "unstarted"])
        assertEquals(0, player["rum_deal_pressure_count", 0])
        assertEquals(1, player["rum_deal_brewing_control", 0])
    }

    @Test
    fun `The lever refuses to pressurise fewer than five creatures`() {
        val player = createPlayer(Tile(2142, 5103, 2))
        player["rum_deal"] = "catch_creatures"
        player["rum_deal_pressure_count"] = 3
        val lever = GameObjects.find(Tile(2141, 5103, 2), "braindeath_island_pressure_lever_multi")

        player.objectOption(lever, "Pull")
        tick(2)

        assertTrue(player.containsMessage("You do not yet have five sea creatures in the barrel!"))
        assertEquals("catch_creatures", player["rum_deal", "unstarted"])
    }

    @Test
    fun `Netting the fishing spot needs level 50 and the tangled bowl`() {
        val player = createPlayer(Tile(2160, 5100))
        player["rum_deal"] = "catch_creatures"
        val spot = createNPC("fishing_spot_braindeath_island", Tile(2161, 5100))

        player.npcOption(spot, "Fish")
        tick(2)
        assertTrue(player.containsMessage("You cannot fish here, you need a Fishing level of at least 50."))

        player.experience.set(Skill.Fishing, Level.experience(50))
        player.npcOption(spot, "Fish")
        tick(2)
        assertTrue(player.containsMessage("You do not have the correct equipment to fish here."))

        player.inventory.add("fishbowl_and_net")
        player.npcOption(spot, "Fish")
        tick(8)
        assertTrue(player.inventory.contains("sluglings") || player.inventory.contains("karamthulhu"))
    }

    @Test
    fun `Smiting the brewing controls forces out the Evil Spirit`() {
        val player = createPlayer(Tile(2143, 5101, 1))
        player["rum_deal"] = "bless_wrench"
        player["rum_deal_brewing_control"] = 1
        player.inventory.add("holy_wrench")
        val controls = GameObjects.find(Tile(2143, 5100, 1), "braindeath_island_brewing_controls_multi")

        player.itemOnObject(controls, player.inventory.indexOf("holy_wrench"))
        tick(8)

        assertTrue(player.containsMessage("The Evil Spirit is forced from the controls!"))
        val spirit = NPCs.at(player.tile.regionLevel).firstOrNull { it.id == "evil_spirit" }
        assertNotNull(spirit, "the Evil Spirit should have manifested")

        // Same rules as the Ghosts Ahoy lobster: owned, temporary, hint-marked and attacking
        assertEquals(player.accountName, spirit["owner", ""], "the spirit belongs to its summoner")
        assertTrue(player.viewport!!.hints.any { it != 0 }, "a hint arrow should mark the spirit")
    }

    @Test
    fun `The output tap only pours once the vat is full`() {
        val player = createPlayer(Tile(2142, 5092, 1))
        player["rum_deal"] = "spider_added"
        player.inventory.add("bucket")
        val tap = GameObjects.find(Tile(2142, 5093, 1), "braindeath_island_output_tap")

        player.itemOnObject(tap, player.inventory.indexOf("bucket"))
        tick(4)
        assertTrue(player.containsMessage("The vat is empty."))
        assertNull(player.inventory.indexOf("unsanitary_swill").takeIf { it >= 0 })

        player["rum_deal"] = "collect_swill"
        player.itemOnObject(tap, player.inventory.indexOf("bucket"))
        tick(2)
        assertTrue(player.inventory.contains("unsanitary_swill"))
    }

    @Test
    fun `A zombie swab can only be talked to once intimidated`() {
        val player = createPlayer(Tile(2160, 5070))
        val swab = createNPC("zombie_swab", Tile(2161, 5070))

        player.npcOption(swab, "Talk-to")
        tick(2)
        assertTrue(player.containsMessage("I don't think he wants to talk to you."))

        player["rum_deal_swab_a"] = 1
        player.npcOption(swab, "Talk-to")
        tick(2)
        player.dialogueContinue()

        player.npcOption(swab, "Intimidate")
        tick(2)
        assertTrue(player.containsMessage("I don't think he will be causing any more trouble."))
    }

    @Test
    fun `Each brewer has their own dialogue`() {
        val player = createPlayer(Tile(2150, 5093, 1))
        val first = createNPC("brewer", Tile(2149, 5093, 1))
        val fourth = createNPC("brewer_4", Tile(2151, 5093, 1))
        tick(2)

        // A brewer whose id did not match would end the dialogue right after the opener
        player.npcOption(first, "Talk-to")
        tick(2)
        player.dialogueContinue() // "So... how are you holding up?"
        assertNotNull(player.dialogue, "brewer should have its own reply")
        player.skipDialogues()

        player.npcOption(fourth, "Talk-to")
        tick(2)
        player.dialogueContinue()
        assertNotNull(player.dialogue, "brewer_4 should have its own reply")
    }

    @Test
    fun `Luke sees through a diversion before Braindeath sends you for water`() {
        val player = createPlayer(Tile(2120, 5098))
        createNPC("50_luke", Tile(2120, 5096))
        player["rum_deal"] = "blindweed_added" // stage 7, one short of the water errand
        val gate = GameObjects.find(Tile(2120, 5098), "gate_104_closed")
        tick(2)

        player.objectOption(gate, "Open")
        tick(2)
        player.skipDialogues()
        tick(4)

        assertEquals(Tile(2120, 5098), player.tile, "Luke should not let the player through")
    }

    @Test
    fun `A diversion gets the player out through Luke's gate`() {
        val player = createPlayer(Tile(2120, 5098))
        createNPC("50_luke", Tile(2120, 5096))
        player["rum_deal"] = "fetch_water" // stage 8 unlocks the diversions
        val gate = GameObjects.find(Tile(2120, 5098), "gate_104_closed")
        tick(2)

        player.objectOption(gate, "Open")
        tick(2)
        player.skipDialogues()
        tick(6)

        assertEquals(Tile(2120, 5099), player.tile, "the player should slip north past Luke")
    }

    @Test
    fun `Luke marches the player back into the compound`() {
        val player = createPlayer(Tile(2120, 5099))
        createNPC("50_luke", Tile(2120, 5096))
        player["rum_deal"] = "fetch_water"
        val gate = GameObjects.find(Tile(2120, 5098), "gate_104_closed")
        tick(2)

        player.objectOption(gate, "Open")
        tick(2)
        player.skipDialogues()
        tick(6)

        assertEquals(Tile(2120, 5098), player.tile, "the player should be sent back south")
    }

    @Test
    fun `Luke tells the tale of his missing half`() {
        val player = createPlayer(Tile(2120, 5097))
        val luke = createNPC("50_luke", Tile(2120, 5096))
        tick(2)

        player.npcOption(luke, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line1") // What happened to you?
        player.dialogueContinue()
        assertNotNull(player.dialogue, "the albatross story should follow")
        player.skipDialogues()
    }

    @Test
    fun `The Evil Spirit fights with its own animations and sounds`() {
        val definition = get<CombatDefinitions>().getOrNull("evil_spirit")
        assertNotNull(definition, "the Evil Spirit needs a combat definition to fight back")

        assertEquals("spirit_parry", definition.defendAnim)
        assertEquals("spirit_death", definition.deathAnim)
        assertEquals("deal_spirit_attack", definition.defendSound?.id)
        assertEquals("rumdeal_spirit_depart", definition.deathSound?.id)

        val melee = definition.attacks["melee"]
        assertNotNull(melee, "the Evil Spirit needs a melee attack")
        assertEquals("spirit_attack", melee.anim)
        val hit = melee.targetHits.first()
        assertEquals("slash", hit.offense)
        assertEquals(280, hit.max)
    }

    @Test
    fun `Blindweed seeds can be planted and harvested in the Braindeath patch`() {
        val player = createPlayer(Tile(2161, 5069))
        player.experience.set(Skill.Farming, Level.experience(40))
        player.inventory.add("seed_dibber")
        player.inventory.add("spade")
        player.inventory.add("blindweed_seed")
        val patch = GameObjects.find(Tile(2162, 5069), "farming_blindweed_patch_braindeath_island")
        tick(2)

        player["farming_blindweed_patch_braindeath_island"] = "weeds_0"

        player.itemOnObject(patch, player.inventory.indexOf("blindweed_seed"))
        tick(6)
        assertEquals(
            "blindweed_0",
            player["farming_blindweed_patch_braindeath_island", ""],
            "the seed should be in the ground",
        )
        assertEquals(0, player.inventory.count("blindweed_seed"))
        assertTrue(
            player.timers.contains("farming_tick"),
            "planting must start the growth timer for a patch that was already clear",
        )

        // Skip the growth tick
        player["farming_blindweed_patch_braindeath_island"] = "blindweed_life1"
        player.objectOption(patch, "Pick")
        tick(6)

        assertEquals(1, player.inventory.count("blindweed"), "picking should yield one blindweed")
    }

    @Test
    fun `Blindweed reaches full growth in a single farming cycle`() {
        val player = createPlayer(Tile(2161, 5069))
        val farming = scripts.filterIsInstance<Farming>().first()
        player["farming_blindweed_patch_braindeath_island"] = "blindweed_0"

        // Blindweed is a 2-stage crop on a 5 minute tick, so one cycle finishes it
        farming.grow(player, 5)
        assertEquals("blindweed_life1", player["farming_blindweed_patch_braindeath_island", ""])

        // life1 is the last stage; further cycles must not advance or kill it
        farming.grow(player, 5)
        farming.grow(player, 10)
        assertEquals("blindweed_life1", player["farming_blindweed_patch_braindeath_island", ""])
    }

    @Test
    fun `Planting below the farming level keeps the seed`() {
        val player = createPlayer(Tile(2161, 5069))
        player.experience.set(Skill.Farming, Level.experience(39))
        player.inventory.add("seed_dibber")
        player.inventory.add("blindweed_seed")
        player["farming_blindweed_patch_braindeath_island"] = "weeds_0"
        val patch = GameObjects.find(Tile(2162, 5069), "farming_blindweed_patch_braindeath_island")
        tick(2)

        player.itemOnObject(patch, player.inventory.indexOf("blindweed_seed"))
        tick(4)

        assertEquals(1, player.inventory.count("blindweed_seed"), "the seed should survive a failed level check")
        assertEquals("weeds_0", player["farming_blindweed_patch_braindeath_island", ""])
    }

    @Test
    fun `Harvesting the blindweed advances the quest`() {
        val player = createPlayer(Tile(2161, 5069))
        player.experience.set(Skill.Farming, Level.experience(40))
        player.inventory.add("spade")
        player["rum_deal"] = "given_seeds"
        player["farming_blindweed_patch_braindeath_island"] = "blindweed_life1"
        val patch = GameObjects.find(Tile(2162, 5069), "farming_blindweed_patch_braindeath_island")
        tick(2)

        player.objectOption(patch, "Pick")
        tick(6)

        assertTrue(player.inventory.contains("blindweed"))
        assertEquals(
            "grown_blindweed",
            player["rum_deal", "unstarted"],
            "picking the blindweed is what moves the quest off given_seeds",
        )
    }

    @Test
    fun `A crop planted before the timer fix resumes growing on login`() {
        // Simulates a save from before planting started the timer: seed in the ground,
        // no last_growth_cycle because the growth tick had never run.
        val player = createPlayer(Tile(2161, 5069)) {
            it["farming_blindweed_patch_braindeath_island"] = "blindweed_0"
        }

        assertTrue(
            player.timers.contains("farming_tick"),
            "a crop already in the ground must resume growing on login",
        )
    }

    @Test
    fun `Login does not start the growth timer for a player with nothing planted`() {
        val player = createPlayer(Tile(2161, 5069))

        assertTrue(
            !player.timers.contains("farming_tick"),
            "players who have never farmed should not tick every minute",
        )
    }

    @Test
    fun `The player is told when the blindweed finishes growing`() {
        val player = createPlayer(Tile(2161, 5069))
        player["rum_deal"] = "given_seeds"
        player["farming_blindweed_patch_braindeath_island"] = "blindweed_0"
        val farming = scripts.filterIsInstance<Farming>().first()

        farming.grow(player, 5)

        assertEquals("blindweed_life1", player["farming_blindweed_patch_braindeath_island", ""])
        assertTrue(player.containsMessage("I wonder how my Blindweed is coming along..."))
    }

    @Test
    fun `Opening Luke's gate swaps in the open gate model`() {
        val player = createPlayer(Tile(2120, 5098))
        createNPC("50_luke", Tile(2120, 5096))
        player["rum_deal"] = "fetch_water"
        val gate = GameObjects.find(Tile(2120, 5098), "gate_104_closed")
        tick(2)

        player.objectOption(gate, "Open")
        tick(2)
        player.skipDialogues()
        tick()

        // The closed gate is swapped for an invisible wall and the open gate appears alongside it,
        // both on the wall layer - a centrepiece here would render nothing.
        assertNotNull(
            GameObjects.findLayerOrNull(Tile(2120, 5098), ObjectLayer.WALL, "inviswall"),
            "the closed gate should be replaced by an invisible wall",
        )
        assertNotNull(
            GameObjects.findLayerOrNull(Tile(2120, 5099), ObjectLayer.WALL, "gate_104_opened"),
            "the open gate should render on the wall layer",
        )
    }

    @Test
    fun `Weeds regrow on the blindweed patch and stop when fully overgrown`() {
        val player = createPlayer(Tile(2161, 5069))
        val farming = scripts.filterIsInstance<Farming>().first()
        player["farming_blindweed_patch_braindeath_island"] = "weeds_0"

        // weeds_3 is the varbit default, so a fully overgrown patch stores no value of its own
        for ((cycle, expected) in listOf("weeds_1", "weeds_2", "weeds_3").withIndex()) {
            farming.grow(player, 5 * (cycle + 1))
            assertEquals(expected, player["farming_blindweed_patch_braindeath_island", "weeds_3"])
        }

        // weeds_3 is fully overgrown - further cycles must not wrap it back around to clear
        farming.grow(player, 20)
        farming.grow(player, 25)
        assertEquals("weeds_3", player["farming_blindweed_patch_braindeath_island", "weeds_3"])
    }

    @Test
    fun `Blindweed patch can be raked clear with a rake`() {
        val player = createPlayer(Tile(2161, 5069))
        player.inventory.add("rake")
        player["farming_blindweed_patch_braindeath_island"] = "weeds_3"
        val patch = GameObjects.find(Tile(2162, 5069), "farming_blindweed_patch_braindeath_island")
        tick(2)

        // These weed states have no Rake option, so the rake is used on the patch instead
        player.itemOnObject(patch, player.inventory.indexOf("rake"))
        tick(20)

        assertEquals("weeds_0", player["farming_blindweed_patch_braindeath_island", ""])
        assertEquals(3, player.inventory.count("weeds"))
    }

    @Test
    fun `Blindweed yields a single harvest and empties the patch`() {
        val player = createPlayer(Tile(2161, 5069))
        player.experience.set(Skill.Farming, Level.experience(40))
        player.inventory.add("spade")
        player["farming_blindweed_patch_braindeath_island"] = "blindweed_life1"
        val patch = GameObjects.find(Tile(2162, 5069), "farming_blindweed_patch_braindeath_island")
        tick(2)

        player.objectOption(patch, "Pick")
        tick(20)

        // blindweed_life1 is the only life state, so picking it exhausts the patch
        assertEquals(1, player.inventory.count("blindweed"), "blindweed has a single life")
        assertEquals("weeds_0", player["farming_blindweed_patch_braindeath_island", ""])
    }

    @Test
    fun `Banishing the Evil Spirit clears only its own hint arrow`() {
        val player = createPlayer(Tile(2143, 5101, 1))
        player["rum_deal"] = "bless_wrench"
        player["rum_deal_brewing_control"] = 1
        player.inventory.add("holy_wrench")

        // An arrow belonging to some other content, which must survive. Written straight into
        // the viewport because Player.hint always allocates slot 0 - see the note below.
        val otherArrow = 3
        player.viewport!!.hints[otherArrow] = 1

        val controls = GameObjects.find(Tile(2143, 5100, 1), "braindeath_island_brewing_controls_multi")
        player.itemOnObject(controls, player.inventory.indexOf("holy_wrench"))
        tick(6)

        val spirit = NPCs.at(player.tile.regionLevel).first { it.id == "evil_spirit" }
        val spiritArrow: Int = spirit["hint_index", -1]
        assertTrue(spiritArrow != -1, "the spirit should record its arrow slot")
        assertTrue(player.viewport!!.hints[spiritArrow] != 0)

        spirit.damage(5000, source = player)
        tick(6)

        assertEquals(0, player.viewport!!.hints[spiritArrow], "the spirit's arrow should be gone")
        assertTrue(player.viewport!!.hints[otherArrow] != 0, "other content's arrow must survive")
        // Note: Player.hint uses firstOrNull instead of indexOfFirst, so it always returns slot 0
        // and cannot currently allocate a second arrow. Clearing by index is still correct.
    }

    @Test
    fun `Inspecting a trashed patch says the soil is ruined`() {
        val player = createPlayer(Tile(2142, 5070))
        val trashed = GameObjects.find(Tile(2142, 5069), "blindweed_patch_trashed")
        tick(2)

        player.objectOption(trashed, "Inspect")
        tick(2)

        assertTrue(player.containsMessage("The soil here is too poor to farm on."))
    }
}
