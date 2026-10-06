package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.client.instruction.InteractInterface
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BotDropItemsTest {
    private lateinit var player: Player
    private lateinit var bot: Bot
    private val frame = BehaviourFrame(FakeBehaviour())

    @BeforeEach
    fun setup() {
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 0, options = arrayOf(null, null, null, null, "Drop")),
                ItemDefinition(id = 1, stackable = 1, options = arrayOf(null, null, null, null, "Drop")),
                ItemDefinition(id = 2),
                ItemDefinition(id = 3),
            ),
            mapOf("shortbow_u" to 0, "arrow_shaft" to 1, "knife" to 2, "steel_hatchet" to 3),
        )
        InterfaceDefinitions.set(
            arrayOf(
                InterfaceDefinition(
                    id = 0,
                    components = mutableMapOf(0 to InterfaceComponentDefinition(id = 0, params = mapOf(Params.INVENTORY to "inventory"))),
                ),
            ),
            mapOf("inventory" to 0),
            mapOf("inventory:inventory" to 0),
        )
        player = Player()
        bot = Bot(player)
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 28))
    }

    @Test
    fun `Drops all non-stackable products through client instructions and preserves tools`() {
        player.inventory.add("knife")
        player.inventory.add("steel_hatchet")
        player.inventory.add("shortbow_u", 3)
        val action = BotDropItems("shortbow_u", keepStackable = true)
        var drops = 0
        val world = FakeWorld(execute = { _, instruction ->
            val interaction = instruction as InteractInterface
            assertEquals(4, interaction.option)
            assertEquals("shortbow_u", player.inventory[interaction.itemSlot].id)
            player.inventory.remove("shortbow_u")
            drops++
            true
        })
        repeat(3) {
            assertEquals(BehaviourState.Wait(1, BehaviourState.Running), action.update(bot, world, frame))
        }
        assertEquals(BehaviourState.Success, action.update(bot, world, frame))
        assertEquals(3, drops)
        assertEquals(1, player.inventory.count("knife"))
        assertEquals(1, player.inventory.count("steel_hatchet"))
    }

    @Test
    fun `Retains stackable products without dispatching a drop`() {
        player.inventory.add("arrow_shaft", 1000)
        val world = FakeWorld(execute = { _, _ -> error("Stackable products must be retained") })
        assertEquals(BehaviourState.Success, BotDropItems("arrow_shaft", keepStackable = true).update(bot, world, frame))
        assertEquals(1000, player.inventory.count("arrow_shaft"))
    }

    @Test
    fun `Absent product succeeds without dispatching a drop`() {
        assertEquals(BehaviourState.Success, BotDropItems("shortbow_u").update(bot, FakeWorld(), frame))
    }

    @Test
    fun `Rejected drop fails and leaves the item in inventory`() {
        player.inventory.add("shortbow_u")
        assertTrue(BotDropItems("shortbow_u").update(bot, FakeWorld(), frame) is BehaviourState.Failed)
        assertEquals(1, player.inventory.count("shortbow_u"))
    }

    @Test
    fun `Parser preserves the stack-retention option`() {
        val actions = ActionParser.parse(listOf("drop_items" to mapOf("id" to "arrow_shaft", "keep_stackable" to true)), "test")
        assertEquals(BotDropItems("arrow_shaft", true), actions.single())
    }
}
