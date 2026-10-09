package content.quest.member.tree_gnome_village

import WorldTest
import content.entity.combat.Combat
import content.entity.combat.damageDealers
import content.entity.combat.target
import content.entity.player.bank.bank
import dialogueContinue
import dialogueOption
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.mockk.verifyOrder
import messages
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunt
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAnimation
import world.gregs.voidps.network.login.protocol.encode.zone.ProjectileAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ZoneUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TreeGnomeVillageTest : WorldTest() {

    @Test
    fun `Stronghold door lets players walk out and closes before an outside entry attempt`() {
        val player = createPlayer(Tile(2502, 3252))
        val door = GameObjects.find(Tile(2502, 3250), "door_39_closed")
        player.objectOption(door, "Open")
        tick(10)
        assertEquals(Tile(2502, 3250), player.tile)
        val closed = GameObjects.find(Tile(2502, 3250), "door_39_closed")
        player.objectOption(closed, "Open")
        tick(3)
        player.skipDialogues()
        assertEquals(Tile(2502, 3250), player.tile)
        assertEquals("door_39_closed", GameObjects.find(Tile(2502, 3250), "door_39_closed").id)
    }

    @Test
    fun `Quest starts after the player agrees before Bolren explains the battlefield`() {
        val location = Tile(2542, 3168)
        val player = createPlayer(location)
        val king = createNPC("king_bolren_tree_gnome_village", Tile(2542, 3169))
        player.npcOption(king, "Talk-to")
        tick(3)
        player.dialogueContinue(5)
        player.dialogueOption("line1")
        player.dialogueContinue(9) // Player's question, seven replies, and low-combat warning.
        player.dialogueOption("line1")
        assertEquals("unstarted", player["tree_gnome_village", "unstarted"])
        player.dialogueContinue()
        assertEquals("started", player["tree_gnome_village", "unstarted"])
        assertEquals(location, player.tile)
        player.dialogueContinue(3)
        assertEquals(Tile(2504, 3192), player.tile)
        player.dialogueContinue()
        assertEquals(null, player.dialogue)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Bolren replaces a lost amulet but does not duplicate a banked reward`(banked: Boolean) {
        val player = createPlayer(Tile(2542, 3168))
        val king = createNPC("king_bolren_tree_gnome_village", Tile(2542, 3169))
        player["tree_gnome_village"] = "completed"
        if (banked) player.bank.add("gnome_amulet")
        repeat(2) {
            player.npcOption(king, "Talk-to")
            tick(3)
            player.skipDialogues()
            assertEquals(null, player.dialogue)
            assertEquals(if (banked) 0 else 1, player.inventory.count("gnome_amulet"))
        }
    }

    @Test
    fun `Warlord complaint survives losing pursuit outside his movement boundary`() {
        val player = createPlayer(Tile(2457, 3314))
        val warlord = createNPC("khazard_warlord_underground_pass", Tile(2457, 3302))
        player["tree_gnome_village"] = "hunt_warlord"
        warlord["warlord_challenger"] = player.index
        Combat.combat(warlord, player)
        mockkObject(warlord)
        try {
            tick(49)
            assertFalse(warlord.mode is CombatMovement)
            verify(exactly = 0) { warlord.say("Bah, enough of you!") }
            tick()
            verify(exactly = 1) { warlord.say("Bah, enough of you!") }
            tick(20)
            verify(exactly = 1) { warlord.say("Bah, enough of you!") }
        } finally {
            unmockkObject(warlord)
        }
    }

    @ParameterizedTest
    @CsvSource("khazard_commander_battlefield,2509,3254,0", "khazard_commander_battlefield,2503,3255,1", "khazard_warlord_underground_pass,2457,3302,0")
    fun `Stronghold commanders and warlord respawn after death`(id: String, x: Int, y: Int, level: Int) {
        val location = Tile(x, y, level)
        val player = createPlayer(location.addY(1))
        val npc = createNPC(id, location)
        npc["warlord_challenger"] = player.index
        npc.damageDealers[player] = 100
        npc.levels.set(Skill.Constitution, 0)
        tick(20)
        assertTrue(npc.hide)
        if (id == "khazard_commander_battlefield") {
            assertTrue(FloorItems.firstOrNull(location, "bones") != null)
            assertEquals(null, FloorItems.firstOrNull(location, "orb_of_protection"))
        }
        tick(50)
        assertTrue(npc.index >= 0)
        assertFalse(npc.hide)
        assertFalse(npc.dead)
        assertEquals(level, npc.tile.level)
        if (id == "khazard_warlord_underground_pass") {
            assertEquals(-1, npc["warlord_challenger", -1])
            assertEquals(null, npc.target)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["unstarted", "orb_recovered", "hunt_warlord", "warlord_defeated"])
    fun `Warlord conversation follows the first orb and defeat milestones`(stage: String) {
        val player = createPlayer(Tile(2458, 3302))
        val warlord = createNPC("khazard_warlord_underground_pass", Tile(2457, 3302))
        player["tree_gnome_village"] = stage
        player.npcOption(warlord, "Talk-to")
        tick(3)
        assertEquals(null, warlord.target)
        player.skipDialogues()
        assertEquals(null, player.dialogue)
        assertEquals(if (stage in setOf("hunt_warlord", "warlord_defeated")) player else null, warlord.target)
    }

    @Test
    fun `Warlord hunting requires a completed challenge and stops after recovering the orbs`() {
        val player = createPlayer(Tile(2458, 3302))
        val warlord = createNPC("khazard_warlord_underground_pass", Tile(2457, 3302))
        player["tree_gnome_village"] = "hunt_warlord"
        Hunt.hunt(warlord, player, "tree_gnome_village_warlord")
        assertEquals(null, warlord.target)
        player.npcOption(warlord, "Talk-to")
        tick(3)
        player.dialogueContinue(5)
        assertEquals(null, warlord.target)
        player.dialogueContinue()
        assertEquals(player, warlord.target)
        warlord.target = null
        Hunt.hunt(warlord, player, "tree_gnome_village_warlord")
        assertEquals(player, warlord.target)
        warlord.target = null
        player["tree_gnome_village"] = "orbs_recovered"
        Hunt.hunt(warlord, player, "tree_gnome_village_warlord")
        assertEquals(null, warlord.target)
        val respawned = createNPC("khazard_warlord_underground_pass", Tile(2457, 3303))
        player["tree_gnome_village"] = "warlord_defeated"
        Hunt.hunt(respawned, player, "tree_gnome_village_warlord")
        assertEquals(null, respawned.target)
        player.npcOption(respawned, "Talk-to")
        tick(3)
        player.dialogueContinue(2)
        assertEquals(player, respawned.target)
    }

    @Test
    fun `Blocked warlord aggression complains once without a quest conversation`() {
        val player = createPlayer(Tile(2457, 3307))
        val warlord = createNPC("khazard_warlord_underground_pass", Tile(2457, 3302))
        warlord.start("movement_delay", 1000)
        Combat.combat(warlord, player)
        mockkObject(warlord)
        try {
            tick(49)
            verify(exactly = 0) { warlord.say("Bah, enough of you!") }
            tick()
            verify(exactly = 1) { warlord.say("Bah, enough of you!") }
            tick(20)
            verify(exactly = 1) { warlord.say("Bah, enough of you!") }
        } finally {
            unmockkObject(warlord)
        }
    }

    @Test
    fun `Attacking the warlord resets the wait for his blocked aggression complaint`() {
        val player = createPlayer(Tile(2457, 3307))
        val warlord = createNPC("khazard_warlord_underground_pass", Tile(2457, 3302))
        warlord.start("movement_delay", 1000)
        Combat.combat(warlord, player)
        mockkObject(warlord)
        try {
            tick(49)
            warlord.start("under_attack", 200)
            tick(60)
            verify(exactly = 0) { warlord.say("Bah, enough of you!") }
            warlord.stop("under_attack")
            tick(49)
            verify(exactly = 0) { warlord.say("Bah, enough of you!") }
            tick()
            verify(exactly = 1) { warlord.say("Bah, enough of you!") }
        } finally {
            unmockkObject(warlord)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Unstarted Elkoy introductions end without an escort choice`(inside: Boolean) {
        val tile = if (inside) Tile(2514, 3160) else Tile(2504, 3192)
        val player = createPlayer(tile)
        val elkoy = createNPC(if (inside) "elkoy_tree_gnome_village_2" else "elkoy_tree_gnome_village", tile.addY(-1))
        player.npcOption(elkoy, "Talk-to")
        tick(3)
        player.dialogueContinue(8)
        assertEquals(null, player.dialogue)
        assertEquals(tile, player.tile)
        assertEquals("unstarted", player["tree_gnome_village", "unstarted"])
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Completed Elkoy dialogue escorts after continuing the chosen player line`(inside: Boolean) {
        val tile = if (inside) Tile(2514, 3160) else Tile(2504, 3192)
        val player = createPlayer(tile)
        player["tree_gnome_village"] = "completed"
        val elkoy = createNPC(if (inside) "elkoy_tree_gnome_village_2" else "elkoy_tree_gnome_village", tile.addY(-1))
        player.npcOption(elkoy, "Talk-to")
        tick(3)
        player.dialogueContinue(2)
        player.dialogueOption("line1")
        assertEquals(tile, player.tile)
        player.dialogueContinue()
        assertEquals(if (inside) Tile(2504, 3192) else Tile(2514, 3160), player.tile)
        player.dialogueContinue()
        assertEquals(null, player.dialogue)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Completed Elkoy refusal stays at the chosen maze endpoint`(inside: Boolean) {
        val tile = if (inside) Tile(2514, 3160) else Tile(2504, 3192)
        val player = createPlayer(tile)
        player["tree_gnome_village"] = "completed"
        val elkoy = createNPC(if (inside) "elkoy_tree_gnome_village_2" else "elkoy_tree_gnome_village", tile.addY(-1))
        player.npcOption(elkoy, "Talk-to")
        tick(3)
        player.dialogueContinue(2)
        player.dialogueOption("line2")
        player.dialogueContinue(if (inside) 1 else 2)
        assertEquals(null, player.dialogue)
        assertEquals(tile, player.tile)
    }

    @Test
    fun `Ballista strings animate while the spear message is open without a projectile`() {
        val player = createPlayer(Tile(2508, 3209))
        player["tree_gnome_village"] = "coordinates_obtained"
        player["tree_gnome_village_trackers"] = 7
        player["tree_gnome_village_coordinate"] = 1
        val ballista = GameObjects.find(Tile(2508, 3210), "tree_gnome_village_ballista")
        mockkObject(ZoneBatchUpdates)
        try {
            player.objectOption(ballista, "Fire")
            tick(3)
            player.dialogueContinue()
            player.dialogueOption("line1")
            player.dialogueContinue()
            assertTrue(player.dialogue?.startsWith("dialogue_message") == true)
            val updates = mutableListOf<ZoneUpdate>()
            verify { ZoneBatchUpdates.add(any<Zone>(), capture(updates)) }
            assertEquals(listOf(730), updates.filterIsInstance<ObjectAnimation>().map { it.id })
            assertTrue(updates.filterIsInstance<ProjectileAddition>().isEmpty())
            player.skipDialogues()
        } finally {
            unmockkObject(ZoneBatchUpdates)
        }
    }

    @Test
    fun `Tracker two can give his clue through the bars without walking beside him`() {
        val tile = Tile(2524, 3255)
        val player = createPlayer(tile)
        val tracker = createNPC("tracker_gnome_2_battlefield", Tile(2524, 3257))
        player["tree_gnome_village"] = "logs_delivered"
        player["tree_gnome_village_briefed"] = true
        assertFalse(PlayerOnNPCInteract(tracker, "Attack", player).arrived())
        player.npcOption(tracker, "Talk-to")
        tick(3)
        player.skipDialogues()
        assertEquals(tile, player.tile)
        assertEquals(2, player["tree_gnome_village_trackers", 0])
    }

    @Test
    fun `Opening the upstairs chest starts combat without blocking the orb search`() {
        val player = createPlayer(Tile(2506, 3258, 1))
        player["tree_gnome_village"] = "stronghold_open"
        val commander = createNPC("khazard_commander_battlefield", Tile(2503, 3255, 1))
        player.objectOption(GameObjects.find(Tile(2506, 3259, 1), "tree_gnome_village_chest"), "Open")
        tick(3)
        assertEquals(player, commander.target)
        player.objectOption(GameObjects.find(Tile(2506, 3259, 1), "tree_gnome_village_chest_open"), "Search")
        tick(3)
        assertFalse(commander.dead)
        assertEquals(1, player.inventory.count("orb_of_protection"))
        assertEquals("orb_recovered", player["tree_gnome_village", ""])
    }

    @Test
    fun `Ceremony ends before a separate conversation awards the quest reward`() {
        val player = createPlayer(Tile(2542, 3168))
        val king = createNPC("king_bolren_tree_gnome_village", Tile(2542, 3169))
        val north = createNPC("local_gnome_tree_gnome_village", Tile(2541, 3171))
        val south = createNPC("local_gnome_tree_gnome_village", Tile(2541, 3168))
        player["tree_gnome_village"] = "orbs_recovered"
        player.inventory.add("orbs_of_protection")
        mockkObject(north, south, king)
        try {
            player.npcOption(king, "Talk-to")
            tick(3)
            player.skipDialogues()
            tick(20)
            player.skipDialogues()
            verifyOrder {
                north.say("Su tana.")
                south.say("En tania.")
                north.say("Su tana.")
                south.say("En tania.")
                king.anim("tree_gnome_village_cast_orbs", delay = 20)
                king.gfx("tree_gnome_village_orbs", delay = 20, height = 124)
            }
            verify(exactly = 2) { north.anim("tree_gnome_village_chant") }
            verify(exactly = 2) { south.anim("tree_gnome_village_chant") }
            assertEquals(2, player["tree_gnome_village_ceremony", 0])
            assertEquals("orbs_recovered", player["tree_gnome_village", ""])
            assertEquals(0, player.inventory.count("gnome_amulet"))
            assertTrue(player.treeGnomeVillageJournal().joinToString(" ").contains("hopefully he will reward me"))
            player.npcOption(king, "Talk-to")
            tick(3)
            player.skipDialogues()
            assertEquals("completed", player["tree_gnome_village", ""])
            assertEquals(1, player.inventory.count("gnome_amulet"))
        } finally {
            unmockkObject(north, south, king)
        }
    }

    @Test
    fun `Progression consumes quest items and awards completion only once`() {
        val player = createPlayer()
        val progress = TreeGnomeVillageProgress
        assertFalse(progress.complete(player))
        assertTrue(progress.start(player))
        val coordinate = player["tree_gnome_village_coordinate", 0]
        assertTrue(coordinate in 1..4)
        assertFalse(progress.start(player))
        assertEquals(coordinate, player["tree_gnome_village_coordinate", 0])

        player.inventory.add("logs", 5)
        assertFalse(progress.deliverLogs(player))
        assertEquals(5, player.inventory.count("logs"))
        player.inventory.add("logs")
        player["tree_gnome_village_montai_met"] = true
        assertTrue(progress.deliverLogs(player))
        assertEquals(0, player.inventory.count("logs"))
        assertFalse(progress.deliverLogs(player))

        assertFalse(progress.recordTracker(player, 3))
        progress.brief(player)
        progress.recordTracker(player, 3)
        progress.recordTracker(player, 3)
        progress.recordTracker(player, 1)
        assertFalse(progress.fire(player, coordinate))
        progress.recordTracker(player, 2)
        assertFalse(progress.fire(player, 0))
        assertTrue(progress.fire(player, coordinate))
        assertFalse(progress.fire(player, coordinate))
        assertTrue(progress.recoverOrb(player))
        assertFalse(progress.recoverOrb(player))
        assertTrue(progress.returnOrb(player))
        assertFalse(progress.returnOrb(player))
        assertFalse(progress.recoverOrb(player))
        assertTrue(progress.defeatWarlord(player))
        assertFalse(progress.defeatWarlord(player))
        val experience = player.experience.get(Skill.Attack)
        val points = player["quest_points", 0]
        assertFalse(progress.complete(player))
        assertTrue(progress.beginCeremony(player))
        assertTrue(progress.beginCeremony(player)) // Resume without consuming a second set of orbs.
        assertFalse(progress.defeatWarlord(player))
        assertFalse(progress.complete(player))
        progress.finishCeremony(player)
        assertTrue(progress.complete(player))
        assertFalse(progress.complete(player))
        assertEquals("completed", player["tree_gnome_village", "unstarted"])
        assertEquals(points + 2, player["quest_points", 0])
        assertEquals(experience + 11450.0, player.experience.get(Skill.Attack))
        assertEquals(1, player.inventory.count("gnome_amulet"))
        assertEquals(0, player.inventory.count("orbs_of_protection"))
        assertFalse(progress.defeatWarlord(player))
    }

    @Test
    fun `Chest rejects early access duplicates and full inventories but replaces a lost orb`() {
        val player = createPlayer(Tile(2506, 3258, 1))
        val closed = GameObjects.find(Tile(2506, 3259, 1), "tree_gnome_village_chest")
        player.objectOption(closed, "Open")
        tick(3)
        assertEquals(0, player.inventory.count("orb_of_protection"))
        player["tree_gnome_village"] = "stronghold_open"
        player.objectOption(closed, "Open")
        tick(3)
        val chest = GameObjects.find(Tile(2506, 3259, 1), "tree_gnome_village_chest_open")
        player.inventory.add("logs", 28)
        player.objectOption(chest, "Search")
        tick(3)
        assertEquals("stronghold_open", player["tree_gnome_village", ""])
        player.inventory.clear()
        player.objectOption(chest, "Search")
        tick(3)
        assertEquals(1, player.inventory.count("orb_of_protection"), player.messages.toString())
        player.inventory.remove("orb_of_protection")
        player.bank.add("orb_of_protection")
        assertFalse(TreeGnomeVillageProgress.recoverOrb(player))
        player.bank.clear()
        assertTrue(TreeGnomeVillageProgress.recoverOrb(player))
    }

    @Test
    fun `A warlord kill with a full inventory preserves recovery and cannot skip the first orb`() {
        val player = createPlayer()
        assertFalse(TreeGnomeVillageProgress.defeatWarlord(player))
        player["tree_gnome_village"] = "hunt_warlord"
        player.inventory.add("logs", 28)
        assertFalse(TreeGnomeVillageProgress.defeatWarlord(player))
        assertEquals("warlord_defeated", player["tree_gnome_village", ""])
        player.inventory.remove("logs")
        assertTrue(TreeGnomeVillageProgress.recoverOrbs(player))
        assertEquals("orbs_recovered", player["tree_gnome_village", ""])
        player.inventory.remove("orbs_of_protection")
        assertTrue(TreeGnomeVillageProgress.defeatWarlord(player))
    }

    @Test
    fun `Ballista success does not unlock another player's stronghold`() {
        val player = createPlayer(Tile(2509, 3252))
        val other = createPlayer()
        TreeGnomeVillageProgress.start(other)
        other["tree_gnome_village"] = "coordinates_obtained"
        other["tree_gnome_village_trackers"] = 7
        assertTrue(TreeGnomeVillageProgress.fire(other, other["tree_gnome_village_coordinate", 0]))
        val wall = GameObjects.find(Tile(2509, 3253), "tree_gnome_village_crumbled_wall")
        player.objectOption(wall, "Climb-over")
        tick(5)
        assertEquals(Tile(2509, 3252), player.tile)
    }

    @Test
    fun `Firing the actual ballista rejects a miss and accepts the stored coordinate`() {
        val player = createPlayer(Tile(2508, 3209))
        TreeGnomeVillageProgress.start(player)
        player["tree_gnome_village"] = "coordinates_obtained"
        player["tree_gnome_village_trackers"] = 7
        val ballista = GameObjects.find(Tile(2508, 3210), "tree_gnome_village_ballista")
        player.objectOption(ballista, "Fire")
        tick(3)
        player.dialogueContinue()
        val wrong = player["tree_gnome_village_coordinate", 0] % 4 + 1
        player.dialogueOption("line$wrong")
        player.skipDialogues()
        tick()
        assertEquals("coordinates_obtained", player["tree_gnome_village", ""])
        player.objectOption(ballista, "Fire")
        tick(3)
        player.dialogueContinue()
        player.dialogueOption("line${player["tree_gnome_village_coordinate", 0]}")
        player.skipDialogues()
        tick()
        assertEquals("stronghold_open", player["tree_gnome_village", ""])
    }

    @Test
    fun `Started players can follow Elkoy from both maze endpoints`() {
        val player = createPlayer(Tile(2504, 3192))
        TreeGnomeVillageProgress.start(player)
        val outside = createNPC("elkoy_tree_gnome_village", Tile(2504, 3191))
        player.npcOption(outside, "Follow")
        tick(3)
        assertTrue(player.interfaces.contains("dialogue_message_np1"))
        tick(8)
        assertEquals(Tile(2514, 3160), player.tile)
        assertFalse(player.interfaces.contains("dialogue_message_np1"))
        val inside = createNPC("elkoy_tree_gnome_village_2", Tile(2514, 3159))
        player.npcOption(inside, "Follow")
        tick(11)
        assertEquals(Tile(2504, 3192), player.tile)
        assertFalse(player.interfaces.contains("dialogue_message_np1"))
    }
}
