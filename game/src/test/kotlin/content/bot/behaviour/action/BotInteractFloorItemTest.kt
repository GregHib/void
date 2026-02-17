package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Condition
import content.bot.behaviour.Reason
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.network.client.instruction.InteractFloorItem

class BotInteractFloorItemTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        FloorItems.clear()
        ItemDefinitions.clear()

        player = Player()
        bot = Bot(player)
    }

    @Test
    fun `Success condition returns success`() {
        player.start("done", 10)

        val action = BotInteractFloorItem(
            option = "Take",
            id = "coins",
            success = Condition.Clock("done")
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Valid floor item interaction returns running`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(floorOptions = arrayOf("Take"))),
            mapOf("coins" to 0)
        )
        FloorItems.add(player.tile, "coins", 1, owner = player.accountName)
        FloorItems.run()

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractFloorItem
                true
            }
        )

        val action = BotInteractFloorItem("Take", "coins")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Execution failure returns failed`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(floorOptions = arrayOf("Take"))),
            mapOf("coins" to 0)
        )
        FloorItems.add(player.tile, "coins", 1, owner = player.accountName)
        FloorItems.run()

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotInteractFloorItem("Take", "coins")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `No target without success returns failed`() {
        val action = BotInteractFloorItem("Take", "coins")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoTarget), state)
    }

    @Test
    fun `No target with success and delay returns wait`() {
        val action = BotInteractFloorItem(
            option = "Take",
            id = "coins",
            success = Condition.Clock("missing"),
            delay = 5
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(
            BehaviourState.Wait(5, BehaviourState.Running),
            state
        )
    }
}
