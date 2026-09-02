package content.quest.member.myreque

import WorldTest
import containsMessage
import content.entity.combat.hit.damage
import dialogueContinue
import dialogueOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.InterfaceApi
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * In Search of the Myreque object locations:
 *  rope bridge rungs (3502, 3427-3430)   bridge trees   (3502, 3426) and (3502, 3431)
 *  hillside doors    (3508, 3446)        stalagmite     (3492, 9824)
 *  secret wall       (3480, 9837)        inn ladder     (3477, 9846)
 *  tavern trapdoor   (3495, 3465)
 */
class SearchMyrequeTest : WorldTest() {

    private fun Player.giveWeapons() {
        inventory.add("steel_longsword")
        inventory.add("steel_sword", 2)
        inventory.add("steel_dagger")
        inventory.add("steel_mace")
        inventory.add("steel_warhammer")
    }

    @Test
    fun `Vanstrom talks the player into arming the Myreque`() {
        val player = createPlayer(Tile(3503, 3476))
        val vanstrom = createNPC("stranger", Tile(3503, 3477))

        player.npcOption(vanstrom, "Talk-to")
        tick(2)
        player.skipDialogues() // greeting through "I'm at a loss of how I can help them."
        player.dialogueOption("line2") // Why do they need help? Are they in trouble?
        player.skipDialogues() // the weapons he has in mind
        player.dialogueOption("line4") // Perhaps I could help you out here.
        player.skipDialogues()
        player.dialogueOption("line4") // Yes, I'll do it!
        player.dialogueContinue()

        assertEquals("agreed_to_help", player["in_search_of_the_myreque", "unstarted"])
    }

    @Test
    fun `Cyreg refuses to take the weapons himself`() {
        val player = createPlayer(Tile(3522, 3285))
        val cyreg = createNPC("cyreg_paddlehorn", Tile(3522, 3284))
        player["in_search_of_the_myreque"] = "agreed_to_help"
        player.inventory.add("steel_longsword")
        player.inventory.add("steel_sword", 2)
        player.inventory.add("steel_dagger")
        player.inventory.add("steel_mace")
        player.inventory.add("steel_warhammer")

        player.npcOption(cyreg, "Talk-to")
        tick(2)
        player.skipDialogues() // he admits to having worked for them, then refuses

        assertEquals("refused_delivery", player["in_search_of_the_myreque", "unstarted"])
    }

    @Test
    fun `Cyreg won't lend the boat out before he has been talked round`() {
        val player = createPlayer(Tile(3522, 3285))
        createNPC("cyreg_paddlehorn", Tile(3522, 3284))
        val boat = GameObjects.find(Tile(3523, 3284), "swamp_boat_mort_ton")

        player.objectOption(boat, "Board")
        tick(4)

        assertEquals(Tile(3522, 3285), player.tile)
    }

    @Test
    fun `A rotten bridge rung breaks underfoot and can be nailed back together`() {
        val player = createPlayer(Tile(3502, 3427))
        player.inventory.add("plank", 6)
        player.inventory.add("steel_nails", 225)
        player.inventory.add("hammer")

        val rung = GameObjects.find(Tile(3502, 3428), "swamp_bridge1")
        player.objectOption(rung, "Walk-here")
        tick(2)
        player.dialogueContinue()

        val broken = GameObjects.find(Tile(3502, 3428), "spooky_tree_base_forbridge")
        player.objectOption(broken, "Repair")
        tick(6)

        assertTrue(player["bridgerung1", false], "the southern rung should be fixed")
        assertEquals(5, player.inventory.count("plank"))
        assertEquals(150, player.inventory.count("steel_nails"))
    }

    @Test
    fun `Repairing a rung without the materials tells the player so`() {
        val player = createPlayer(Tile(3502, 3427))
        val rung = GameObjects.find(Tile(3502, 3428), "swamp_bridge1")

        player.objectOption(rung, "Walk-here")
        tick(2)
        player.dialogueContinue()
        val broken = GameObjects.find(Tile(3502, 3428), "spooky_tree_base_forbridge")
        player.objectOption(broken, "Repair")
        tick(2)

        assertFalse(player["bridgerung1", false], "an empty handed player can't fix the bridge")
    }

