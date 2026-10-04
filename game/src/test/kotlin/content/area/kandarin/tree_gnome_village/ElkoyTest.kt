package content.area.kandarin.tree_gnome_village

import WorldTest
import content.entity.player.command.TeleportCommands
import dialogueOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElkoyTest : WorldTest() {
    override var loadNpcs = true

    @Test
    fun `Inner Elkoy talks before village quest`() {
        val player = createPlayer(Tile(2515, 3159))
        player.npcOption(NPCs.find(player.tile.regionLevel, "elkoy_tree_gnome_village_2"), "Talk-to")
        tick(2)
        player.skipDialogues()
    }

    @Test
    fun `Loose railing allows passage in both directions`() {
        val player = createPlayer(Tile(2515, 3159))
        val railing = GameObjects.find(Tile(2515, 3161), "tree_gnome_village_loose_railing")
        for (start in listOf(Tile(2515, 3159), Tile(2515, 3160), Tile(2515, 3161), Tile(2515, 3162), Tile(2515, 3163))) {
            player.tele(start)
            player.objectOption(railing, "Squeeze-through")
            tick(12)
            assertEquals(if (start.y < 3161) Tile(2515, 3161) else Tile(2515, 3160), player.tile, "Starting at $start")
        }
    }

    @Test
    fun `Elkoy talks before and during Waterfall without granting maze travel`() {
        val player = createPlayer(Tile(2504, 3190))
        val elkoy = NPCs.find(player.tile.regionLevel, "elkoy_tree_gnome_village")
        for (stage in listOf("unstarted", "read_book")) {
            player["waterfall_quest"] = stage
            player.npcOption(elkoy, "Talk-to")
            tick(2)
            player.skipDialogues()
            assertEquals("unstarted", player["tree_gnome_village", "unstarted"])
            assertEquals(stage, player["waterfall_quest", "unstarted"])
        }
    }

    @Test
    fun `Elkoy guides through maze in both directions after village quest`() {
        val player = createPlayer(Tile(2504, 3190))
        player["tree_gnome_village"] = "completed"
        player.npcOption(NPCs.find(player.tile.regionLevel, "elkoy_tree_gnome_village"), "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals(Tile(2515, 3160), player.tile)
        player.npcOption(NPCs.find(player.tile.regionLevel, "elkoy_tree_gnome_village_2"), "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals(Tile(2504, 3190), player.tile)
    }

    @Test
    fun `Follow appears after starting village quest and guides in both directions`() {
        val player = createPlayer(Tile(2504, 3190))
        val outer = NPCs.find(player.tile.regionLevel, "elkoy_tree_gnome_village")
        val inner = NPCs.find(player.tile.regionLevel, "elkoy_tree_gnome_village_2")
        assertFalse(outer.def(player).options.contains("Follow"))
        assertFalse(inner.def(player).options.contains("Follow"))
        for (stage in listOf("started", "completed")) {
            player["tree_gnome_village"] = stage
            assertTrue(outer.def(player).options.contains("Follow"))
            assertTrue(inner.def(player).options.contains("Follow"))
            player.npcOption(outer, "Follow")
            tick(2)
            player.skipDialogues()
            assertEquals(Tile(2515, 3160), player.tile)
            player.npcOption(inner, "Follow")
            tick(2)
            player.skipDialogues()
            assertEquals(Tile(2504, 3190), player.tile)
        }
    }

    @Test
    fun `Teleport village name and aliases`() {
        val player = createPlayer()
        val commands = scripts.filterIsInstance<TeleportCommands>().single()
        for (name in listOf("tree_gnome_village", "gnome_village", "tgv")) {
            commands.area(player, listOf(name))
            assertEquals(Tile(2542, 3168), player.tile)
        }
    }
}
