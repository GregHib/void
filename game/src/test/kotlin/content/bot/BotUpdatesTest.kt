package content.bot

import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotInterfaceOpen
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.BotSkillLevel
import content.bot.behaviour.condition.BotVariable
import content.bot.behaviour.setup.Resolver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile

class BotUpdatesTest {

    private lateinit var manager: BotManager
    private val activities: MutableMap<String, BotActivity> = mutableMapOf()
    private val resolvers: MutableMap<String, MutableList<Resolver>> = mutableMapOf()
    private val groups: MutableMap<String, MutableList<String>> = mutableMapOf()

    @BeforeEach
    fun setup() {
        Script.clear()
        BotUpdates()
        activities.clear()
        resolvers.clear()
        groups.clear()
        manager = BotManager(activities, resolvers, groups, FakeWorld())
    }

    @Test
    fun `Level change evaluates skill requirements`() {
        val bot = createBot()
        activities["kill_chickens"] = BotActivity(
            "kill_chickens",
            requires = listOf(BotSkillLevel(Skill.Attack, 1, 5)),
        )
        manager.reloadGroups()

        manager.add(bot)
        assertTrue(bot.available.contains("kill_chickens"))

        bot.levels.set(Skill.Attack, 10)
        manager.updateAvailable(bot)

        assertFalse(bot.available.contains("kill_chickens"))
    }

    @Test
    fun `Inventory change evaluates inventory requirements`() {
        val bot = createBot()
        ItemDefinitions.set(arrayOf(ItemDefinition(name = "item")), mapOf("item" to 0))
        activities["bank_items"] = BotActivity(
            "bank_items",
            requires = listOf(BotInventorySetup(listOf(BotItem(setOf("item"), 1)))),
        )
        manager.reloadGroups()

        manager.add(bot)
        assertFalse(bot.available.contains("bank_items"))

        bot.player.inventory.add("item")
        manager.updateAvailable(bot)

        assertTrue(bot.available.contains("bank_items"))
    }

    @Test
    fun `Interface change evaluates interface requirements`() {
        val bot = createBot()
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(type = "main_screen", stringId = "test")), mapOf("test" to 0))
        activities["craft"] = BotActivity(
            "craft",
            requires = listOf(BotInterfaceOpen("test")),
        )
        manager.reloadGroups()

        manager.add(bot)
        assertFalse(bot.available.contains("craft"))

        assertTrue(bot.player.interfaces.open("test"))
        manager.updateAvailable(bot)

        assertTrue(bot.available.contains("craft"))

        bot.player.interfaces.close("test")
        manager.updateAvailable(bot)

        assertFalse(bot.available.contains("craft"))
    }

    @Test
    fun `Area change evaluates area requirements`() {
        val bot = createBot()
        val definition = AreaDefinition("test", bot.player.tile.toCuboid(2))
        Areas.set(mapOf("test" to definition), areas = mapOf(bot.player.tile.zone.id to setOf(definition)))
        activities["move"] = BotActivity(
            "move",
            requires = listOf(BotInArea("test")),
        )
        manager.reloadGroups()

        manager.add(bot)
        assertTrue(bot.available.contains("move"))

        Movement.move(bot.player, Delta(0, 0, 1))
        manager.updateAvailable(bot)

        assertFalse(bot.available.contains("move"))

        Movement.move(bot.player, Delta(0, 0, -1))
        manager.updateAvailable(bot)

        assertTrue(bot.available.contains("move"))
    }

    @Test
    fun `Variable change evaluates variable requirements`() {
        val bot = createBot()
        activities["quest"] = BotActivity(
            "quest",
            requires = listOf(BotVariable("test", 2, 0)),
        )
        manager.reloadGroups()

        manager.add(bot)
        assertFalse(bot.available.contains("quest"))

        bot.player["test"] = 2
        manager.updateAvailable(bot)

        assertTrue(bot.available.contains("quest"))

        bot.player["test"] = 1
        manager.updateAvailable(bot)

        assertFalse(bot.available.contains("quest"))
    }

    @Test
    fun `Reset timeout on variable produced`() {
        val bot = createBot()
        val activity = BotActivity(
            "quest",
            produces = setOf("variable:quest_complete"),
        )
        val frame = BehaviourFrame(activity, timeout = 100)
        bot.queue(frame)

        bot.player["quest_complete"] = true
        assertEquals(0, frame.timeout)
    }

    @Test
    fun `Reset timeout on item produced`() {
        val bot = createBot()
        ItemDefinitions.set(arrayOf(ItemDefinition()), mapOf("fish" to 0))
        val activity = BotActivity(
            "skilling",
            produces = setOf("item:fish"),
        )
        val frame = BehaviourFrame(activity, timeout = 100)
        bot.queue(frame)

        bot.player.inventory.add("fish")
        assertEquals(0, frame.timeout)
    }

    @Test
    fun `Reset timeout on inventory space produced`() {
        ItemDefinitions.set(arrayOf(ItemDefinition()), mapOf("fish" to 0))
        val bot = createBot()
        bot.player.inventory.add("fish")
        val activity = BotActivity(
            "banking",
            produces = setOf("item:empty"),
        )
        val frame = BehaviourFrame(activity, timeout = 100)
        bot.queue(frame)

        bot.player.inventory.remove("fish")
        assertEquals(0, frame.timeout)
    }

    @Test
    fun `Reset timeout on experience granted`() {
        val bot = createBot()
        val activity = BotActivity(
            "banking",
            produces = setOf("skill:slayer"),
        )
        val frame = BehaviourFrame(activity, timeout = 100)
        bot.queue(frame)

        bot.player.exp(Skill.Slayer, 5.0)
        assertEquals(0, frame.timeout)
    }

    private fun createBot(): Bot {
        val player = Player(tile = Tile(3200, 3200))
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 10))
        player.interfaces = Interfaces(player)
        val bot = Bot(player)
        player["bot"] = bot
        return bot
    }
}
