package content.area.fremennik_province.keldagrim

import WorldTest
import containsMessage
import content.entity.player.bank.bank
import dialogueContinue
import dialogueOption
import intEntry
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import java.util.Properties

class KeldagrimPostQuestTest : WorldTest() {

    private fun skipMissing(skip: Boolean) {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = skip.toString()
        Settings.load(properties)
        SettingsReload.now()
    }

    @AfterEach
    fun reset() {
        skipMissing(false)
    }

    @Test
    fun `Dondakan fires players wearing a gold helmet into the mine`() {
        skipMissing(true)
        val player = createPlayer(Tile(2824, 10167))
        val dondakan = createNPC("dondakan_the_dwarf_keldagrim", Tile(2824, 10168))
        player.equipment.set(EquipSlot.Hat.index, "gold_helmet")

        assertEquals("dondakan_the_dwarf_keldagrim_3", dondakan.def(player).stringId)
        player.npcOption(dondakan, "Fire-into-rock")
        tick(6)

        assertTrue(player.tile in Areas["arzinian_mine"])
    }

    @Test
    fun `Dondakan won't fire players without a gold helmet`() {
        skipMissing(true)
        val player = createPlayer(Tile(2824, 10167))
        val dondakan = createNPC("dondakan_the_dwarf_keldagrim", Tile(2824, 10168))

        player.npcOption(dondakan, "Fire-into-rock")
        tick(6)

        assertNotNull(player.dialogue)
        assertFalse(player.tile in Areas["arzinian_mine"])
    }

    @Test
    fun `Asking Dondakan to shoot you into the rock`() {
        skipMissing(true)
        val player = createPlayer(Tile(2824, 10167))
        val dondakan = createNPC("dondakan_the_dwarf_keldagrim", Tile(2824, 10168))
        player.equipment.set(EquipSlot.Hat.index, "gold_helmet")

        player.npcOption(dondakan, "Talk-to")
        tick(2)
        player.dialogueContinue(2)
        player.dialogueOption(1) // Can you shoot me into the rock again?
        player.dialogueContinue(2)
        tick(6)

        assertTrue(player.tile in Areas["arzinian_mine"])
    }

    @Test
    fun `Dondakan won't sell his boots`() {
        skipMissing(true)
        val player = createPlayer(Tile(2824, 10167))
        val dondakan = createNPC("dondakan_the_dwarf_keldagrim", Tile(2824, 10168))
        player.inventory.add("coins", 100_000)

        player.npcOption(dondakan, "Talk-to")
        tick(2)
        player.dialogueContinue(2)
        player.dialogueOption(2) // Are you selling those boots of yours?
        player.dialogueContinue(5)
        player.dialogueOption(2) // Pretty please?
        player.dialogueContinue(6)
        player.intEntry(80_000)
        tick()

        assertNotNull(player.dialogue)
        assertEquals(100_000, player.inventory.count("coins"))
    }

    @Test
    fun `Removing the gold helmet leaves the mine`() {
        val player = createPlayer(Tile(2824, 10167))
        player.equipment.set(EquipSlot.Hat.index, "gold_helmet")
        player.tele(Tile(2355, 4960))
        tick()

        player.equipment.remove(EquipSlot.Hat.index, "gold_helmet")
        tick(6)

        assertTrue(player.containsMessage("sucked out of the rock"))
        assertEquals(Tile(2823, 10164), player.tile)
    }

    @Test
    fun `Logging in inside the mine leaves it`() {
        val player = createPlayer(Tile(2355, 4960))

        assertEquals(Tile(2823, 10164), player.tile)
    }

    @Test
    fun `Boatman ships gold ore to the bank for a fifth of the load`() {
        skipMissing(true)
        val player = createPlayer(Tile(2873, 10165))
        val boatman = createNPC("dwarven_boatman_keldagrim", Tile(2873, 10166))
        player.inventory.add("gold_ore", 12)

        assertEquals("dwarven_boatman_keldagrim_4", boatman.def(player).stringId)
        player.npcOption(boatman, "Deliver-gold")
        tick(2)
        player.dialogueOption(1) // Yes, please take my gold to the city.
        player.dialogueContinue(2)
        player.dialogueOption(1) // Yes.
        player.dialogueContinue(2)
        tick()

        assertEquals(0, player.inventory.count("gold_ore"))
        assertEquals(9, player.bank.count("gold_ore"))
    }

    @Test
    fun `Boatman fees round up`() {
        assertEquals(1, OreHaulerBoatman.fee(5))
        assertEquals(2, OreHaulerBoatman.fee(6))
        assertEquals(6, OreHaulerBoatman.fee(28))
        assertEquals(1, OreHaulerBoatman.charmedFee(10))
        assertEquals(3, OreHaulerBoatman.charmedFee(28))
    }

    @Test
    fun `Boatman introduces himself and refuses a single ore`() {
        skipMissing(true)
        val player = createPlayer(Tile(2873, 10165))
        val boatman = createNPC("dwarven_boatman_keldagrim", Tile(2873, 10166))
        player.inventory.add("gold_ore")

        player.npcOption(boatman, "Talk-to")
        tick(2)
        player.dialogueContinue(8)

        assertTrue(player["ore_hauler_introduced", false])
        assertNotNull(player.dialogue)
        assertEquals(1, player.inventory.count("gold_ore"))
    }

    @Test
    fun `King Veldaban talks about his reign`() {
        skipMissing(true)
        val player = createPlayer(Tile(2879, 10192, 2))
        val veldaban = createNPC("king_veldaban", Tile(2879, 10193, 2))

        assertEquals("king_veldaban_2", veldaban.def(player).stringId)
        player.npcOption(veldaban, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption(2) // Your Majesty.
        player.dialogueContinue(3)
        player.dialogueOption(1) // I don't understand how you ended up as king.

        assertNotNull(player.dialogue)
    }
}
