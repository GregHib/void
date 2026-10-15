package content.bot.behaviour.condition

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class BotInventorySetupTest {
    @Test
    fun `Inventory parsing preserves the usable item restriction`() {
        val parsed = Condition.parse(listOf("inventory" to listOf(mapOf("id" to "steel_hatchet", "usable" to true))), "test").single() as BotInventorySetup
        assertTrue(parsed.items.single().usable)
        assertEquals(1, parsed.items.single().min)
    }

    @Test
    fun `Inventory entries without usable keep the existing default`() {
        val parsed = Condition.parse(listOf("inventory" to listOf(mapOf("id" to "steel_hatchet"))), "test").single() as BotInventorySetup
        assertFalse(parsed.items.single().usable)
    }

    @Test
    fun `Equipment parsing preserves the usable item restriction`() {
        val parsed = Condition.parse(listOf("equipment" to listOf(mapOf("weapon" to mapOf("id" to "steel_hatchet", "usable" to true)))), "test").single() as BotEquipmentSetup
        assertTrue(parsed.items.getValue(EquipSlot.Weapon).usable)
    }

    @Test
    fun `Inventory and bank parsing preserve equippable independently of usable`() {
        for (type in listOf("inventory", "bank")) {
            val parsed = Condition.parse(listOf(type to listOf(mapOf("id" to "steel_hatchet", "equippable" to true))), "test").single()
            val item = when (parsed) {
                is BotInventorySetup -> parsed.items.single()
                is BotBankSetup -> parsed.items.single()
                else -> error("Unexpected condition $parsed")
            }
            assertTrue(item.equippable)
            assertFalse(item.usable)
        }
    }

    @Test
    fun `Equipment parsing preserves equippable and leaves it optional`() {
        for (flag in listOf(true, false, null)) {
            val entry = mutableMapOf<String, Any>("id" to "steel_hatchet")
            if (flag != null) entry["equippable"] = flag
            val parsed = Condition.parse(listOf("equipment" to listOf(mapOf("weapon" to entry))), "test").single() as BotEquipmentSetup
            assertEquals(flag == true, parsed.items.getValue(EquipSlot.Weapon).equippable)
        }
    }
}
