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
import content.bot.behaviour.navigation.NavigationGraph
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
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.type.Tile
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
        Areas.clear()
    }

    @Test
    fun `Combined activities only use normal and oak logs while higher level spots remain woodcutting only`() {
        Settings.load(mapOf("bots.templates" to "templates.toml", "bots.definitions" to "bots.toml", "bots.setups" to "setups.toml"))
        val files = ConfigFiles(
            mapOf(
                "templates.toml" to File("../data/bot").listFiles()!!.filter { it.name.endsWith("templates.toml") }.map { it.path },
                "bots.toml" to listOf(
                    "../data/area/misthalin/lumbridge/lumbridge.bots.toml",
                    "../data/area/misthalin/draynor/draynor.bots.toml",
                    "../data/area/misthalin/varrock/varrock.bots.toml",
                    "../data/area/kandarin/catherby/catherby.bots.toml",
                    "../data/area/kandarin/seers_village/seers_village.bots.toml",
                ),
            ),
        )
        val activities = mutableMapOf<String, BotActivity>()
        loadBehaviours(files, activities, mutableMapOf<String, MutableList<Resolver>>())
        val combined = activities.values.filter { it.actions.any { action -> action is BotFletchLogs } }
        assertEquals(24, combined.size)
        val original = activities.values.filter { it.produces.contains("skill:woodcutting") && !it.produces.contains("skill:fletching") }
        assertEquals(15, original.size)
        for (activity in original) {
            val area = activity.setup.filterIsInstance<BotInArea>().single()
            val higherLevel = activity.produces.any { it in setOf("item:willow_logs", "item:yew_logs", "item:magic_logs") }
            if (higherLevel) {
                assertTrue(combined.none { area in it.setup }, "Unexpected combined activity for ${activity.id}")
            } else {
                assertTrue(combined.any { area in it.setup }, "No combined activity for ${activity.id}")
            }
        }
        for (activity in combined) {
            assertTrue(activity.actions.first() is BotInteractObject)
            val fletching = activity.actions.filterIsInstance<BotFletchLogs>().single()
            assertTrue(fletching.wood in setOf("logs", "oak_logs"))
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
            } else if (fletching.product.endsWith("bow_u")) {
                assertEquals(7, activity.actions.size)
                assertEquals(BotGoTo("draynor_bank"), activity.actions[2])
                val bank = activity.actions[3] as BotInteractObject
                assertEquals("Use-quickly", bank.option)
                assertEquals("bank_booth*", bank.id)
                val deposit = activity.actions[4] as BotInterfaceOption
                assertEquals("Deposit-All", deposit.option)
                assertEquals("bank_side:inventory:${fletching.product}", deposit.id)
                assertTrue(deposit.success != null)
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
    fun `New woodcutting groves connect to their banks in both directions`() {
        Areas.load(
            listOf(
                "../data/area/misthalin/varrock/varrock.areas.toml",
                "../data/area/kandarin/catherby/catherby.areas.toml",
                "../data/area/kandarin/seers_village/seers_village.areas.toml",
            ),
        )
        val graph = NavigationGraph.loadGraph(
            listOf(
                "../data/bot/varrock.nav-edges.toml",
                "../data/bot/catherby.nav-edges.toml",
                "../data/bot/seers_village.nav-edges.toml",
            ),
            emptyList(),
        )
        val routes = listOf(
            Triple(Tile(2783, 3428), "catherby_bank", "catherby_willow_trees"),
            Triple(Tile(2702, 3401), "seers_village_bank", "sorcerers_tower_magic_trees"),
            Triple(Tile(3207, 3506), "varrock_west_bank", "varrock_palace_yew_trees"),
        )
        for ((grove, bank, area) in routes) {
            player.tile = grove
            val path = mutableListOf<Int>()
            assertTrue(graph.find(player, path, bank), "No route from $area to $bank")
            player.tile = graph.endTile(path.last())
            path.clear()
            assertTrue(graph.find(player, path, area), "No route from $bank to $area")
        }
        assertTrue(Tile(2698, 3396) in Areas["sorcerers_tower_magic_trees"])
        assertTrue(Tile(2705, 3398) in Areas["sorcerers_tower_magic_trees"])
        assertTrue(Tile(2781, 3427) in Areas["catherby_willow_trees"])
        assertTrue(Tile(2786, 3429) in Areas["catherby_willow_trees"])
        assertTrue(Tile(3204, 3503) in Areas["varrock_palace_yew_trees"])
        assertTrue(Tile(3221, 3502) in Areas["varrock_palace_yew_trees"])
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
