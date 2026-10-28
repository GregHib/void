package content.bot.behaviour.setup

import content.bot.behaviour.action.BotGoTo
import content.bot.behaviour.action.BotGoToNearest
import content.bot.behaviour.action.BotInteractNpc
import content.bot.behaviour.action.BotInterfaceOption
import content.bot.behaviour.action.BotOpenBank
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotInterfaceOpen
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.BotVariable
import content.bot.behaviour.condition.Condition
import content.entity.player.bank.bank
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import set
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
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
        player.levels.link(player, PlayerLevels(player.experience))
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
        assertTrue(resolver!!.actions[0] is BotGoToNearest)
        assertTrue(resolver.actions[1] is BotOpenBank)
    }

    @Test
    fun `Bank withdrawals check the requested note mode for each item`() {
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(stringId = "pickaxe", noteId = 1),
                ItemDefinition(stringId = "pickaxe_noted", noteId = 0, notedTemplateId = 799, stackable = 1),
            ),
            mapOf("pickaxe" to 0, "pickaxe_noted" to 1),
        )
        player.bank.add("pickaxe", 10)

        for (initialMode in listOf(false, true)) {
            player["bank_notes"] = initialMode
            val resolver = DynamicResolvers.resolver(
                player,
                BotInventorySetup(listOf(BotItem(setOf("pickaxe_noted"), min = 5), BotItem(setOf("pickaxe")))),
            )!!
            val options = resolver.actions.filterIsInstance<BotInterfaceOption>()
            val modes = options.filter { it.id == "bank:note_mode" }
            assertEquals(listOf(true, false), modes.map { (it.success as BotVariable).equals })
            for (mode in modes) {
                val condition = mode.success as BotVariable
                val desiredMode = condition.equals as Boolean
                player["bank_notes"] = desiredMode
                assertTrue(condition.check(player))
                player["bank_notes"] = !desiredMode
                assertFalse(condition.check(player))
            }
            assertEquals("bank:inventory:pickaxe", options[options.indexOf(modes[0]) + 1].id)
            assertEquals("Withdraw-X", options[options.indexOf(modes[0]) + 1].option)
            assertEquals("Withdraw-1", options[options.indexOf(modes[1]) + 1].option)
        }
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
        ItemDefinitions.set(arrayOf(ItemDefinition(stringId = "sword", params = mapOf(Params.SLOT to EquipSlot.Weapon))), mapOf("sword" to 0))
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

    private fun toolShop() {
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(stringId = "steel_hatchet", cost = 200, params = mapOf(Params.USE_SKILL_1 to Skill.Woodcutting.ordinal, Params.USE_LEVEL_1 to 6, Params.EQUIP_SKILL_1 to Skill.Attack.ordinal, Params.EQUIP_LEVEL_1 to 5, Params.SLOT to EquipSlot.Weapon)),
                ItemDefinition(stringId = "iron_hatchet", cost = 50, params = mapOf(Params.SLOT to EquipSlot.Weapon)),
                ItemDefinition(stringId = "coins", stackable = 1),
            ),
            mapOf("steel_hatchet" to 0, "iron_hatchet" to 1, "coins" to 2),
        )
        player.levels.set(Skill.Woodcutting, 1)
        for (id in listOf("steel_hatchet", "iron_hatchet")) {
            DynamicResolvers.shopItems[id] = mutableListOf("axe_shop" to "bob")
        }
    }

    @Test
    fun `Purchases skip unusable tools even when one is already carried`() {
        toolShop()
        val entry = BotItem(linkedSetOf("steel_hatchet", "iron_hatchet"), usable = true)
        for (carried in listOf(false, true)) {
            player.inventory.clear()
            player.inventory.add("coins", 10000)
            if (carried) player.inventory.add("steel_hatchet")
            val resolver = DynamicResolvers.resolver(player, BotInventorySetup(listOf(entry)))!!
            assertEquals("buy_from_shop", resolver.id)
            assertEquals(listOf("shop:stock:iron_hatchet"), resolver.actions.filterIsInstance<BotInterfaceOption>().map { it.id })
        }
    }

    @Test
    fun `Samples also skip unusable tools`() {
        toolShop()
        for (id in listOf("steel_hatchet", "iron_hatchet")) {
            DynamicResolvers.sampleItems[id] = mutableListOf("axe_shop" to "bob")
        }
        val resolver = DynamicResolvers.resolver(player, BotInventorySetup(listOf(BotItem(linkedSetOf("steel_hatchet", "iron_hatchet"), usable = true))))!!
        assertEquals("take_from_shop", resolver.id)
        assertEquals(listOf("shop:sample:iron_hatchet"), resolver.actions.filterIsInstance<BotInterfaceOption>().map { it.id })
    }

    @Test
    fun `Equipment selection skips carried items that cannot be used`() {
        toolShop()
        player.inventory.add("steel_hatchet")
        player.inventory.add("iron_hatchet")
        val entry = BotItem(linkedSetOf("steel_hatchet", "iron_hatchet"), usable = true)
        val resolver = DynamicResolvers.resolver(player, BotEquipmentSetup(mapOf(EquipSlot.Weapon to entry)))!!
        assertEquals("equip_items", resolver.id)
        assertEquals(listOf("inventory:inventory:iron_hatchet"), resolver.actions.filterIsInstance<BotInterfaceOption>().map { it.id })
    }

    @Test
    fun `Parsed equippable requirement rejects tools usable for chopping but not wielding`() {
        toolShop()
        player.levels.set(Skill.Woodcutting, 6)
        player.inventory.add("coins", 10000)
        player.inventory.add("steel_hatchet")
        val condition = Condition.parse(listOf("inventory" to listOf(mapOf("id" to "steel_hatchet,iron_hatchet", "equippable" to true))), "test").single() as BotInventorySetup
        assertFalse(condition.check(player))
        val resolver = DynamicResolvers.resolver(player, condition)!!
        assertEquals("buy_from_shop", resolver.id)
        assertEquals(listOf("shop:stock:iron_hatchet"), resolver.actions.filterIsInstance<BotInterfaceOption>().map { it.id })
    }

    @Test
    fun `Equipment condition and selection enforce parsed equippable requirements`() {
        toolShop()
        player.levels.set(Skill.Woodcutting, 6)
        val condition = Condition.parse(listOf("equipment" to listOf(mapOf("weapon" to mapOf("id" to "steel_hatchet,iron_hatchet", "equippable" to true)))), "test").single() as BotEquipmentSetup
        player.equipment.set(EquipSlot.Weapon.index, "steel_hatchet")
        assertFalse(condition.check(player))
        player.inventory.add("steel_hatchet")
        player.inventory.add("iron_hatchet")
        val resolver = DynamicResolvers.resolver(player, condition)!!
        assertEquals("equip_items", resolver.id)
        assertEquals(listOf("inventory:inventory:iron_hatchet"), resolver.actions.filterIsInstance<BotInterfaceOption>().map { it.id })
        player.equipment.set(EquipSlot.Weapon.index, "iron_hatchet")
        assertTrue(condition.check(player))
    }

    @Test
    fun `Purchases return no resolver when every available tool is unusable`() {
        toolShop()
        player.inventory.add("coins", 10000)
        assertNull(DynamicResolvers.resolver(player, BotInventorySetup(listOf(BotItem(setOf("steel_hatchet"), usable = true)))))
    }

    @Test
    fun `Purchases allow upgraded tools after their use requirements are met`() {
        toolShop()
        player.inventory.add("coins", 10000)
        player.levels.set(Skill.Woodcutting, 6)
        val resolver = DynamicResolvers.resolver(player, BotInventorySetup(listOf(BotItem(linkedSetOf("steel_hatchet", "iron_hatchet"), usable = true))))!!
        assertEquals(listOf("shop:stock:steel_hatchet"), resolver.actions.filterIsInstance<BotInterfaceOption>().map { it.id })
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
