package content.skill.farming

import WorldTest
import containsMessage
import interfaceOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class FarmingEquipmentStoreTest : WorldTest() {

    @Test
    fun `Store item with single limit`() {
        val player = createPlayer()
        player.inventory.add("rake")
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store_side", "rake", "Store")

        assertEquals(0, player.inventory.count("rake"))
        assertEquals(1, player["farming_tool_rake", 0])
    }

    @Test
    fun `Store items with multiple limit`() {
        val player = createPlayer()
        player.inventory.add("bucket", 6)
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store_side", "bucket", "Store 5")

        assertEquals(1, player.inventory.count("bucket"))
        assertEquals(5, player["farming_tool_bucket", 0])
    }

    @Test
    fun `Store magic secateurs`() {
        val player = createPlayer()
        player.inventory.add("magic_secateurs")
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store_side", "secateurs", "Store")

        assertEquals(0, player.inventory.count("magic_secateurs"))
        assertEquals(1, player["farming_tool_secateurs", 0])
        assertEquals("magic_secateurs", player["farming_tool_secateurs_type", "secateurs"])
    }

    @Test
    fun `Store filled watering can`() {
        val player = createPlayer()
        player.inventory.add("watering_can_5")
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store_side", "watering_can", "Store")

        assertEquals(0, player.inventory.count("watering_can_5"))
        assertEquals(6, player["farming_tool_watering_can", 0])
    }

    @Test
    fun `Can't store item if single limit reached`() {
        val player = createPlayer()
        player.inventory.add("seed_dibber")
        player.open("farming_equipment_store")
        player["farming_tool_seed_dibber"] = 1

        player.interfaceOption("farming_equipment_store_side", "seed_dibber", "Store")

        assertEquals(1, player.inventory.count("seed_dibber"))
        assertEquals(1, player["farming_tool_seed_dibber", 0])
        assertTrue(player.containsMessage("You cannot store more than one seed dibber"))
    }

    @Test
    fun `Can't store item if multiple limit reached`() {
        val player = createPlayer()
        player.inventory.add("compost")
        player.open("farming_equipment_store")
        player["farming_tool_compost"] = 255

        player.interfaceOption("farming_equipment_store_side", "compost", "Store 1")

        assertEquals(1, player.inventory.count("compost"))
        assertEquals(255, player["farming_tool_compost", 0])
        assertTrue(player.containsMessage("You cannot store that much compost"))
    }

    @Test
    fun `Can store some items if close to limit`() {
        val player = createPlayer()
        player.inventory.add("supercompost", 5)
        player["farming_tool_supercompost"] = 253
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store_side", "supercompost", "Store All")

        assertEquals(3, player.inventory.count("supercompost"))
        assertEquals(255, player["farming_tool_supercompost", 0])
    }

    @Test
    fun `Can't store nothing`() {
        val player = createPlayer()
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store_side", "rake", "Store")

        assertEquals(0, player["farming_tool_rake", 0])
        assertTrue(player.containsMessage("You haven't got a rake"))
    }

    @Test
    fun `Remove single item`() {
        val player = createPlayer()
        player["farming_tool_trowel"] = 1
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store", "trowel", "Remove")

        assertEquals(1, player.inventory.count("trowel"))
        assertEquals(0, player["farming_tool_trowel", 0])
    }

    @Test
    fun `Remove multiple items`() {
        val player = createPlayer()
        player["farming_tool_bucket"] = 15
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store", "bucket", "Remove 5")

        assertEquals(5, player.inventory.count("bucket"))
        assertEquals(10, player["farming_tool_bucket", 0])
    }

    @Test
    fun `Remove partial amount`() {
        val player = createPlayer()
        player["farming_tool_compost"] = 3
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store", "compost", "Remove 5")

        assertEquals(3, player.inventory.count("compost"))
        assertEquals(0, player["farming_tool_compost", 0])
    }

    @Test
    fun `Remove inventory full`() {
        val player = createPlayer()
        player["farming_tool_scarecrow"] = 3
        player.inventory.add("bucket", 28)
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store", "scarecrow", "Remove 1")

        assertEquals(0, player.inventory.count("scarecrow"))
        assertEquals(3, player["farming_tool_scarecrow", 0])
        assertTrue(player.containsMessage("You don't have room"))
    }

    @Test
    fun `Remove inventory almost full`() {
        val player = createPlayer()
        player["farming_tool_bucket"] = 5
        player.inventory.add("bucket", 25)
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store", "bucket", "Remove All")

        assertEquals(28, player.inventory.count("bucket"))
        assertEquals(2, player["farming_tool_bucket", 0])
    }

    @Test
    fun `Remove magic secateurs`() {
        val player = createPlayer()
        player["farming_tool_secateurs"] = 1
        player["farming_tool_secateurs_type"] = "magic_secateurs"
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store", "secateurs", "Remove")

        assertEquals(1, player.inventory.count("magic_secateurs"))
        assertEquals(0, player["farming_tool_secateurs", 0])
        assertEquals("magic_secateurs", player["farming_tool_secateurs_type", "secateurs"])
    }

    @Test
    fun `Remove filled watering can`() {
        val player = createPlayer()
        player["farming_tool_watering_can"] = 6
        player.open("farming_equipment_store")

        player.interfaceOption("farming_equipment_store", "watering_can", "Remove")

        assertEquals(1, player.inventory.count("watering_can_5"))
        assertEquals(0, player["farming_tool_watering_can", 0])
    }

}