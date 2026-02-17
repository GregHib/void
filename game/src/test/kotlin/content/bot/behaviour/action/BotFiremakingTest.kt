package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractInterfaceItem
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.area.Rectangle

class BotFiremakingTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        GameObjects.clear()
        Areas.clear()
        player = Player()
        bot = Bot(player)

        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(
            InventoryDefinition(stringId = "inventory", length = 10),
        )

        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 1),
                ItemDefinition(id = 2),
            ),
            mapOf(
                "tinderbox" to 0,
                "logs" to 1,
            ),
        )
    }

    @Test
    fun `Returns running if bot not in empty mode`() {
        bot.mode = mockk(relaxed = true)

        val action = BotFiremaking("logs", "area")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Walks one tile back if cannot light and can travel`() {
        ObjectDefinitions.set(arrayOf(ObjectDefinition(name = "flowers")), mapOf("flowers" to 0))
        GameObjects.add("flowers", player.tile, ObjectShape.CENTRE_PIECE_STRAIGHT, ObjectLayer.GROUND)

        var walked = false
        val world = FakeWorld(
            canTravel = { _, _, _, _, _ -> true },
            execute = { _, instruction ->
                walked = instruction is Walk
                true
            },
        )

        val action = BotFiremaking("logs", "area")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(walked)
        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
    }

    @Test
    fun `Returns to original area if no valid tiles`() {
        ObjectDefinitions.set(arrayOf(ObjectDefinition(name = "fire")), mapOf("fire" to 0))
        GameObjects.add("fire", player.tile, ObjectShape.CENTRE_PIECE_STRAIGHT, ObjectLayer.GROUND)

        Areas.set(
            mapOf(
                "area" to AreaDefinition("area", Rectangle(0, 0, 2, 2), setOf("tags")),
            ),
            emptyMap(),
            emptyMap(),
        )

        var called = false
        val world = FakeWorld(
            canTravel = { _, _, _, _, _ -> false },
            execute = { _, instruction ->
                called = instruction is Walk
                true
            },
        )

        val action = BotFiremaking("logs", "area")
        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
        assertTrue(called)
    }

    @Test
    fun `Fails if cannot light and no valid tiles`() {
        ObjectDefinitions.set(arrayOf(ObjectDefinition(name = "fire")), mapOf("fire" to 0))
        GameObjects.add("fire", player.tile, ObjectShape.CENTRE_PIECE_STRAIGHT, ObjectLayer.GROUND)

        Areas.set(
            mapOf(
                "area" to AreaDefinition("area", Rectangle(0, 0, 0, 0), setOf("tags")),
            ),
            emptyMap(),
            emptyMap(),
        )

        val world = FakeWorld(
            canTravel = { _, _, _, _, _ -> false },
        )

        val action = BotFiremaking("logs", "area")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.Stuck), state)
    }

    @Test
    fun `Uses tinderbox on logs when inventory contains item`() {
        player.inventory.add("tinderbox")
        player.inventory.add("logs")

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractInterface || instruction is InteractInterfaceItem
                true
            },
        )

        val action = BotFiremaking("logs", "area")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertTrue(state is BehaviourState.Running || state is BehaviourState.Wait)
    }

    @Test
    fun `Returns success when no logs in inventory`() {
        val action = BotFiremaking("logs", "area")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }
}
