package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotHasClock
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
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

class BotFightPlayerTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        FloorItems.clear()
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
    }

    private fun initInventory(length: Int = 28) {
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = length))
    }

    @Test
    fun `Low hp triggers eat and returns wait`() {
        initInventory(length = 2)
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

        val action = BotFightPlayer()

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
    }

    @Test
    fun `Eat fails if execution invalid`() {
        initInventory(length = 2)
        player.levels.set(Skill.Constitution, 5)
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 100))

        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, options = arrayOf("Eat"))),
            mapOf("fish" to 0),
        )
        player.inventory.add("fish")

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotFightPlayer()

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Success condition returns success`() {
        player.start("true", 10)
        val action = BotFightPlayer(success = BotHasClock("true"))

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `PlayerOnPlayerInteract with no success returns success`() {
        val mock = mockk<PlayerOnPlayerInteract>(relaxed = true)
        bot.mode = mock

        val action = BotFightPlayer()

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Loot valid floor item`() {
        initInventory()
        ItemDefinitions.set(arrayOf(ItemDefinition(cost = 1000, floorOptions = arrayOf("Take"), stackable = 1)), mapOf("coins" to 0))
        FloorItems.add(player.tile, "coins", 100, owner = player.accountName)
        FloorItems.run()
        player.start("loot_pending", 60)
        player["loot_drop_tile"] = player.tile.id

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractFloorItem
                true
            },
        )

        val action = BotFightPlayer(lootOverValue = 0)

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Wealth strategy loots highest value item first`() {
        initInventory()
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 200, cost = 100, floorOptions = arrayOf("Take"), stackable = 1),
                ItemDefinition(id = 201, cost = 5_000, floorOptions = arrayOf("Take"), stackable = 1),
            ),
            mapOf("trinket" to 0, "rune" to 1),
        )
        FloorItems.add(player.tile, "trinket", 1, owner = player.accountName)
        FloorItems.add(player.tile, "rune", 1, owner = player.accountName)
        FloorItems.run()
        player.start("loot_pending", 60)
        player["loot_drop_tile"] = player.tile.id

        var pickedId = -1
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractFloorItem) {
                    pickedId = instruction.id
                }
                true
            },
        )

        val action = BotFightPlayer(lootStrategy = BotLootStrategy.WEALTH)

        action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(201, pickedId)
    }

    @Test
    fun `Survival strategy skips non-consumable loot`() {
        initInventory()
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 300, cost = 1_000, floorOptions = arrayOf("Take"), stackable = 1)),
            mapOf("rune_axe" to 0),
        )
        FloorItems.add(player.tile, "rune_axe", 1, owner = player.accountName)
        FloorItems.run()
        player.start("loot_pending", 60)
        player["loot_drop_tile"] = player.tile.id

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractFloorItem) called = true
                true
            },
        )

        val action = BotFightPlayer(lootStrategy = BotLootStrategy.SURVIVAL)

        action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(!called)
    }

    @Test
    fun `Survival strategy loots edible items`() {
        initInventory()
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 400, cost = 10, floorOptions = arrayOf("Take"), options = arrayOf("Eat"), stackable = 0),
                ItemDefinition(id = 401, cost = 9_999, floorOptions = arrayOf("Take"), stackable = 1),
            ),
            mapOf("shark" to 0, "diamond" to 1),
        )
        FloorItems.add(player.tile, "shark", 1, owner = player.accountName)
        FloorItems.add(player.tile, "diamond", 1, owner = player.accountName)
        FloorItems.run()
        player.start("loot_pending", 60)
        player["loot_drop_tile"] = player.tile.id

        var pickedId = -1
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractFloorItem) {
                    pickedId = instruction.id
                }
                true
            },
        )

        val action = BotFightPlayer(lootStrategy = BotLootStrategy.SURVIVAL)

        action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(400, pickedId)
    }

    @Test
    fun `Survival strategy loots potions identified by categories`() {
        initInventory()
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(
                    id = 500,
                    cost = 50,
                    floorOptions = arrayOf("Take"),
                    options = arrayOf(null),
                    params = mapOf(5019 to setOf("potion")),
                    stackable = 0,
                ),
            ),
            mapOf("super_restore_4" to 0),
        )
        FloorItems.add(player.tile, "super_restore_4", 1, owner = player.accountName)
        FloorItems.run()
        player.start("loot_pending", 60)
        player["loot_drop_tile"] = player.tile.id

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractFloorItem) called = true
                true
            },
        )

        val action = BotFightPlayer(lootStrategy = BotLootStrategy.SURVIVAL)

        action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
    }

    @Test
    fun `Loot stops when inventory has only one free slot left`() {
        initInventory(length = 4)
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 600, cost = 100, floorOptions = arrayOf("Take"), stackable = 1),
                ItemDefinition(id = 601, cost = 200, floorOptions = arrayOf("Take"), stackable = 1),
                ItemDefinition(id = 602, cost = 300, floorOptions = arrayOf("Take"), stackable = 1),
                ItemDefinition(id = 603, cost = 1_000, floorOptions = arrayOf("Take"), stackable = 1),
            ),
            mapOf("a" to 0, "b" to 1, "c" to 2, "drop" to 3),
        )
        player.inventory.add("a")
        player.inventory.add("b")
        player.inventory.add("c")
        FloorItems.add(player.tile, "drop", 1, owner = player.accountName)
        FloorItems.run()
        player.start("loot_pending", 60)
        player["loot_drop_tile"] = player.tile.id

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractFloorItem) called = true
                true
            },
        )

        val action = BotFightPlayer()

        action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(!called)
        assertTrue(!player.hasClock("loot_pending"))
    }

    @Test
    fun `Full inventory still tops up an existing stackable pile`() {
        initInventory(length = 4)
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 700, cost = 100, floorOptions = arrayOf("Take"), stackable = 1),
                ItemDefinition(id = 701, cost = 100, floorOptions = arrayOf("Take"), stackable = 1),
                ItemDefinition(id = 702, cost = 100, floorOptions = arrayOf("Take"), stackable = 1),
            ),
            mapOf("coins" to 0, "a" to 1, "b" to 2),
        )
        player.inventory.add("coins", amount = 50)
        player.inventory.add("a")
        player.inventory.add("b")
        FloorItems.add(player.tile, "coins", 100, owner = player.accountName)
        FloorItems.run()
        player.start("loot_pending", 60)
        player["loot_drop_tile"] = player.tile.id

        var pickedId = -1
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractFloorItem) {
                    pickedId = instruction.id
                }
                true
            },
        )

        val action = BotFightPlayer()

        action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(700, pickedId)
    }

    @Test
    fun `Empty ground clears the loot_pending clock`() {
        initInventory()
        player.start("loot_pending", 60)
        player["loot_drop_tile"] = player.tile.id

        val action = BotFightPlayer()

        action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(!player.hasClock("loot_pending"))
    }

    @Test
    fun `No target without success returns failed`() {
        val action = BotFightPlayer()

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoTarget), state)
    }

    @Test
    fun `No target with success and delay returns wait`() {
        val action = BotFightPlayer(
            success = BotHasClock("false"),
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
        val action = BotFightPlayer(
            success = BotHasClock("false"),
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }
}
