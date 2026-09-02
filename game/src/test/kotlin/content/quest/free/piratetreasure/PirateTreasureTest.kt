package content.quest.free.piratetreasure

import WorldTest
import containsMessage
import dialogueOption
import equipItem
import itemOnObject
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Walked in two halves because the quest crosses Port Sarim, Karamja, Varrock and Falador;
 * the second half starts from the stage the first one ends on.
 */
class PirateTreasureTest : WorldTest() {

    @Test
    fun `Start the quest and smuggle the rum off Karamja`() {
        val player = createPlayer(Tile(3049, 3254))

        // unstarted -> started: Frank wants a bottle of Karamja rum
        val frank = createNPC("redbeard_frank_port_sarim", Tile(3049, 3253))
        tick(2)
        player.npcOption(frank, "Talk-to")
        chat(player)
        player.dialogueOption("line1") // I'm in search of treasure.
        chat(player)
        player.dialogueOption("line1") // Ok, I will bring you some rum.
        chat(player)
        assertEquals("started", player.stage)

        // Take the plantation job so there's a crate heading for Wydin's
        player.moveTo(2941, 3151)
        val luthas = createNPC("luthas_musa_point", Tile(2940, 3151))
        tick(2)
        player.npcOption(luthas, "Talk-to")
        chat(player)
        player.dialogueOption("line1") // Could you offer me employment on your plantation?
        chat(player)
        assertTrue(player["banana_plantation_job", false])

        // Hide the rum in the crate, then bury it under bananas
        val crate = GameObjects.find(EXPORT_CRATE, "crate_14")
        player.inventory.add("karamjan_rum")
        player.itemOnObject(crate, player.inventory.indexOf("karamjan_rum"))
        tick(4)
        assertTrue(player["pirates_treasure_stashed_rum", false])
        assertFalse(player.inventory.contains("karamjan_rum"))

        player.inventory.add("banana", 10)
        player.objectOption(crate, "Fill")
        tick(4)
        assertEquals(10, player["banana_crate_bananas", 0])

        // Luthas ships the crate; the rum goes with it
        player.npcOption(luthas, "Talk-to")
        chat(player)
        assertTrue(player["pirates_treasure_delivered_rum", false])
        assertFalse(player["pirates_treasure_stashed_rum", false])
        assertEquals(30, player.inventory.count("coins"))

        // Wydin only lets employees into the back room, and only in a white apron
        player.moveTo(3015, 3204)
        val wydin = createNPC("wydin", Tile(3014, 3204))
        tick(2)
        player.inventory.add("white_apron")
        player.npcOption(wydin, "Talk-to")
        chat(player)
        player.dialogueOption("line4") // Can I get a job here?
        chat(player)
        assertTrue(player["pirates_treasure_wydin", false])

        // Retrieve the bottle from the delivered crate
        player.equipItem("white_apron", option = "Wear")
        tick(2)
        player.moveTo(3009, 3208)
        val bananaCrate = GameObjects.find(WYDIN_CRATE, "crate_13")
        player.objectOption(bananaCrate, "Search")
        chat(player)
        player.dialogueOption("line2") // No.
        tick(2)
        assertTrue(player.inventory.contains("karamjan_rum"))
        assertTrue(player["pirates_treasure_collected_rum", false])
        assertFalse(player["pirates_treasure_delivered_rum", false])
    }

    @Test
    fun `Trade the rum for Hector's treasure`() {
        val player = createPlayer(Tile(3049, 3254))
        player["pirates_treasure"] = "started"
        player["pirates_treasure_collected_rum"] = true
        player.inventory.add("karamjan_rum")

        // started -> chest_key: Frank swaps the rum for Hector's key
        val frank = createNPC("redbeard_frank_port_sarim", Tile(3049, 3253))
        tick(2)
        player.npcOption(frank, "Talk-to")
        chat(player)
        player.dialogueOption("line1") // Ok thanks, I'll go and get it.
        chat(player)
        assertEquals("chest_key", player.stage)
        assertFalse(player.inventory.contains("karamjan_rum"))
        assertTrue(player.inventory.contains("chest_key_pirates_treasure"))

        // The chest is locked without the key
        player.moveTo(3219, 3395, 1)
        val chest = GameObjects.find(BLUE_MOON_CHEST, "bluemoon_chest")
        player.objectOption(chest, "Open")
        tick(6)
        assertTrue(player.containsMessage("The chest is locked."))

        // chest_key -> read_message: the key gets you the note
        player.itemOnObject(chest, player.inventory.indexOf("chest_key_pirates_treasure"))
        tick(8)
        assertFalse(player.inventory.contains("chest_key_pirates_treasure"))
        assertTrue(player.inventory.contains("pirate_message"))

        player.itemOption("Read", "pirate_message")
        tick(2)
        assertEquals("read_message", player.stage)

        // Digging the flowers wakes the gardener before it yields anything
        player.moveTo(3000, 3383)
        player.inventory.add("spade")
        player.itemOption("Dig", "spade")
        tick(4)
        assertTrue(player["pirates_treasure_spawned_gardener", false])
        assertFalse(player.inventory.contains("casket_pirates_treasure"))

        // He blocks digging while he's around
        player.itemOption("Dig", "spade")
        tick(4)
        assertTrue(player.containsMessage("I can't dig up anything with him attacking me!"))

        // read_message -> completed: dig again once he's gone
        val gardener = NPCs.findOrNull(player.tile.regionLevel, "gardener_level_4")
        assertNotNull(gardener)
        NPCs.remove(gardener)
        tick(2)
        player.itemOption("Dig", "spade")
        tick(8)
        assertEquals("completed", player.stage)
        assertTrue(player.inventory.contains("casket_pirates_treasure"))
        assertEquals(2, player["quest_points", 0])

        // The casket holds Hector's treasure
        player.itemOption("Open", "casket_pirates_treasure")
        tick(2)
        assertFalse(player.inventory.contains("casket_pirates_treasure"))
        assertTrue(player.inventory.contains("gold_ring"))
        assertTrue(player.inventory.contains("emerald"))
        assertEquals(450, player.inventory.count("coins"))
    }

