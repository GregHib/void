package content.bot.behaviour.setup

import content.bot.behaviour.action.BotGoTo
import content.bot.behaviour.action.BotGoToNearest
import content.bot.behaviour.action.BotInteractNpc
import content.bot.behaviour.action.BotInteractObject
import content.bot.behaviour.action.BotInterfaceOption
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotInterfaceOpen
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.entity.player.bank.bank
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import set
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class DynamicResolversTest {

    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        DynamicResolvers.shopItems.clear()
        DynamicResolvers.sampleItems.clear()
        Areas.clear()
        player = Player()
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 10))
        player.inventories.inventory(InventoryDefinition(stringId = "bank", length = 10))
        player.inventories.inventory(InventoryDefinition(stringId = "worn_equipment", length = 10))
        ItemDefinitions.clear()
        NPCs.clear()
        GameObjects.reset()
    }

    @Test
    fun `Resolve not in area with GoTo`() {
        val condition = BotInArea("target_area")
        Areas.set(mapOf("target_area" to AreaDefinition("target_area", player.tile.toCuboid(2))))
        val resolver = DynamicResolvers.resolver(player, condition)
        assertNotNull(resolver)
        assertTrue(resolver!!.actions.first() is BotGoTo)
    }

    @Test
    fun `Ignore unsupported conditions`() {
        val condition = BotInterfaceOpen("shop")
        val resolver = DynamicResolvers.resolver(player, condition)
        assertNull(resolver)
    }

    @Test
    fun `Resolve missing inventory item with one in bank`() {
        ItemDefinitions.set(arrayOf(ItemDefinition(id = 100)), mapOf("fish" to 0))
        val entry = BotItem(setOf("fish"))
        player.bank.add("fish")

        val resolver = DynamicResolvers.resolver(player, BotInventorySetup(listOf(entry)))
        assertNotNull(resolver)
        assertTrue(resolver!!.actions.any { it is BotGoToNearest || it is BotInteractObject })
    }

    @Test
    fun `Resolve not enough empty inventory spaces with bank deposit`() {
        val entry = BotItem(setOf("empty"), min = 20)
        val resolver = DynamicResolvers.resolver(player, BotInventorySetup(listOf(entry)))
        assertNotNull(resolver)
        assertEquals("deposit_all_bank", resolver!!.id)
    }

    @Test
    fun `Resolve unequipped item in inventory by equipping it`() {
        ItemDefinitions.set(arrayOf(ItemDefinition(stringId = "sword", extras = mapOf("slot" to EquipSlot.Weapon))), mapOf("sword" to 0))
        val entry = BotItem(setOf("sword"))
        player.inventory.add("sword")

        val resolver = DynamicResolvers.resolver(player, BotEquipmentSetup(mapOf(EquipSlot.Weapon to entry)))
        assertNotNull(resolver)
        assertEquals("equip_items", resolver!!.id)
    }

    @Test
    fun `Resolve blank equipment slot by unequipping current item`() {
        ItemDefinitions.set(arrayOf(ItemDefinition(stringId = "sword")), mapOf("sword" to 0))
        player.equipment.set(EquipSlot.Weapon.index, "sword")
        val entry = BotItem(setOf("empty"))
        val resolver = DynamicResolvers.resolver(player, BotEquipmentSetup(mapOf(EquipSlot.Weapon to entry)))
        assertNotNull(resolver)
        assertEquals("unequip_items", resolver!!.id)
    }

    @Test
    fun `Take free samples from shop before buying items`() {
        DynamicResolvers.sampleItems["fish"] = mutableListOf("market" to "trader")
        val entry = BotItem(setOf("fish"), min = 5)

        val resolver = DynamicResolvers.resolver(player, BotInventorySetup(listOf(entry)))
        assertNotNull(resolver)
        assertEquals("take_from_shop", resolver!!.id)
        assertTrue(resolver.actions.any { it is BotGoTo })
        assertTrue(resolver.actions.any { it is BotInteractNpc })
        assertTrue(resolver.actions.any { it is BotInterfaceOption })
    }

    @Test
    fun `Resolve unowned item by buying item from shop`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(cost = 15), ItemDefinition(stackable = 1)),
            mapOf("fish" to 0, "coins" to 1),
        )
        DynamicResolvers.shopItems["fish"] = mutableListOf("market" to "trader")
        val entry = BotItem(setOf("fish"), min = 5)
        player.inventory.add("coins", 1000)

        val resolver = DynamicResolvers.resolver(player, BotInventorySetup(listOf(entry)))
        assertNotNull(resolver)
        assertEquals("buy_from_shop", resolver!!.id)
        assertTrue(resolver.actions.any { it is BotGoTo })
        assertTrue(resolver.actions.any { it is BotInteractNpc })
        assertTrue(resolver.actions.any { it is BotInterfaceOption })
    }

    @Test
    fun `Resolve inventory returns null if no matching resolver`() {
        val entry = BotItem(setOf("missing"))
        val resolver = DynamicResolvers.resolver(player, BotInventorySetup(listOf(entry)))
        assertNull(resolver)
    }

    @Test
    fun `Valid returns false if item empty`() {
        val entry = BotItem(setOf("item"))
        val item = Item()
        assertFalse(DynamicResolvers.valid(player, item, entry))
    }

    @Test
    fun `Valid returns false if item id not in entry`() {
        val entry = BotItem(setOf("other"))
        val item = Item("item")
        assertFalse(DynamicResolvers.valid(player, item, entry))
    }

    @Test
    fun `Valid returns true for matching item`() {
        val entry = BotItem(setOf("item"))
        val item = Item("item")
        assertTrue(DynamicResolvers.valid(player, item, entry))
    }
}
