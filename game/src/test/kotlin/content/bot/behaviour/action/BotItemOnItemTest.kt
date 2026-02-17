package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.condition.BotHasClock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack

class BotItemOnItemTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        ItemDefinitions.clear()

        player = Player()
        bot = Bot(player)

        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(
            InventoryDefinition(stringId = "inventory", length = 10)
        )

        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 1),
                ItemDefinition(id = 2)
            ),
            mapOf(
                "knife" to 0,
                "logs" to 1
            )
        )
    }

    @Test
    fun `Fails if from item missing`() {
        player.inventory.add("logs")

        val action = BotItemOnItem("knife", "logs")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Fails if to item missing`() {
        player.inventory.add("knife")

        val action = BotItemOnItem("knife", "logs")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Execution failure returns failed`() {
        player.inventory.add("knife")
        player.inventory.add("logs")

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotItemOnItem("knife", "logs")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Successful interaction returns wait success when no success condition`() {
        player.inventory.add("knife")
        player.inventory.add("logs")

        val world = FakeWorld(execute = { _, _ -> true })

        val action = BotItemOnItem("knife", "logs")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(
            BehaviourState.Wait(1, BehaviourState.Success),
            state
        )
    }

    @Test
    fun `Success condition returns success`() {
        player.start("done", 10)

        val action = BotItemOnItem(item = "knife", on = "logs", success = BotHasClock("done"))

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }
}
