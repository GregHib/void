package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Condition
import content.bot.behaviour.Reason
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractNPC

class BotFightNpcTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        NPCs.clear()
        FloorItems.clear()
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
    }

    @Test
    fun `Low hp triggers eat and returns wait`() {
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 2))
        player.levels.set(Skill.Constitution, 5)
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 100))

        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, options = arrayOf("Eat"))),
            mapOf("fish" to 0),
        )
        player.inventory.add("fish")

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractInterface
                true
            },
        )

        val action = BotFightNpc(id = "cow")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
    }

    @Test
    fun `Eat fails if execution invalid`() {
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 2))
        player.levels.set(Skill.Constitution, 5)
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 100))

        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, options = arrayOf("Eat"))),
            mapOf("fish" to 0),
        )
        player.inventory.add("fish")

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotFightNpc(id = "cow")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Success condition returns success`() {
        player.start("true", 10)
        val action = BotFightNpc(
            id = "cow",
            success = Condition.Clock("true"),
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `PlayerOnNPCInteract with no success returns success`() {
        val mock = mockk<PlayerOnNPCInteract>(relaxed = true)
        bot.mode = mock

        val action = BotFightNpc(id = "cow")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Loot valid floor item`() {
        ItemDefinitions.set(arrayOf(ItemDefinition(cost = 1000, floorOptions = arrayOf("Take"), stackable = 1)), mapOf("coins" to 0))
        FloorItems.add(player.tile, "coins", 100, owner = player.accountName)
        FloorItems.run()

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractFloorItem
                true
            },
        )

        val action = BotFightNpc(id = "cow", lootOverValue = 0)

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Attack valid npc`() {
        NPCDefinitions.set(
            arrayOf(NPCDefinition(options = arrayOf("Attack"))),
            mapOf("cow" to 0),
        )
        NPCs.add("cow", player.tile)
        NPCs.run()

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractNPC
                true
            },
        )

        val action = BotFightNpc(id = "cow")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `No target without success returns failed`() {
        val action = BotFightNpc(id = "cow")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoTarget), state)
    }

    @Test
    fun `No target with success and delay returns wait`() {
        val action = BotFightNpc(
            id = "cow",
            success = Condition.Clock("false"),
            delay = 5,
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(
            BehaviourState.Wait(5, BehaviourState.Running),
            state,
        )
    }

    @Test
    fun `No target with success and no delay returns running`() {
        val action = BotFightNpc(
            id = "cow",
            success = Condition.Clock("false"),
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }
}
