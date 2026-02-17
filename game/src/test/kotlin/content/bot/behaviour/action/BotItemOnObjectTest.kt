package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject

class BotItemOnObjectTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        GameObjects.reset()
        ItemDefinitions.clear()

        player = Player()
        bot = Bot(player)

        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(
            InventoryDefinition(stringId = "inventory", length = 10),
        )

        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 1)),
            mapOf("bucket" to 0),
        )
    }

    @Test
    fun `Fails if inventory item missing`() {
        val action = BotItemOnObject("bucket", "well")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Valid interaction returns running`() {
        player.inventory.add("bucket")

        ObjectDefinitions.set(
            arrayOf(ObjectDefinition(id = 0, stringId = "well")),
            mapOf("well" to 0),
        )
        GameObjects.add("well", player.tile)

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractInterfaceObject
                true
            },
        )

        val action = BotItemOnObject("bucket", "well")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        assertTrue(called)
    }

    @Test
    fun `Execution failure returns failed`() {
        player.inventory.add("bucket")

        ObjectDefinitions.set(
            arrayOf(ObjectDefinition()),
            mapOf("well" to 0),
        )
        GameObjects.add("well", player.tile)

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotItemOnObject("bucket", "well")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `No target without success returns failed`() {
        player.inventory.add("bucket")

        val action = BotItemOnObject("bucket", "well")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoTarget), state)
    }
}