    @Test
    fun `Rungs out of arm's reach can't be stepped onto or repaired`() {
        val player = createPlayer(Tile(3502, 3427))
        player.inventory.add("plank", 6)
        player.inventory.add("steel_nails", 225)
        player.inventory.add("hammer")

        val far = GameObjects.find(Tile(3502, 3430), "swamp_bridge1")
        player.objectOption(far, "Walk-here")
        tick(2)

        assertTrue(player.containsMessage("You can't reach that."))
        assertEquals(Tile(3502, 3427), player.tile, "the player stays put")
        assertNull(GameObjects.findOrNull(Tile(3502, 3430), "spooky_tree_base_forbridge"), "and the far rung holds")

        // Break the rung in front of them, then try to fix it from the other end of the bridge
        val near = GameObjects.find(Tile(3502, 3428), "swamp_bridge1")
        player.objectOption(near, "Walk-here")
        tick(2)
        player.dialogueContinue()
        val broken = GameObjects.find(Tile(3502, 3428), "spooky_tree_base_forbridge")
        player.tele(3502, 3425, 0)
        tick()
        player.objectOption(broken, "Repair")
        tick(4)

        assertFalse(player["bridgerung1", false], "a rung out of reach can't be repaired")
    }

    @Test
    fun `The trees are only in reach from the tile beside them`() {
        val player = createPlayer(Tile(3502, 3427))
        for (rung in 1..3) {
            player["bridgerung$rung"] = true
        }
        val tree = GameObjects.find(Tile(3502, 3431), "swamp_bridge_tree")

        // Step out onto the first rung, then try to climb the tree at the far end
        val rung = GameObjects.find(Tile(3502, 3428), "swamp_bridge1")
        player.objectOption(rung, "Walk-here")
        tick(3)
        assertEquals(Tile(3502, 3428), player.tile)

        player.objectOption(tree, "Climb")
        tick(3)
        assertTrue(player.containsMessage("You can't reach that."))
        assertEquals(Tile(3502, 3428), player.tile, "no shuffling along the bridge to it")

        // From the rung it stands on though, the tree drops the player off the bridge
        player.tele(3502, 3430, 0)
        tick()
        player.objectOption(tree, "Climb")
        tick(3)

        assertEquals(Tile(3503, 3431), player.tile, "the far bank")
    }

    @Test
    fun `The northern tree can't be climbed until the top rung is fixed`() {
        val player = createPlayer(Tile(3502, 3432))
        val tree = GameObjects.find(Tile(3502, 3431), "swamp_bridge_tree")

        player.objectOption(tree, "Climb")
        tick(3)

        assertNotEquals(Tile(3502, 3430), player.tile, "the broken top rung is nowhere to stand")
        player.dialogueContinue()

        player["bridgerung3"] = true
        player.objectOption(tree, "Climb")
        tick(4)

        assertEquals(Tile(3502, 3430), player.tile)
    }

