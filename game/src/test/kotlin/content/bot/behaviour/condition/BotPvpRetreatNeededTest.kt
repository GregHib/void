package content.bot.behaviour.condition

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import set
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack

class BotPvpRetreatNeededTest {

    private lateinit var player: Player
    private lateinit var condition: BotPvpRetreatNeeded

    @BeforeEach
    fun setup() {
        player = Player()
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 28))
        condition = BotPvpRetreatNeeded()
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
    }

    @Test
    fun `Returns false when bot has eat-able item`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, options = arrayOf("Eat"))),
            mapOf("shark" to 0),
        )
        player.inventory.add("shark")
        assertFalse(condition.check(player))
    }

    @Test
    fun `Returns true when single-combat and inventory has no eat-able items`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 200, options = arrayOf("Wield"))),
            mapOf("whip" to 0),
        )
        player.inventory.add("whip")
        assertTrue(condition.check(player))
    }

    @Test
    fun `Returns true when inventory is completely empty`() {
        assertTrue(condition.check(player))
    }

    @Test
    fun `Returns false when in multi-combat zone regardless of food`() {
        player["in_multi_combat"] = true
        assertFalse(condition.check(player))
    }

    @Test
    fun `Returns false in multi-combat even with no food`() {
        player["in_multi_combat"] = true
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 200, options = arrayOf("Wield"))),
            mapOf("whip" to 0),
        )
        player.inventory.add("whip")
        assertFalse(condition.check(player))
    }
}