    @Test
    fun `Wydin's back room is closed to anyone who isn't working for him`() {
        val player = createPlayer(Tile(3013, 3204))
        createNPC("wydin", Tile(3014, 3204))
        tick(2)
        val door = GameObjects.find(SHOP_DOOR, "door_37_closed")

        player.objectOption(door, "Open")
        chat(player)
        tick(4)

        assertTrue(player.tile.x >= SHOP_DOOR.x, "should not have got past the door")
    }

    @Test
    fun `Wydin's employee still needs the apron on`() {
        val player = createPlayer(Tile(3013, 3204))
        player["pirates_treasure"] = "started"
        player["pirates_treasure_wydin"] = true
        player.inventory.add("white_apron")
        createNPC("wydin", Tile(3014, 3204))
        tick(2)
        val door = GameObjects.find(SHOP_DOOR, "door_37_closed")

        player.objectOption(door, "Open")
        chat(player)
        tick(4)
        assertTrue(player.tile.x >= SHOP_DOOR.x, "apron off: should not have got past the door")

        player.equipItem("white_apron", option = "Wear")
        tick(2)
        player.objectOption(GameObjects.find(SHOP_DOOR, "door_37_closed"), "Open")
        tick(8)
        assertTrue(player.tile.x < SHOP_DOOR.x, "apron on: should be through into the back room")
    }

    @Test
    fun `Reading the note opens the handwriting scroll`() {
        val player = createPlayer(Tile(3219, 3395, 1))
        player["pirates_treasure"] = "chest_key"
        player.inventory.add("pirate_message")

        player.itemOption("Read", "pirate_message")
        tick(2)

        assertTrue(player.hasOpen("message_scroll_handwriting"))
        assertEquals("message_scroll_handwriting", player.interfaces.get("main_screen"))
        assertEquals("read_message", player.stage)
    }

    @Test
    fun `Someone else's gardener doesn't block the dig`() {
        val player = createPlayer(Tile(3000, 3383))
        player["pirates_treasure"] = "read_message"
        player["pirates_treasure_spawned_gardener"] = true
        player.inventory.add("spade")
        // an ordinary gardener standing nearby isn't the one chasing this player
        createNPC("gardener_level_4", Tile(3001, 3383))
        tick(2)

        player.itemOption("Dig", "spade")
        tick(8)

        assertFalse(player.containsMessage("I can't dig up anything with him attacking me!"))
        assertEquals("completed", player.stage)
        assertTrue(player.inventory.contains("casket_pirates_treasure"))
    }

    @Test
    fun `The gardener the dig spawns attacks and blocks further digging`() {
        val player = createPlayer(Tile(3000, 3383))
        player["pirates_treasure"] = "read_message"
        player.inventory.add("spade")

        player.itemOption("Dig", "spade")
        tick(4)

        val gardener = NPCs.at(player.tile.regionLevel).firstOrNull { it.id == "gardener_level_4" }
        assertNotNull(gardener)
        assertEquals(player.accountName, gardener["owner", ""])
        assertTrue(player["pirates_treasure_spawned_gardener", false])

        player.itemOption("Dig", "spade")
        tick(4)
        assertTrue(player.containsMessage("I can't dig up anything with him attacking me!"))
        assertFalse(player.inventory.contains("casket_pirates_treasure"))
    }

