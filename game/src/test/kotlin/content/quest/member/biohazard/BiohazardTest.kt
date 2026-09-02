package content.quest.member.biohazard

import WorldTest
import containsMessage
import content.entity.combat.hit.damage
import dialogueOption
import equipItem
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import itemOnObject
import itemOption
import kotlinx.coroutines.test.runTest
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.client.ui.InterfaceApi
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.encode.zone.SoundAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ZoneUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BiohazardTest : WorldTest() {

    @Test
    fun `Elena sets the player looking for her distillator`() {
        val player = createPlayer(Tile(2592, 3335))
        player["plague_city"] = "completed"
        val elena = createNPC("elena2_vis", Tile(2592, 3336))

        player.npcOption(elena, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line1")
        player.skipDialogues()

        assertEquals("started", player["biohazard", "unstarted"])
        assertFalse(player["plaguecity_dug_mud_pile", false], "the mourners fill Edmond's tunnel back in")
    }

    @Test
    fun `Elena takes no for an answer`() {
        val player = createPlayer(Tile(2592, 3335))
        player["plague_city"] = "completed"
        val elena = createNPC("elena2_vis", Tile(2592, 3336))

        player.npcOption(elena, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line2")
        player.skipDialogues()

        assertEquals("unstarted", player["biohazard", "unstarted"])
    }

    @Test
    fun `Questprep leaves a player able to walk up to Elena and start`() {
        val player = createPlayer(Tile(2592, 3335), name = "biohazard prep")
        player.rights = PlayerRights.Admin
        runTest { Commands.call(player, "questprep biohazard") }
        tick()

        val elena = createNPC("elena2", Tile(2592, 3336))
        player.npcOption(elena, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line1")
        player.skipDialogues()

        assertEquals("started", player["biohazard", "unstarted"])
    }

    @Test
    fun `Elena won't start the quest before Plague City is finished`() {
        val player = createPlayer(Tile(2592, 3335))
        val elena = createNPC("elena2_vis", Tile(2592, 3336))

        player.npcOption(elena, "Talk-to")
        tick(2)
        player.skipDialogues()

        assertEquals("unstarted", player["biohazard", "unstarted"])
    }

    @Test
    fun `Jerico arranges the rope ladder and stocks the bird feed`() {
        val player = createPlayer(Tile(2612, 3325))
        player["biohazard"] = "started"
        val jerico = createNPC("jerico", Tile(2612, 3324))

        player.npcOption(jerico, "Talk-to")
        tick(2)
        player.skipDialogues()
        assertEquals("spoke_to_jerico", player["biohazard", "unstarted"])

        player.tele(2611, 3325)
        val cupboard = GameObjects.find(Tile(2611, 3326), "jericos_cupboard_shut")
        player.objectOption(cupboard, "Open")
        tick(2)
        val open = GameObjects.find(Tile(2611, 3326), "jericos_cupboard_open")
        player.objectOption(open, "Search")
        tick(2)
        player.skipDialogues()

        assertEquals(1, player.inventory.count("bird_feed"))
    }

    @Test
    fun `Bird feed thrown on the watchtower goes unnoticed on its own`() {
        val player = createPlayer(Tile(2560, 3301))
        player["biohazard"] = "spoke_to_jerico"
        player.inventory.add("bird_feed")
        val fence = GameObjects.find(Tile(2560, 3301), "bio_watchtower_fence")

        player.itemOnObject(fence, player.inventory.indexOf("bird_feed"))
        tick(6)

        assertEquals("birdfeed_thrown", player["biohazard", "unstarted"])
        assertEquals(0, player.inventory.count("bird_feed"))
        assertTrue(player.containsMessage("You throw a handful of seeds onto the watchtower."))
        assertTrue(player.containsMessage("The mourners do not seem to notice."))
    }

    @Test
    fun `Pigeons let out beside the seeded tower swarm the mourners`() {
        val player = createPlayer(Tile(2560, 3301))
        player["biohazard"] = "birdfeed_thrown"
        player.inventory.add("pigeon_cage_full")

        player.itemOption("Open", "pigeon_cage_full")
        tick(6)

        assertEquals("pigeons_released", player["biohazard", "unstarted"])
        assertEquals(0, player.inventory.count("pigeon_cage_full"))
        assertEquals(1, player.inventory.count("pigeon_cage_empty"), "the cage is kept, empty")
        assertTrue(player.containsMessage("The pigeons fly towards the watchtower."))
        assertTrue(player.containsMessage("The mourners are frantically trying to scare the pigeons away."))
    }

    @Test
    fun `Pigeons stay put when let out away from the watchtower`() {
        val player = createPlayer(Tile(2592, 3335))
        player["biohazard"] = "birdfeed_thrown"
        player.inventory.add("pigeon_cage_full")

        player.itemOption("Open", "pigeon_cage_full")
        tick(2)

        assertEquals("birdfeed_thrown", player["biohazard", "unstarted"])
        assertEquals(1, player.inventory.count("pigeon_cage_full"))
        assertTrue(player.containsMessage("The pigeons don't want to leave."))
    }

    @Test
    fun `Bird feed does nothing once the pigeons have already flown`() {
        val player = createPlayer(Tile(2560, 3301))
        player["biohazard"] = "crossed_wall"
        player.inventory.add("bird_feed")
        val fence = GameObjects.find(Tile(2560, 3301), "bio_watchtower_fence")

        player.itemOnObject(fence, player.inventory.indexOf("bird_feed"))
        tick(2)

        assertEquals(1, player.inventory.count("bird_feed"), "the feed is kept")
        assertTrue(player.containsMessage("Nothing interesting happens."))
    }

    @Test
    fun `Omart's rope ladder drops the player into West Ardougne`() {
        val player = createPlayer(Tile(2559, 3265))
        player["biohazard"] = "pigeons_released"
        val omart = createNPC("omart_west_ardougne", Tile(2559, 3266))

        player.npcOption(omart, "Talk-to")
        playThrough(player, 12)
        player.dialogueOption("line1")
        playThrough(player, 8)

        assertEquals("crossed_wall", player["biohazard", "unstarted"])
        assertEquals(Tile(2556, 3267), player.tile)
    }

    @Test
    fun `Kilron takes the player back over the wall`() {
        val player = createPlayer(Tile(2557, 3265))
        player["biohazard"] = "crossed_wall"
        val kilron = createNPC("kilron_west_ardougne", Tile(2557, 3266))

        player.npcOption(kilron, "Talk-to")
        playThrough(player, 12)
        player.dialogueOption("line2")
        playThrough(player, 8)

        assertEquals(Tile(2559, 3267), player.tile)
    }

    @Test
    fun `The gap in the fence leads through to the mourners' yard`() {
        val player = createPlayer(Tile(2541, 3331))
        val fence = GameObjects.find(Tile(2541, 3331), "mourner_stew_fence")

        player.objectOption(fence, "Squeeze-through")
        tick(8)

        assertEquals(Tile(2542, 3331), player.tile)
        assertTrue(player.containsMessage("You squeeze through the fence."))
    }

    @Test
    fun `A rotten apple in the cauldron poisons the mourners' stew`() {
        val player = createPlayer(Tile(2543, 3331))
        player["biohazard"] = "crossed_wall"
        player.inventory.add("rotten_apple")
        val cauldron = GameObjects.find(Tile(2543, 3332), "mourner_stew_cauldron")

        player.itemOnObject(cauldron, player.inventory.indexOf("rotten_apple"))
        tick(12)

        assertEquals("poisoned_stew", player["biohazard", "unstarted"])
        assertEquals(0, player.inventory.count("rotten_apple"))
        assertTrue(player.containsMessage("and it quickly dissolves into the stew."))
    }

    @Test
    fun `Eating a rotten apple is its own punishment`() {
        val player = createPlayer(Tile(2543, 3331))
        player.inventory.add("rotten_apple")

        player.itemOption("Eat", "rotten_apple")
        tick(2)
        player.skipDialogues()

        assertEquals(0, player.inventory.count("rotten_apple"))
        assertTrue(player.containsMessage("...but it's rotten so you spit it out."))
    }

    @Test
    fun `The nurse only leaves her gown out once the mourners fall ill`() {
        val player = createPlayer(Tile(2516, 3277))
        player["biohazard"] = "crossed_wall"
        val box = GameObjects.find(Tile(2516, 3278), "bio_nurses_cupboard_shut")

        player.objectOption(box, "Open")
        tick(2)
        val open = GameObjects.find(Tile(2516, 3278), "bio_nurses_cupboard_open")
        player.objectOption(open, "Search")
        tick(2)
        assertEquals(0, player.inventory.count("doctors_gown"))
        assertTrue(player.containsMessage("but you find nothing of interest."))

        player["biohazard"] = "poisoned_stew"
        player.objectOption(open, "Search")
        tick(2)

        assertEquals(1, player.inventory.count("doctors_gown"))
        assertTrue(player.containsMessage("and find a doctor's gown."))
    }

    @Test
    fun `The mourners' door is locked until their stew is poisoned`() {
        val player = createPlayer(Tile(2551, 3320))
        player["biohazard"] = "crossed_wall"
        val door = GameObjects.find(Tile(2551, 3320), "mourner_stew_door_closed")

        player.objectOption(door, "Open")
        tick(2)
        player.skipDialogues()

        assertNotEquals(Tile(2551, 3321), player.tile, "the door stays shut")
    }

    @Test
    fun `Only the doctor gets past the mourner on the door`() {
        val player = createPlayer(Tile(2551, 3320))
        player["biohazard"] = "poisoned_stew"
        val door = GameObjects.find(Tile(2551, 3320), "mourner_stew_door_closed")

        player.objectOption(door, "Open")
        tick(2)
        player.skipDialogues()
        assertNotEquals(Tile(2551, 3321), player.tile)

        player.inventory.add("doctors_gown")
        player.equipItem("doctors_gown", option = "Wear")
        tick(2)
        player.objectOption(door, "Open")
        tick(2)
        player.skipDialogues()
        tick(6)

        assertEquals(Tile(2551, 3321), player.tile)
    }

    @Test
    fun `The landing door needs the gown but never traps anyone upstairs`() {
        val player = createPlayer(Tile(2546, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        val door = GameObjects.find(Tile(2547, 3325, 1), "mourner_stew_door_up_closed")

        player.objectOption(door, "Open")
        tick(4)
        assertTrue(player.containsMessage("The mourner is refusing to open the door."))
        assertEquals(Tile(2546, 3325, 1), player.tile)

        player.inventory.add("doctors_gown")
        player.equipItem("doctors_gown", option = "Wear")
        tick(2)
        player.objectOption(door, "Open")
        tick(6)

        assertEquals(Tile(2547, 3325, 1), player.tile)
    }

    @Test
    fun `A sick mourner is carrying the key to the quarters gate`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        val mourner = createNPC("mourner_2", Tile(2551, 3324, 1))
        val deathTile = mourner.tile

        mourner.damage(5000, source = player)
        tick(DEATH_TICKS)

        assertEquals(1, keysOn(deathTile), "the mourner should drop a key")
    }

    @Test
    fun `Mourners keep hold of the key outside the quest`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        val mourner = createNPC("mourner_2", Tile(2551, 3324, 1))
        val deathTile = mourner.tile

        mourner.damage(5000, source = player)
        tick(DEATH_TICKS)

        assertEquals(0, keysOn(deathTile))
    }

    @Test
    fun `A second key isn't dropped for someone already carrying one`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        player.inventory.add("key_biohazard")
        val mourner = createNPC("mourner_2", Tile(2551, 3324, 1))
        val deathTile = mourner.tile

        mourner.damage(5000, source = player)
        tick(DEATH_TICKS)

        assertEquals(0, keysOn(deathTile))
    }

    @Test
    fun `The quarters gate only opens for the key`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        val gate = GameObjects.find(Tile(2552, 3325, 1), "mourner_quarters_gate_left_closed")

        player.objectOption(gate, "Open")
        tick(4)
        assertTrue(player.containsMessage("You need a key."))
        assertEquals(Tile(2551, 3325, 1), player.tile)

        player.inventory.add("key_biohazard")
        player.itemOnObject(gate, player.inventory.indexOf("key_biohazard"))
        tick(8)

        assertTrue(player.containsMessage("The key fits the gate."))
        assertEquals(Tile(2552, 3325, 1), player.tile)
    }

    @Test
    fun `Opening the quarters gate uses the key already in hand`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        player.inventory.add("key_biohazard")
        val gate = GameObjects.find(Tile(2552, 3325, 1), "mourner_quarters_gate_left_closed")

        player.objectOption(gate, "Open")
        tick(8)

        assertTrue(player.containsMessage("The key fits the gate."))
        assertEquals(Tile(2552, 3325, 1), player.tile)
        assertEquals(1, player.inventory.count("key_biohazard"), "the key isn't used up")
    }

    @Test
    fun `The quarters gate opens from the inside without a key`() {
        val player = createPlayer(Tile(2552, 3325, 1))
        player["biohazard"] = "found_distillator"
        val gate = GameObjects.find(Tile(2552, 3325, 1), "mourner_quarters_gate_left_closed")

        player.objectOption(gate, "Open")
        tick(8)

        assertEquals(Tile(2551, 3325, 1), player.tile)
        assertFalse(player.containsMessage("You need a key."))
    }

    @Test
    fun `Leaving the quarters says nothing about the key you are carrying`() {
        val player = createPlayer(Tile(2552, 3325, 1))
        player["biohazard"] = "found_distillator"
        player.inventory.add("key_biohazard")
        val gate = GameObjects.find(Tile(2552, 3325, 1), "mourner_quarters_gate_left_closed")

        player.objectOption(gate, "Open")
        tick(8)

        assertEquals(Tile(2551, 3325, 1), player.tile)
        assertFalse(player.containsMessage("The key fits the gate."), "nothing is unlocked on the way out")
    }

    @Test
    fun `Clicking the quarters gate from off its row walks you onto it first`() {
        val player = createPlayer(Tile(2551, 3324, 1))
        player["biohazard"] = "poisoned_stew"
        player.inventory.add("key_biohazard")
        val gate = GameObjects.find(Tile(2552, 3325, 1), "mourner_quarters_gate_left_closed")

        player.objectOption(gate, "Open")
        tick(12)

        assertEquals(Tile(2552, 3325, 1), player.tile)
    }

    @Test
    fun `The right half of the quarters gate takes you through its own row`() {
        val player = createPlayer(Tile(2551, 3326, 1))
        player["biohazard"] = "poisoned_stew"
        player.inventory.add("key_biohazard")
        val gate = GameObjects.find(Tile(2552, 3326, 1), "mourner_quarters_gate_right_closed")

        player.objectOption(gate, "Open")
        tick(12)

        assertEquals(Tile(2552, 3326, 1), player.tile, "the row is taken from the half that was clicked")
    }

    @Test
    fun `The quarters gate swings onto the row it started on`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        player.inventory.add("key_biohazard")
        val gate = GameObjects.find(Tile(2552, 3325, 1), "mourner_quarters_gate_left_closed")

        player.objectOption(gate, "Open")
        tick(2)

        val left = GameObjects.findOrNull(Tile(2551, 3325, 1), 2059)
        assertNotNull(left, "the left half should swing west onto its own row")
        assertEquals(3, left.rotation)
        val right = GameObjects.findOrNull(Tile(2551, 3326, 1), 2061)
        assertNotNull(right, "and the right half onto its own")
        assertEquals(1, right.rotation)
    }

    @Test
    fun `The quarters gate bangs shut behind you like every other gate`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        player.inventory.add("key_biohazard")
        val gate = GameObjects.find(Tile(2552, 3325, 1), "mourner_quarters_gate_left_closed")
        mockkObject(ZoneBatchUpdates)
        try {
            player.objectOption(gate, "Open")
            tick(8)

            val updates = mutableListOf<ZoneUpdate>()
            verify { ZoneBatchUpdates.add(any<Zone>(), capture(updates)) }
            val sounds = updates.filterIsInstance<SoundAddition>()
            assertEquals(listOf(66), sounds.map { it.id })
        } finally {
            unmockkObject(ZoneBatchUpdates)
        }
    }

    @Test
    fun `The crates around the distillator's are empty`() {
        val player = createPlayer(Tile(2553, 3325, 1))
        val crate = GameObjects.find(Tile(2553, 3324, 1), "crate_272")

        player.objectOption(crate, "Search")
        tick(2)

        assertTrue(player.containsMessage("You search the crate but find nothing."))
    }

    @Test
    fun `The other crate model stacked by the distillator is empty too`() {
        val player = createPlayer(Tile(2553, 3325, 1))
        val crate = createObject("crate_273", Tile(2553, 3326, 1))

        player.objectOption(crate, "Search")
        tick(4)

        assertTrue(player.containsMessage("You search the crate but find nothing."))
    }

    @Test
    fun `The mourner tunnel trapdoor is bolted from below`() {
        val player = createPlayer(Tile(2542, 3326))
        val trapdoor = GameObjects.find(Tile(2542, 3327), "trapdoor_mourner_tunnels")

        player.objectOption(trapdoor, "Open")
        tick(2)

        assertTrue(player.containsMessage("The trapdoor is bolted on the other side."))
        assertEquals(Tile(2542, 3326), player.tile)
    }

    @Test
    fun `The plague sample doesn't survive a teleport`() {
        val player = createPlayer(Tile(2592, 3335))
        player["biohazard"] = "smuggle_chemicals"
        player.inventory.add("plague_sample")
        player.inventory.add("plague_sample")
        player.inventory.add("plague_sample")
        player.inventory.add("ethenea")
        assertEquals(3, player.inventory.count("plague_sample"), "three separate slots, it doesn't stack")

        Teleport.teleport(player, Tile(3222, 3218), "modern")
        tick(12)

        assertEquals(0, player.inventory.count("plague_sample"))
        assertTrue(player.containsMessage("The Plague Sample is fragile and is destroyed in the crossing."))
        assertEquals(1, player.inventory.count("ethenea"), "the vials travel fine")
    }

    @Test
    fun `The plague sample doesn't survive any of the other teleports either`() {
        for (type in listOf("jewellery", "wilderness", "tablet", "scroll", "fairy_ring", "ectophial")) {
            val player = createPlayer(Tile(2592, 3335), name = "teleporter $type")
            player["biohazard"] = "smuggle_chemicals"
            player.inventory.add("plague_sample")

            Teleport.teleport(player, Tile(3222, 3218), type)
            tick(12)

            assertEquals(0, player.inventory.count("plague_sample"), "$type should destroy the sample")
            assertTrue(player.containsMessage("The Plague Sample is fragile and is destroyed in the crossing."), type)
        }
    }

    @Test
    fun `The crate upstairs holds Elena's distillator, and only one of them`() {
        val player = createPlayer(Tile(2554, 3326, 1))
        player["biohazard"] = "poisoned_stew"
        val crate = GameObjects.find(Tile(2554, 3327, 1), "mourner_crate_up")

        player.objectOption(crate, "Search")
        tick(8)
        assertEquals("found_distillator", player["biohazard", "unstarted"])
        assertEquals(1, player.inventory.count("distillator"))

        player.objectOption(crate, "Search")
        tick(8)

        assertEquals(1, player.inventory.count("distillator"))
        assertTrue(player.containsMessage("It's empty."))
    }

    @Test
    fun `Elena swaps the distillator for a set of chemicals`() {
        val player = createPlayer(Tile(2592, 3335))
        player["biohazard"] = "found_distillator"
        player.inventory.add("distillator")
        val elena = createNPC("elena2_vis", Tile(2592, 3336))

        player.npcOption(elena, "Talk-to")
        playThrough(player, 20)

        assertEquals("collect_chemicals", player["biohazard", "unstarted"])
        assertEquals(0, player.inventory.count("distillator"))
        for (item in CHEMICALS) {
            assertEquals(1, player.inventory.count(item), "Elena should hand over $item")
        }
    }

    @Test
    fun `Elena replaces chemicals the player has lost`() {
        val player = createPlayer(Tile(2592, 3335))
        player["biohazard"] = "collect_chemicals"
        player.inventory.add("ethenea")
        val elena = createNPC("elena2_vis", Tile(2592, 3336))

        player.npcOption(elena, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line2")
        player.skipDialogues()

        for (item in CHEMICALS) {
            assertEquals(1, player.inventory.count(item), "Elena should replace $item")
        }
    }

    @Test
    fun `The chemist hands over touch paper for Guidor`() {
        val player = createPlayer(Tile(2934, 3209))
        player["biohazard"] = "collect_chemicals"
        player.inventory.add("plague_sample")
        val chemist = createNPC("chemist_rimmington", Tile(2934, 3210))

        player.npcOption(chemist, "Talk-to")
        tick(2)
        player.dialogueOption("line2")
        player.skipDialogues()
        player.dialogueOption("line2")
        player.skipDialogues()
        player.dialogueOption("line2")
        player.skipDialogues()

        assertEquals("smuggle_chemicals", player["biohazard", "unstarted"])
        assertEquals(1, player.inventory.count("touch_paper"))
        assertEquals(1, player.inventory.count("plague_sample"), "the sample stays hidden")
    }

    @Test
    fun `Waving the plague sample at the chemist loses it`() {
        val player = createPlayer(Tile(2934, 3209))
        player["biohazard"] = "collect_chemicals"
        player.inventory.add("plague_sample")
        val chemist = createNPC("chemist_rimmington", Tile(2934, 3210))

        player.npcOption(chemist, "Talk-to")
        tick(2)
        player.dialogueOption("line2")
        player.skipDialogues()
        player.dialogueOption("line1")
        player.skipDialogues()

        assertEquals(0, player.inventory.count("plague_sample"))
        assertTrue(player.containsMessage("He takes the plague sample from you."))
    }

    @Test
    fun `Each errand boy takes a vial off to Varrock`() {
        val player = createPlayer(Tile(2928, 3218))
        player["biohazard"] = "smuggle_chemicals"
        for (item in VIALS) {
            player.inventory.add(item)
        }

        talkTo(player, createNPC("da_vinci_rimmington", Tile(2927, 3218)), "line1")
        assertEquals("delivered", player["biohazard_davinci_errand", "none"])
        assertEquals(0, player.inventory.count("ethenea"))

        moveTo(player, Tile(2929, 3221))
        talkTo(player, createNPC("chancy_rimmington", Tile(2929, 3222)), "line3")
        assertEquals("gambled", player["biohazard_chancy_errand", "none"])
        assertEquals(0, player.inventory.count("sulphuric_broline"))

        moveTo(player, Tile(2930, 3217))
        talkTo(player, createNPC("hops_rimmington", Tile(2930, 3218)), "line2")
        assertEquals("drank", player["biohazard_hops_errand", "none"])
        assertEquals(0, player.inventory.count("liquid_honey"))
    }

    @Test
    fun `An errand boy only hands back the vial he had no use for`() {
        val player = createPlayer(Tile(3272, 3388))
        player["biohazard"] = "smuggle_chemicals"
        player["biohazard_davinci_errand"] = "delivered"
        player["biohazard_chancy_errand"] = "gambled"
        player["biohazard_hops_errand"] = "drank"

        talkTo(player, createNPC("da_vinci", Tile(3272, 3389)))
        assertEquals(1, player.inventory.count("ethenea"), "Da Vinci brought the ethenea through")
        assertEquals("none", player["biohazard_davinci_errand", "none"])

        moveTo(player, Tile(3271, 3387))
        talkTo(player, createNPC("chancy", Tile(3271, 3388)), "line1")
        assertEquals(0, player.inventory.count("sulphuric_broline"), "Chancy sold it")
        assertEquals("none", player["biohazard_chancy_errand", "none"])

        moveTo(player, Tile(3268, 3388))
        talkTo(player, createNPC("hops", Tile(3268, 3389)))
        assertEquals(0, player.inventory.count("liquid_honey"), "Hops drank it")
        assertEquals("none", player["biohazard_hops_errand", "none"])
    }

    @Test
    fun `An errand boy won't take a second vial`() {
        val player = createPlayer(Tile(2928, 3218))
        player["biohazard"] = "smuggle_chemicals"
        player["biohazard_davinci_errand"] = "delivered"
        player.inventory.add("liquid_honey")

        talkTo(player, createNPC("da_vinci_rimmington", Tile(2927, 3218)))

        assertEquals(1, player.inventory.count("liquid_honey"), "he's already carrying one")
        assertEquals("delivered", player["biohazard_davinci_errand", "none"])
    }

    @Test
    fun `The Varrock gate guard confiscates every vial carried through`() {
        val player = createPlayer(Tile(3263, 3405))
        player["biohazard"] = "smuggle_chemicals"
        for (item in VIALS) {
            player.inventory.add(item)
        }
        player.inventory.add("touch_paper")
        val gate = GameObjects.find(Tile(3264, 3405), "guidor_gate_left_closed")

        player.objectOption(gate, "Open")
        playThrough(player, 20)

        for (item in VIALS) {
            assertEquals(0, player.inventory.count(item), "the guard should take the $item")
        }
        assertEquals(1, player.inventory.count("touch_paper"), "paper isn't a vial")
        assertEquals(Tile(3264, 3405), player.tile)
    }

    @Test
    fun `Guidor's wife only lets a priest into the bedroom`() {
        val player = createPlayer(Tile(3282, 3382))
        player["biohazard"] = "smuggle_chemicals"
        val door = GameObjects.find(Tile(3282, 3382), "guidor_door_closed")

        player.objectOption(door, "Open")
        tick(2)
        player.skipDialogues()
        assertTrue(player.containsMessage("Guidor's wife refuses to let you enter."))
        assertNotEquals(Tile(3283, 3382), player.tile)

        player.inventory.add("priest_gown_top")
        player.inventory.add("priest_gown_bottom")
        player.equipItem("priest_gown_top", option = "Wear")
        tick()
        player.equipItem("priest_gown_bottom", option = "Wear")
        tick(2)
        player.objectOption(door, "Open")
        tick(6)

        assertTrue(player.containsMessage("Guidor's wife allows you to go in."))
        assertEquals(Tile(3283, 3382), player.tile)
    }

    @Test
    fun `Guidor tests the sample and finds no plague`() {
        val player = createPlayer(Tile(3284, 3383))
        player["biohazard"] = "smuggle_chemicals"
        for (item in CHEMICALS) {
            player.inventory.add(item)
        }
        player.inventory.add("touch_paper")
        val guidor = createNPC("guidor", Tile(3284, 3382))

        player.npcOption(guidor, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line1")
        player.skipDialogues()
        player.dialogueOption("line2")
        player.skipDialogues()

        assertEquals("sample_tested", player["biohazard", "unstarted"])
        for (item in CHEMICALS) {
            assertEquals(0, player.inventory.count(item), "Guidor uses the $item")
        }
        assertEquals(0, player.inventory.count("touch_paper"))
    }

    @Test
    fun `Guidor sends the player away without all three reagents`() {
        val player = createPlayer(Tile(3284, 3383))
        player["biohazard"] = "smuggle_chemicals"
        player.inventory.add("plague_sample")
        player.inventory.add("ethenea")
        player.inventory.add("touch_paper")
        val guidor = createNPC("guidor", Tile(3284, 3382))

        player.npcOption(guidor, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line1")
        player.skipDialogues()
        player.dialogueOption("line2")
        player.skipDialogues()

        assertEquals("smuggle_chemicals", player["biohazard", "unstarted"])
        assertEquals(1, player.inventory.count("plague_sample"), "nothing is taken")
    }

    @Test
    fun `Elena sends the player to confront the king`() {
        val player = createPlayer(Tile(2592, 3335))
        player["biohazard"] = "sample_tested"
        val elena = createNPC("elena2_vis", Tile(2592, 3336))

        player.npcOption(elena, "Talk-to")
        tick(2)
        player.skipDialogues()

        assertEquals("told_elena", player["biohazard", "unstarted"])
    }

    @Test
    fun `King Lathas admits the hoax and completes the quest`() {
        val player = createPlayer(Tile(2577, 3299, 1))
        player["biohazard"] = "told_elena"
        val lathas = createNPC("king_lathas", Tile(2577, 3298, 1))
        val points = player["quest_points", 0]

        player.npcOption(lathas, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line1")
        player.skipDialogues()

        assertEquals("completed", player["biohazard", "unstarted"])
        assertEquals(points + 3, player["quest_points", 0])
        assertEquals(1250.0, player.experience.get(Skill.Thieving))
    }

    @Test
    fun `The king won't talk about the plague before Guidor's findings`() {
        val player = createPlayer(Tile(2577, 3299, 1))
        val lathas = createNPC("king_lathas", Tile(2577, 3298, 1))

        player.npcOption(lathas, "Talk-to")
        tick(2)

        assertTrue(player.containsMessage("The king is too busy to talk."))
    }

    @Test
    fun `The training camp gate opens once the quest is done`() {
        val player = createPlayer(Tile(2517, 3356))
        val gate = GameObjects.find(Tile(2517, 3356), "lathas_training_gate_left_closed")

        player.objectOption(gate, "Open")
        tick(2)
        player.skipDialogues()
        tick(4)

        assertEquals(Tile(2517, 3356), player.tile, "the gate stays shut")
    }

    @Test
    fun `The training camp gate swings onto the column it started on`() {
        val player = createPlayer(Tile(2517, 3356))
        player["biohazard"] = "completed"
        val gate = GameObjects.find(Tile(2517, 3356), "lathas_training_gate_left_closed")

        player.objectOption(gate, "Open")
        tick(2)
        player.skipDialogues()
        tick(1)

        val left = GameObjects.findOrNull(Tile(2517, 3357), 2040)
        assertNotNull(left, "the left half should swing north onto its own column")
        assertEquals(0, left.rotation)
        val right = GameObjects.findOrNull(Tile(2518, 3357), 2042)
        assertNotNull(right, "and the right half onto its own")
        assertEquals(2, right.rotation)

        tick(6)
        assertEquals(Tile(2517, 3357), player.tile)
    }

    @Test
    fun `The training camp gate uses the iron door sound RuneScape-2011 gives it`() {
        val player = createPlayer(Tile(2517, 3356), name = "training gate listener")
        player["biohazard"] = "completed"
        val gate = GameObjects.find(Tile(2517, 3356), "lathas_training_gate_left_closed")
        mockkObject(ZoneBatchUpdates)
        try {
            player.objectOption(gate, "Open")
            tick(2)
            player.skipDialogues()
            tick(8)

            val updates = mutableListOf<ZoneUpdate>()
            verify { ZoneBatchUpdates.add(any<Zone>(), capture(updates)) }
            val sounds = updates.filterIsInstance<SoundAddition>()
            assertEquals(listOf(70), sounds.map { it.id })
        } finally {
            unmockkObject(ZoneBatchUpdates)
        }
    }

    @Test
    fun `The Ardougne wall doors open once the quest is done`() {
        val player = createPlayer(Tile(2558, 3300))
        val doors = GameObjects.find(Tile(2558, 3300), "ardougne_wall_door_closed")

        player.objectOption(doors, "Open")
        tick(4)
        assertTrue(player.containsMessage("...But they will not open."))

        player["biohazard"] = "completed"
        player.objectOption(doors, "Open")
        tick(6)

        assertNotEquals(Tile(2558, 3300), player.tile, "the doors let the player through")
    }

    @Test
    fun `Elena's house is shut up while she is missing`() {
        val player = createPlayer(Tile(2592, 3339))
        val door = GameObjects.find(Tile(2592, 3339), "elena_house_door_closed")

        player.objectOption(door, "Open")
        tick(4)
        assertTrue(player.containsMessage("The door is locked."))
        assertEquals(Tile(2592, 3339), player.tile)

        player["plague_city"] = "completed"
        player.objectOption(door, "Open")
        tick(6)

        assertEquals(Tile(2592, 3338), player.tile)
    }

    @Test
    fun `The journal reads through every stage of the quest`() {
        val player = createPlayer(Tile(2592, 3335))

        for (stage in JOURNAL_STAGES) {
            player["biohazard"] = stage
            InterfaceApi.openQuestJournal(player, "biohazard")
            assertTrue(player.interfaces.contains("quest_scroll"), "the journal should open at $stage")
            player.interfaces.close("quest_scroll")
        }
    }

    private fun playThrough(player: Player, ticks: Int) {
        repeat(ticks) {
            tick()
            if (player.dialogue != null) {
                player.skipDialogues()
            }
        }
    }

    private fun talkTo(player: Player, npc: NPC, vararg options: String) {
        player.npcOption(npc, "Talk-to")
        playThrough(player, 8)
        for (option in options) {
            player.dialogueOption(option)
            playThrough(player, 8)
        }
    }

    private fun keysOn(tile: Tile) = FloorItems.at(tile.zone).flatten().count { it.id == "key_biohazard" }

    private fun moveTo(player: Player, tile: Tile) {
        player.mode = EmptyMode
        player.tele(tile)
        tick()
    }

    private companion object {
        const val DEATH_TICKS = 20

        val VIALS = listOf("ethenea", "liquid_honey", "sulphuric_broline")
        val CHEMICALS = VIALS + "plague_sample"

        val JOURNAL_STAGES = listOf(
            "unstarted",
            "started",
            "spoke_to_jerico",
            "birdfeed_thrown",
            "pigeons_released",
            "crossed_wall",
            "poisoned_stew",
            "found_distillator",
            "collect_chemicals",
            "smuggle_chemicals",
            "sample_tested",
            "told_elena",
            "completed",
        )
    }
}
