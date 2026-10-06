package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.loadBehaviours
import content.bot.behaviour.setup.Resolver
import content.skill.fletching.fletchableProducts
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BotFletchLogsTest {
    private lateinit var player: Player
    private lateinit var bot: Bot
    private val frame = BehaviourFrame(FakeBehaviour())

    @BeforeEach
    fun setup() {
        Settings.clear()
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 0), ItemDefinition(id = 1), ItemDefinition(id = 2)),
            mapOf("knife" to 0, "oak_logs" to 1, "logs" to 2),
        )
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 28))
        player.levels.set(Skill.Fletching, 25)
        mockkObject(Tables)
        every { Tables.itemListOrNull("fletchables.oak_logs.products") } returns listOf("arrow_shaft", "oak_shortbow_u", "oak_longbow_u", "oak_stock")
        every { Tables.itemListOrNull("fletchables.logs.products") } returns listOf("arrow_shaft", "shortbow_u", "longbow_u", "wooden_stock")
        every { Tables.intOrNull("fletching_unf.oak_shortbow_u.level") } returns 20
    }

    @AfterEach
    fun cleanup() {
        unmockkObject(Tables)
        Settings.clear()
        ItemDefinitions.clear()
    }

    @Test
    fun `Combined activities load at every existing woodcutting spot without requiring banked logs`() {
        Settings.load(mapOf("bots.templates" to "templates.toml", "bots.definitions" to "bots.toml", "bots.setups" to "setups.toml"))
        val files = ConfigFiles(
            mapOf(
                "templates.toml" to File("../data/bot").listFiles()!!.filter { it.name.endsWith("templates.toml") }.map { it.path },
                "bots.toml" to listOf("../data/area/misthalin/lumbridge/lumbridge.bots.toml", "../data/area/misthalin/draynor/draynor.bots.toml"),
            ),
        )
        val activities = mutableMapOf<String, BotActivity>()
        loadBehaviours(files, activities, mutableMapOf<String, MutableList<Resolver>>())
        val combined = activities.values.filter { it.actions.any { action -> action is BotFletchLogs } }
        assertEquals(30, combined.size)
        val original = activities.values.filter { it.produces.contains("skill:woodcutting") && !it.produces.contains("skill:fletching") }
        assertEquals(12, original.size)
        for (activity in original) {
            val area = activity.setup.filterIsInstance<BotInArea>().single()
            assertTrue(combined.any { area in it.setup }, "No combined activity for ${activity.id}")
        }
        for (activity in combined) {
            assertTrue(activity.actions.first() is BotInteractObject)
            val fletching = activity.actions.filterIsInstance<BotFletchLogs>().single()
            if (activity.id.startsWith("lumbridge_") && fletching.product.endsWith("bow_u")) {
                assertEquals(7, activity.actions.size)
                assertEquals(BotGoTo("lumbridge_general_store"), activity.actions[2])
                val trade = activity.actions[3] as BotInteractNpc
                assertEquals("Trade", trade.option)
                assertEquals("shopkeeper_lumbridge,shop_assistant_lumbridge", trade.id)
                val sell = activity.actions[4] as BotInterfaceOption
                assertEquals("Sell 50", sell.option)
                assertEquals("shop_side:inventory:${fletching.product}", sell.id)
                assertTrue(sell.success != null)
                assertTrue(activity.actions[5] === BotCloseInterface)
                val area = activity.setup.filterIsInstance<BotInArea>().single()
                assertEquals(BotGoTo(area.id), activity.actions.last())
                assertTrue(activity.actions.none { it is BotDropItems })
            } else {
                assertTrue((activity.actions.last() as BotDropItems).keepStackable)
            }
            assertTrue(activity.produces.containsAll(setOf("skill:woodcutting", "skill:fletching")))
            val inventory = activity.setup.filterIsInstance<BotInventorySetup>().single()
            assertEquals(1, inventory.items.single { "empty" in it.ids }.min)
            assertTrue(inventory.items.any { "knife" in it.ids })
        }
    }

    @Test
    fun `Menu respects the arrow shaft setting without mutating table products`() {
        assertEquals(listOf("oak_shortbow_u", "oak_longbow_u", "oak_stock"), fletchableProducts("oak_logs"))
        assertEquals("arrow_shaft", fletchableProducts("logs").first())
        Settings.load(mapOf("fletching.moreArrowShafts" to "true"))
        assertEquals("arrow_shaft", fletchableProducts("oak_logs").first())
    }

    @Test
    fun `Oak bow chooses first menu entry when extra shafts are disabled`() {
        player.inventory.add("knife")
        player.inventory.add("oak_logs", 26)
        assertEquals(BehaviourState.Running, BotFletchLogs("oak_logs", "oak_shortbow_u").update(bot, FakeWorld(), frame))
        val actions = bot.frames.peek().behaviour.actions
        assertEquals("dialogue_skill_creation:choice1", (actions[2] as BotDialogueContinue).id)
        val restart = actions[3] as BotRestart
        assertEquals(BehaviourState.Running, restart.update(bot, FakeWorld(), bot.frames.peek()))
        assertEquals(0, bot.frames.peek().index)
        assertTrue(actions.first() is BotItemOnItem)
    }

    @Test
    fun `Oak bow chooses second menu entry when extra shafts are enabled`() {
        Settings.load(mapOf("fletching.moreArrowShafts" to "true"))
        player.inventory.add("knife")
        player.inventory.add("oak_logs")
        BotFletchLogs("oak_logs", "oak_shortbow_u").update(bot, FakeWorld(), frame)
        assertEquals("dialogue_skill_creation:choice2", (bot.frames.peek().behaviour.actions[2] as BotDialogueContinue).id)
    }

    @Test
    fun `Disabled shaft product fails without queuing interactions`() {
        player.inventory.add("knife")
        player.inventory.add("oak_logs")
        assertTrue(BotFletchLogs("oak_logs", "arrow_shaft").update(bot, FakeWorld(), frame) is BehaviourState.Failed)
        assertTrue(bot.frames.isEmpty())
    }

    @Test
    fun `Insufficient level fails without queuing interactions`() {
        player.levels.set(Skill.Fletching, 1)
        player.inventory.add("knife")
        player.inventory.add("oak_logs")
        assertTrue(BotFletchLogs("oak_logs", "oak_shortbow_u").update(bot, FakeWorld(), frame) is BehaviourState.Failed)
        assertTrue(bot.frames.isEmpty())
    }

    @Test
    fun `Missing knife fails before opening the menu`() {
        player.inventory.add("oak_logs")
        assertTrue(BotFletchLogs("oak_logs", "oak_shortbow_u").update(bot, FakeWorld(), frame) is BehaviourState.Failed)
        assertTrue(bot.frames.isEmpty())
    }

    @Test
    fun `Finished batch succeeds without opening another menu`() {
        assertEquals(BehaviourState.Success, BotFletchLogs("oak_logs", "oak_shortbow_u").update(bot, FakeWorld(), frame))
        assertTrue(bot.frames.isEmpty())
    }
}