    @Test
    fun `The reset command puts the quest back to unstarted`() {
        val admin = createPlayer(Tile(3000, 3383), name = "ptadmin")
        admin.rights = PlayerRights.Admin
        admin["pirates_treasure"] = "completed"
        admin["pirates_treasure_wydin"] = true
        admin["pirates_treasure_collected_rum"] = true
        admin["pirates_treasure_spawned_gardener"] = true
        admin["banana_plantation_job"] = true
        admin["banana_crate_bananas"] = 7
        admin["quest_points"] = 2
        admin.inventory.add("casket_pirates_treasure")
        admin.inventory.add("chest_key_pirates_treasure")
        admin.inventory.add("karamjan_rum")

        Script.launch { Commands.call(admin, "reset_pirates_treasure") }
        tick(2)

        assertEquals("unstarted", admin.stage)
        assertFalse(admin["pirates_treasure_wydin", false])
        assertFalse(admin["pirates_treasure_collected_rum", false])
        assertFalse(admin["pirates_treasure_spawned_gardener", false])
        assertFalse(admin["banana_plantation_job", false])
        assertEquals(0, admin["banana_crate_bananas", 0])
        assertEquals(0, admin["quest_points", 0])
        assertFalse(admin.inventory.contains("casket_pirates_treasure"))
        assertFalse(admin.inventory.contains("chest_key_pirates_treasure"))
        assertFalse(admin.inventory.contains("karamjan_rum"))
    }

    @Test
    fun `The customs officer confiscates rum at the gangplank`() {
        val player = createPlayer(Tile(2956, 3146))
        player["pirates_treasure"] = "started"
        val officer = createNPC("customs_officer_brimhaven", Tile(2956, 3145))
        tick(2)
        player.inventory.add("karamjan_rum")
        player.inventory.add("karamjan_rum")
        player.inventory.add("karamjan_rum")

        player.npcOption(officer, "Talk-to")
        chat(player)
        player.dialogueOption("line1") // Can I journey on this ship?
        chat(player)
        player.dialogueOption("line2") // Search away, I have nothing to hide.
        chat(player)

        assertEquals(0, player.inventory.count("karamjan_rum"))
        assertTrue(player.containsMessage("The customs officer confiscates your rum."))
    }

    @Test
    fun `Pay-Fare is locked until the quest is finished`() {
        val player = createPlayer(Tile(2956, 3146), name = "ptfare")
        val officer = createNPC("customs_officer_brimhaven", Tile(2956, 3145))
        tick(2)

        player.npcOption(officer, "Pay-Fare")
        tick(4)

        assertTrue(player.containsMessage("You may only use the Pay-fare option after completing Pirate's Treasure."))
    }

    @Test
    fun `Teleporting breaks the rum`() {
        val player = createPlayer(Tile(2956, 3146))
        player.inventory.add("karamjan_rum")
        player.inventory.add("karamjan_rum")

        Teleport.land(player, "modern")

        assertEquals(0, player.inventory.count("karamjan_rum"))
        assertTrue(player.containsMessage("Your Karamjan rum gets broken and spilled."))
    }

    @Test
    fun `Digging the flowers does nothing before the note is read`() {
        val player = createPlayer(Tile(3000, 3383))
        player.inventory.add("spade")

        player.itemOption("Dig", "spade")
        tick(4)

        assertTrue(player.containsMessage("It seems a shame to dig up these nice flowers for no reason."))
        assertFalse(player["pirates_treasure_spawned_gardener", false])
    }

    @Test
    fun `Frank replaces a lost chest key`() {
        val player = createPlayer(Tile(3049, 3254))
        player["pirates_treasure"] = "chest_key"
        val frank = createNPC("redbeard_frank_port_sarim", Tile(3049, 3253))
        tick(2)

        player.npcOption(frank, "Talk-to")
        chat(player)

        assertTrue(player.inventory.contains("chest_key_pirates_treasure"))
    }

    @Test
    fun `Frank refuses rum with a banana in it`() {
        val player = createPlayer(Tile(3049, 3254))
        player["pirates_treasure"] = "started"
        player.inventory.add("karamjan_rum_banana")
        val frank = createNPC("redbeard_frank_port_sarim", Tile(3049, 3253))
        tick(2)

        player.npcOption(frank, "Talk-to")
        chat(player) // greeting and "Yes, I've got some."
        chat(player) // Frank pauses, then complains about the banana
        tick(2)

        assertEquals("started", player.stage)
        assertTrue(player.inventory.contains("karamjan_rum_banana"))
        assertFalse(player.inventory.contains("chest_key_pirates_treasure"))
    }

    private fun chat(player: Player) {
        tickIf(60) { player.dialogue == null }
        player.skipDialogues()
    }

    private fun Player.moveTo(x: Int, y: Int, level: Int = 0) {
        tele(x, y, level)
        tick(3)
    }

    private val Player.stage: String
        get() = this["pirates_treasure", "unstarted"]

    private companion object {
        val EXPORT_CRATE = Tile(2943, 3151)
        val WYDIN_CRATE = Tile(3009, 3207)
        val BLUE_MOON_CHEST = Tile(3219, 3396, 1)
        val SHOP_DOOR = Tile(3012, 3204)
    }
}