    @Test
    fun `Curpile only asks his questions of someone carrying the weapons`() {
        val player = createPlayer(Tile(3508, 3441))
        val curpile = createNPC("curpile_fyod_mort_myre_swamp", Tile(3508, 3440))
        player["in_search_of_the_myreque"] = "reached_hollows"
        player.inventory.add("steel_longsword")
        player.inventory.add("steel_sword", 2)
        player.inventory.add("steel_dagger")
        player.inventory.add("steel_mace")
        player.inventory.add("steel_warhammer")

        player.npcOption(curpile, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption("line1") // I've come to help the Myreque, I've brought weapons.
        player.skipDialogues()

        assertEquals("questioned_by_curpile", player["in_search_of_the_myreque", "unstarted"])
    }

    @Test
    fun `Answering all three of Curpile's questions unlocks the door`() {
        val player = createPlayer(Tile(3508, 3441))
        val curpile = createNPC("curpile_fyod_mort_myre_swamp", Tile(3508, 3440))
        player["in_search_of_the_myreque"] = "questioned_by_curpile"
        player.giveWeapons()

        player.npcOption(curpile, "Talk-to")
        tick(2)
        player.dialogueContinue()
        // The three questions are drawn at random, so seed the draw and answer what he asks
        setRandom(Random(SEED))
        val asked = (0..5).shuffled(Random(SEED)).take(3)
        player.dialogueOption("line1") // I've come to help the Myreque, I've brought weapons.
        player.skipDialogues()
        for (question in asked) {
            player.dialogueOption("line${CORRECT_ANSWERS.getValue(question)}")
            player.skipDialogues()
        }
        setRandom(Random)

        assertEquals("answered_questions", player["in_search_of_the_myreque", "unstarted"])
    }

    @Test
    fun `Getting a question wrong has Curpile knock the player back to Mort'ton`() {
        val player = createPlayer(Tile(3508, 3441))
        val curpile = createNPC("curpile_fyod_mort_myre_swamp", Tile(3508, 3440))
        player["in_search_of_the_myreque"] = "questioned_by_curpile"
        player.giveWeapons()

        player.npcOption(curpile, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption("line1") // I've come to help the Myreque, I've brought weapons.
        player.skipDialogues()
        repeat(3) {
            player.dialogueOption("line5") // I don't know!
            player.skipDialogues()
        }
        tick(12)

        assertEquals("questioned_by_curpile", player["in_search_of_the_myreque", "unstarted"], "he doesn't let them past")
        assertEquals(Tile(3522, 3285), player.tile, "and they wake up back in Mort'ton")
    }

    @Test
    fun `Cyreg lends the boat out for three planks`() {
        val player = createPlayer(Tile(3522, 3285))
        val cyreg = createNPC("cyreg_paddlehorn", Tile(3522, 3284))
        player["in_search_of_the_myreque"] = "persuaded_boatman"
        player.inventory.add("druid_pouch_2", 5)
        player.inventory.add("silver_sickle_b")
        player.inventory.add("plank", 3)

        player.npcOption(cyreg, "Talk-to")
        tick(2)
        player.skipDialogues() // the ghast warning and the druid pouch
        player.dialogueOption("line1") // Give wooden planks to Cyreg.
        player.skipDialogues()

        assertEquals("gave_planks", player["in_search_of_the_myreque", "unstarted"])
        assertEquals(0, player.inventory.count("plank"), "he takes all three")
    }

    @Test
    fun `The boat ride to the Hollows costs ten gold`() {
        val player = createPlayer(Tile(3522, 3285))
        createNPC("cyreg_paddlehorn", Tile(3522, 3284))
        player["in_search_of_the_myreque"] = "gave_planks"
        player.inventory.add("coins", 10)
        val boat = GameObjects.find(Tile(3523, 3284), "swamp_boat_mort_ton")

        player.objectOption(boat, "Board")
        tick(4)
        player.dialogueContinue() // It costs 10 gold to cover the loan of the boat. Will you pay?
        player.dialogueOption("line1") // Yes. I'll pay the ten gold.
        player.dialogueContinue()
        tick(20)

        // Arriving is what counts, not clicking through the message that follows it
        assertEquals(Tile(3498, 3380), player.tile)
        assertEquals("reached_hollows", player["in_search_of_the_myreque", "unstarted"])
        assertEquals(0, player.inventory.count("coins"))
        player.dialogueContinue() // You arrive in the Hollows.
    }

    @Test
    fun `Veliaf sends the player off to meet the rest of the Myreque`() {
        val player = createPlayer(Tile(3507, 9838, 2))
        player["in_search_of_the_myreque"] = "met_veliaf"
        val veliaf = createNPC("veliaf_hurtz_meiyerditch_tunnels", Tile(3506, 9838, 2))
        val sani = createNPC("sani_piliu_meiyerditch_tunnels", Tile(3508, 9837, 2))

        player.npcOption(veliaf, "Talk-to")
        tick(2)
        player.dialogueContinue() // Have you introduced yourself to the rest of the gang?
        player.dialogueOption("line2") // Not yet, I'll go do that now.
        player.skipDialogues()
        assertEquals("met_veliaf", player["in_search_of_the_myreque", "unstarted"], "the weapons wait until he's met everyone")

        player.npcOption(sani, "Talk-to")
        tick(2)
        player.skipDialogues()

        assertTrue(player["met_sani", false], "introducing yourself is remembered")
    }

    @Test
    fun `The journal reads through every stage of the quest`() {
        val player = createPlayer(Tile(3503, 3476))

        for (stage in JOURNAL_STAGES) {
            player["in_search_of_the_myreque"] = stage
            InterfaceApi.openQuestJournal(player, "in_search_of_the_myreque")
            assertTrue(player.interfaces.contains("quest_scroll"), "the journal should open at $stage")
            player.interfaces.close("quest_scroll")
        }
    }

    @Test
    fun `The hillside doors stay shut until Curpile has been answered`() {
        val player = createPlayer(Tile(3508, 3447))
        val doors = GameObjects.find(Tile(3508, 3446), "swamp_wooden_doors_closed")

        player.objectOption(doors, "Open")
        tick(2)
        assertEquals(Tile(3508, 3447), player.tile)

        player["in_search_of_the_myreque"] = "answered_questions"
        player.objectOption(doors, "Open")
        tick(6)

        assertEquals("entered_hideout", player["in_search_of_the_myreque", "unstarted"])
        assertEquals(Tile(3500, 9812), player.tile)
    }

    @Test
    fun `The stalagmite leads to the hideout, and to the emptier one after the ambush`() {
        val player = createPlayer(Tile(3491, 9824))
        player["in_search_of_the_myreque"] = "entered_hideout"
        val stalagmite = GameObjects.find(Tile(3492, 9824), "route_stalagmite_cave_entrace")

        player.objectOption(stalagmite, "Squeeze-past")
        tick(2)
        player.dialogueContinue()
        tick(6)
        assertEquals(Tile(3505, 9832, 2), player.tile)

        player["in_search_of_the_myreque"] = "killed_hellhound"
        player.tele(3491, 9824, 0)
        tick()
        player.objectOption(stalagmite, "Squeeze-past")
        tick(2)
        player.dialogueContinue()
        tick(6)
        assertEquals(Tile(3505, 9832, 0), player.tile)
    }

    @Test
    fun `Vanstrom ambushes the hideout once the weapons are handed over`() {
        val player = createPlayer(Tile(3507, 9838, 2))
        player["in_search_of_the_myreque"] = "weapons_accepted"
        player.inventory.add("steel_longsword")
        player.inventory.add("steel_sword", 2)
        player.inventory.add("steel_dagger")
        player.inventory.add("steel_mace")
        player.inventory.add("steel_warhammer")
        val veliaf = createNPC("veliaf_hurtz_meiyerditch_tunnels", Tile(3506, 9838, 2))
        val sani = createNPC("sani_piliu_meiyerditch_tunnels", Tile(3508, 9837, 2))
        val harold = createNPC("harold_evans_meiyerditch_tunnels", Tile(3504, 9837, 2))

        player.npcOption(veliaf, "Talk-to")
        repeat(80) {
            tick()
            if (player.dialogue != null) {
                player.skipDialogues()
            }
        }

        assertEquals("hellhound_summoned", player["in_search_of_the_myreque", "unstarted"])
        assertEquals(Tile(3507, 9838, 0), player.tile, "the ambush leaves the player in the emptier room")
        assertEquals(0, player.inventory.count("steel_longsword"), "the weapons are handed over")
        assertTrue(player["thsfm_vanstrom_hide", false], "Vanstrom stops sitting in the tavern")
        val hound = NPCs.at(player.tile.regionLevel).firstOrNull { it.id == "skeleton_hellhound" }
        assertNotNull(hound, "a hell hound should be left behind")
        // The ambush runs in a copy of the room, so the real Sani and Harold are never touched
        assertNull(player.get<Int>("instance"), "the cutscene instance should be cleaned up")
        assertEquals(Tile(3508, 9837, 2), sani.tile)
        assertEquals(sani.levels.getMax(Skill.Constitution), sani.levels.get(Skill.Constitution))
        assertEquals(harold.levels.getMax(Skill.Constitution), harold.levels.get(Skill.Constitution))
    }

    @Test
    fun `Killing the hell hound saves the rest of the Myreque`() {
        val player = createPlayer(Tile(3506, 9838))
        player["in_search_of_the_myreque"] = "hellhound_summoned"
        val hound = spawnHellHound(player)
        tick(2)

        hound.damage(5000, source = player)
        tick(6)

        assertEquals("killed_hellhound", player["in_search_of_the_myreque", "unstarted"])
    }

    @Test
    fun `The hell hound fights with its own animations and sounds`() {
        val definition = get<CombatDefinitions>().getOrNull("skeleton_hellhound")
        assertNotNull(definition, "the hell hound needs a combat definition to fight with")

        assertEquals("skeleton_hellhound_defend", definition.defendAnim)
        assertEquals("skeleton_hellhound_death", definition.deathAnim)
        assertEquals("skeleton_hellhound_death", definition.deathSound?.id)

        val melee = definition.attacks["melee"]
        assertNotNull(melee, "the hell hound needs a melee attack")
        assertEquals("skeleton_hellhound_attack", melee.anim)
        assertEquals("skeleton_hellhound_attack", melee.sounds.first().id)
        assertEquals("skeleton_hellhound_hit", melee.targetSounds.first().id)
        val hit = melee.targetHits.first()
        assertEquals("crush", hit.offense)
        assertEquals(120, hit.max)
    }

    @Test
    fun `The hell hound gives up once the player leaves the hideout`() {
        val player = createPlayer(Tile(3506, 9838))
        player["in_search_of_the_myreque"] = "hellhound_summoned"
        val hound = spawnHellHound(player)
        tick(2)

        player.tele(3491, 9824, 0)
        tick(3)

        assertEquals(-1, hound.index, "the hound should be gone")
    }

    @Test
    fun `The hell hound leaves bones and rubies behind`() {
        val player = createPlayer(Tile(3506, 9838))
        player["in_search_of_the_myreque"] = "hellhound_summoned"
        val hound = spawnHellHound(player)
        tick(2)

        val deathTile = hound.tile
        hound.damage(5000, source = player)
        tick(8)

        // Bones and rubies don't stack, so each one is its own pile
        val drops = FloorItems.at(deathTile.zone).flatten()
        assertEquals(4, drops.count { it.id == "big_bones" })
        assertEquals(2, drops.count { it.id == "uncut_ruby" })
    }

    @Test
    fun `Veliaf's secret wall only opens once he has pointed it out`() {
        val player = createPlayer(Tile(3480, 9838))
        val wall = GameObjects.find(Tile(3480, 9837), "canifis_fake_wall_closed")

        player.objectOption(wall, "Search")
        tick(2)
        assertTrue(player.containsMessage("You see nothing interesting with this wall."))

        player["in_search_of_the_myreque"] = "shown_way_out"
        player.objectOption(wall, "Search")
        tick(4)

        assertEquals("in_inn_basement", player["in_search_of_the_myreque", "unstarted"])
    }

    @Test
    fun `The inn ladder comes out beside the tavern`() {
        val player = createPlayer(Tile(3477, 9845))
        player["in_search_of_the_myreque"] = "in_inn_basement"
        val ladder = GameObjects.find(Tile(3477, 9846), "canifis_tavern_ladder")

        player.objectOption(ladder, "Climb-up")
        tick(4)

        assertEquals("escaped_to_canifis", player["in_search_of_the_myreque", "unstarted"])
        assertEquals(Tile(3495, 3466), player.tile)
    }

    @Test
    fun `The tavern trapdoor is a shortcut only for those who finished the quest`() {
        val player = createPlayer(Tile(3495, 3466))
        val trapdoor = GameObjects.find(Tile(3495, 3465), "canifis_tavern_trapdoor")

        player.objectOption(trapdoor, "Open")
        tick(4)
        assertTrue(player.containsMessage("This trap door seems locked"))
        assertEquals(Tile(3495, 3466), player.tile, "and it stays shut")

        player["in_search_of_the_myreque"] = "completed"
        player.objectOption(trapdoor, "Open")
        tick(4)

        assertEquals(Tile(3477, 9845), player.tile)
    }

    @Test
    fun `Telling the stranger about Vanstrom finishes the quest`() {
        val player = createPlayer(Tile(3503, 3476))
        player["in_search_of_the_myreque"] = "escaped_to_canifis"
        player["thsfm_vanstrom_hide"] = true
        val stranger = createNPC("stranger", Tile(3503, 3477))
        val points = player["quest_points", 0]

        player.npcOption(stranger, "Talk-to")
        tick(2)
        player.skipDialogues()

        assertEquals("completed", player["in_search_of_the_myreque", "unstarted"])
        assertEquals(points + 2, player["quest_points", 0])
    }

    private companion object {
        /** Any seed will do, as long as the test draws the same three questions Curpile does. */
        private const val SEED = 42

        /** Which option each of Curpile's questions has the right answer on. */
        private val CORRECT_ANSWERS = mapOf(0 to 4, 1 to 3, 2 to 1, 3 to 2, 4 to 2, 5 to 4)

        /** One stage from every branch of the journal. */
        private val JOURNAL_STAGES = listOf(
            "unstarted",
            "agreed_to_help",
            "refused_delivery",
            "persuaded_boatman",
            "gave_planks",
            "reached_hollows",
            "questioned_by_curpile",
            "answered_questions",
            "entered_hideout",
            "met_veliaf",
            "weapons_accepted",
            "delivered_weapons",
            "hellhound_summoned",
            "killed_hellhound",
            "shown_way_out",
            "in_inn_basement",
            "escaped_to_canifis",
            "completed",
        )
    }
}
