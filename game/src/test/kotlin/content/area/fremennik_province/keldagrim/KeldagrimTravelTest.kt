package content.area.fremennik_province.keldagrim

import WorldTest
import dialogueOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class KeldagrimTravelTest : WorldTest() {

    private fun skipMissing() {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = "true"
        Settings.load(properties)
    }

    @Test
    fun `Walk through the tunnel from Rellekka`() {
        val player = createPlayer(Tile(2730, 3713))
        val tunnel = GameObjects.find(Tile(2731, 3712), "keldagrim_tunnel_rellekka")

        player.objectOption(tunnel, "Enter")
        tick(3)

        assertEquals(Tile(2773, 10162), player.tile)
    }

    @Test
    fun `Boatman takes players to Keldagrim and unlocks the carts`() {
        val player = createPlayer(Tile(2841, 10129))
        val boatman = createNPC("dwarven_boatman_keldagrim_2", Tile(2842, 10129))

        player.npcOption(boatman, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()
        tick(6)

        assertEquals(Tile(2892, 10225), player.tile)
        assertTrue(player["keldagrim_visited", false])
    }

    @Test
    fun `Ferryman crosses the river for 2 coins`() {
        val player = createPlayer(Tile(2839, 10129))
        player.inventory.add("coins", 2)
        val ferryman = createNPC("dwarven_ferryman_keldagrim", Tile(2839, 10128))

        player.npcOption(ferryman, "Cross-river")
        tick(2)

        assertEquals(Tile(2836, 10143), player.tile)
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Ride the cart from Ice Mountain to Keldagrim and on to the Grand Exchange`() {
        skipMissing()
        val player = createPlayer(Tile(2996, 9837))
        player["running"] = true
        val cart = GameObjects.find(Tile(2995, 9836), "train_cart_dwarven_mine")

        player.objectOption(cart, "Ride")
        tick(2)
        player.dialogueOption(1)
        tick(40)
        assertEquals(Tile(2909, 10174), player.tile)

        val keldagrimCart = createObject("train_cart_keldagrim", Tile(2916, 10175))
        player.objectOption(keldagrimCart, "Ride")
        tick(10)
        player.dialogueOption(1)
        tick(40)
        assertEquals(Tile(3140, 3507), player.tile)
    }

    @Test
    fun `Enter the chaos dwarf battlefield and leave again`() {
        val player = createPlayer(Tile(2817, 10155))
        val tunnel = createObject("chaos_dwarf_battlefield_tunnel", Tile(2816, 10155), rotation = 2)

        player.objectOption(tunnel, "Enter")
        tick(3)
        assertEquals(Tile(1520, 4704), player.tile)

        val doorway = GameObjects.find(Tile(1521, 4704), "chaos_dwarf_battlefield_doorway")
        player.objectOption(doorway, "Enter")
        tick(3)
        assertEquals(Tile(2817, 10155), player.tile)
    }
}
