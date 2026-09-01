package content.quest.member.hand_in_the_sand

import WorldTest
import containsMessage
import dialogueOption
import itemOnItem
import itemOnNpc
import itemOnObject
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HandInTheSandTest : WorldTest() {
    @Test
    fun `Investigate the hand and brew the truth serum`() {
        val player = createPlayer(Tile(2550, 3101))
        player.experience.set(Skill.Thieving, Level.experience(20))
        player.experience.set(Skill.Crafting, Level.experience(50))

        val bert = createNPC("bert", Tile(2550, 3100))
        player.npcOption(bert, "Talk-to")
        chat(player)
        player.dialogueOption("line2")
        chat(player)
        player.dialogueOption("line2")
        chat(player)
        assertEquals("investigate_hand", player.stage)
        assertTrue(player.inventory.contains("sandy_hand"))

        player.moveTo(2551, 3079)
        player.inventory.add("beer")
        val captain = createNPC("guard_captain", Tile(2551, 3078))
        player.npcOption(captain, "Talk-to")
        chat(player)
        assertEquals("ask_wizards", player.stage)
        assertFalse(player.inventory.contains("sandy_hand"))
        assertTrue(player.inventory.contains("beer_hand"))

        player.moveTo(2597, 3087)
        val zavistic = createNPC("zavistic_rarve", Tile(2598, 3087))
        player.itemOnNpc(zavistic, player.inventory.indexOf("beer_hand"))
        chat(player)
        assertEquals("bert_hours", player.stage)

        player.moveTo(2550, 3101)
        player.npcOption(bert, "Talk-to")
        chat(player)
        assertEquals("visit_sandy", player.stage)
        assertTrue(player.inventory.contains("berts_rota"))

        player.moveTo(2788, 3177)
        val desk = createObject("handsand_desk", Tile(2789, 3177))
        player.objectOption(desk, "Search")
        chat(player)
        assertEquals("confront_bert", player.stage)
        assertTrue(player.inventory.contains("sandys_rota"))

        player.moveTo(2550, 3101)
        player.npcOption(bert, "Talk-to")
        chat(player)
        assertEquals("deliver_scroll", player.stage)
        assertTrue(player.inventory.contains("magic_scroll"))

        player.moveTo(2597, 3087)
        player.itemOnNpc(zavistic, player.inventory.indexOf("magic_scroll"))
        chat(player)
        assertEquals("make_serum", player.stage)
        assertTrue(player.inventory.contains("magical_orb"))

        player.moveTo(3016, 3259)
        val betty = createNPC("betty_port_sarim", Tile(3016, 3260))
        player.inventory.add("vial")
        player.npcOption(betty, "Talk-to")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        assertEquals(1, player["handsand_serum", 0])
        assertTrue(player.inventory.contains("bottled_water"))

        player.inventory.add("redberries")
        player.itemOnItem("bottled_water", "redberries")
        tick(2)
        assertEquals(2, player["handsand_serum", 0])

        player.inventory.add("white_berries")
        player.itemOnItem("redberry_juice", "white_berries")
        tick(2)
        assertEquals(3, player["handsand_serum", 0])

        player.inventory.add("lantern_lens")
        player.itemOnItem("pink_dye", "lantern_lens")
        chat(player)
        assertEquals(4, player["handsand_serum", 0])
        assertTrue(player.inventory.contains("rose_tinted_lens"))

        player.npcOption(betty, "Talk-to")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        chat(player)
        assertTrue(player["handsand_counter_multi", false])

        openBettysDoor(player)
        player.moveTo(3016, 3259)
        val counter = createObject("handsand_counter", Tile(3016, 3258))
        player.itemOnObject(counter, player.inventory.indexOf("rose_tinted_lens"))
        chat(player)
        assertEquals(5, player["handsand_serum", 0])
        assertTrue(player.inventory.contains("truth_serum"))

        player.inventory.add("sand")
        player.npcOption(betty, "Talk-to")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        assertEquals(6, player["handsand_serum", 0])
        assertEquals("distract_sandy", player.stage)
        assertFalse(player.inventory.contains("sand"))
    }

    @Test
    fun `Interrogate Sandy and lay Clarence to rest`() {
        val player = createPlayer(Tile(2788, 3177))
        player.experience.set(Skill.Thieving, Level.experience(20))
        player.experience.set(Skill.Crafting, Level.experience(50))
        val craftingStart = player.experience.get(Skill.Crafting)
        val thievingStart = player.experience.get(Skill.Thieving)
        player["hand_in_the_sand"] = "distract_sandy"
        player["handsand_serum"] = 6
        player["handsand_question1"] = true
        player["handsand_question2"] = true
        player["handsand_question3"] = true
        player.inventory.add("truth_serum")
        player.inventory.add("magical_orb")

        val sandy = createNPC("sandy_brimhaven_3", Tile(2788, 3176))

        player.npcOption(sandy, "Talk-to")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        assertEquals("drug_coffee", player.stage)
        assertEquals(1, player["handsand_sandy_multi", 0])

        val coffee = createObject("handsand_coffee", Tile(2789, 3177))
        player.itemOnObject(coffee, player.inventory.indexOf("truth_serum"))
        chat(player)
        assertEquals("activate_orb", player.stage)
        assertFalse(player.inventory.contains("truth_serum"))

        player.itemOption("Activate", "magical_orb")
        chat(player)
        assertEquals("interrogate_sandy", player.stage)
        assertTrue(player.inventory.contains("magical_orb_active"))

        player.npcOption(sandy, "Talk-to")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        assertEquals("return_orb", player.stage)
        assertFalse(player["handsand_question1", false])
        assertFalse(player["handsand_question2", false])
        assertFalse(player["handsand_question3", false])

        player.moveTo(2597, 3087)
        val zavistic = createNPC("zavistic_rarve", Tile(2598, 3087))
        player.npcOption(zavistic, "Talk-to")
        chat(player)
        assertEquals("gather_runes", player.stage)
        assertFalse(player.inventory.contains("magical_orb_active"))

        player.inventory.add("earth_rune", 5)
        player.inventory.add("bucket_of_sand")
        player.npcOption(zavistic, "Talk-to")
        chat(player)
        chat(player)
        chat(player)
        chat(player)
        assertEquals("search_entrana", player.stage)
        assertEquals(0, player.inventory.count("earth_rune"))
        assertFalse(player.inventory.contains("bucket_of_sand"))
        assertEquals(Tile(2597, 3087), player.tile)

        player.moveTo(2818, 3343)
        val mazion = createNPC("mazion", Tile(2818, 3342))
        player.npcOption(mazion, "Talk-to")
        chat(player)
        assertEquals("return_head", player.stage)
        assertTrue(player.inventory.contains("wizard_head"))

        player.moveTo(2597, 3087)
        player.npcOption(zavistic, "Talk-to")
        chat(player)
        assertEquals("completed", player.stage)
        assertEquals(1, player["quest_points", 0])
        assertEquals(9000.0, player.experience.get(Skill.Crafting) - craftingStart)
        assertEquals(1000.0, player.experience.get(Skill.Thieving) - thievingStart)
        assertFalse(player.containsMessage("Nothing interesting happens."))
    }

    @Test
    fun `Pickpocketing Sandy yields sand once the quest is started`() {
        val player = createPlayer(Tile(2788, 3177))
        player["hand_in_the_sand"] = "confront_bert"
        val sandy = createNPC("sandy_brimhaven_3", Tile(2788, 3176))

        player.npcOption(sandy, "Pickpocket")
        chat(player)

        assertTrue(player.inventory.contains("sand"))
    }

    @Test
    fun `Pickpocketing Sandy does nothing before the quest is started`() {
        val player = createPlayer(Tile(2788, 3177))
        val sandy = createNPC("sandy_brimhaven_3", Tile(2788, 3176))

        player.npcOption(sandy, "Pickpocket")
        tick(6)

        assertTrue(player.containsMessage("Nothing interesting happens."))
        assertFalse(player.inventory.contains("sand"))
    }

    @Test
    fun `Sandy's desk gives nothing before the rota is wanted`() {
        val player = createPlayer(Tile(2788, 3177))
        val desk = createObject("handsand_desk", Tile(2789, 3177))

        player.objectOption(desk, "Search")
        chat(player)

        assertFalse(player.inventory.contains("sandys_rota"))
        assertEquals("unstarted", player.stage)
    }

    @Test
    fun `Focusing the lens away from the doorway does nothing`() {
        val player = createPlayer(Tile(3016, 3260))
        player["hand_in_the_sand"] = "make_serum"
        player["handsand_serum"] = 4
        player["handsand_counter_multi"] = true
        player.inventory.add("rose_tinted_lens")
        openBettysDoor(player)
        player.moveTo(3016, 3260)
        val counter = createObject("handsand_counter", Tile(3016, 3261))

        player.itemOnObject(counter, player.inventory.indexOf("rose_tinted_lens"))
        tick(4)

        assertTrue(player.containsMessage("You need to be standing in the doorway."))
        assertTrue(player.inventory.contains("rose_tinted_lens"))
        assertEquals(4, player["handsand_serum", 0])
    }

    @Test
    fun `The handsand_cutscene command replays the cutscene from anywhere`() {
        val admin = createPlayer(Tile(3222, 3222), name = "handsandadmin")
        admin.rights = PlayerRights.Admin

        Script.launch { Commands.call(admin, "handsand_cutscene") }
        chat(admin)
        chat(admin)

        val bert = NPCs.findOrNull(admin.tile.regionLevel, "bert")
        assertNotNull(bert)
        tick(8)
        assertSame(PauseMode, bert.mode)

        chat(admin)
        chat(admin)

        assertEquals("search_entrana", admin.stage)
        assertEquals(Tile(3222, 3222), admin.tile)
        assertEquals(0, admin.inventory.count("earth_rune"))
        assertFalse(admin.inventory.contains("bucket_of_sand"))
    }

    @Test
    fun `The sandpit the cutscene animates is part of the map`() {
        val sandpit = GameObjects.getLayer(Tile(2542, 3103), ObjectLayer.GROUND)

        assertEquals("handsand_sandpit_anim", sandpit?.id)
        assertEquals(ObjectShape.CENTRE_PIECE_STRAIGHT, sandpit?.shape)
    }

    @Test
    fun `Bert only delivers sand once a day`() {
        val player = createPlayer(Tile(2550, 3101))
        player["hand_in_the_sand"] = "completed"
        player["handsand_employed_bert"] = true
        val bert = createNPC("bert", Tile(2550, 3100))

        player.npcOption(bert, "Talk-to")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        assertEquals(84, player.bankCount("bucket_of_sand"))

        player.npcOption(bert, "Talk-to")
        chat(player)
        player.dialogueOption("line1")
        chat(player)
        assertEquals(84, player.bankCount("bucket_of_sand"))
    }

    private fun chat(player: Player) {
        tickIf(60) { player.dialogue == null }
        player.skipDialogues()
    }

    private fun Player.moveTo(x: Int, y: Int) {
        tele(x, y)
        tick(3)
    }

    private fun openBettysDoor(player: Player) {
        val door = GameObjects.findOrNull(Tile(3017, 3259), "door_668_closed") ?: return
        player.objectOption(door, "Open")
        tick(2)
    }

    private val Player.stage: String
        get() = this["hand_in_the_sand", "unstarted"]

    private fun Player.bankCount(id: String) = inventories.inventory("bank").count(id)
}
