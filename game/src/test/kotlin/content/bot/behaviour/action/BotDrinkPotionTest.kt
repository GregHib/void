package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.client.instruction.InteractInterface

class BotDrinkPotionTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 4))
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 200, options = arrayOf("Drink")),
                ItemDefinition(id = 201, options = arrayOf("Drink")),
                ItemDefinition(id = 300, options = arrayOf("Wield")),
                ItemDefinition(id = 400, options = arrayOf("Drink")),
                ItemDefinition(id = 500, options = arrayOf("Drink")),
            ),
            mapOf(
                "super_strength_4" to 0,
                "super_strength_3" to 1,
                "whip" to 2,
                "saradomin_brew_4" to 3,
                "super_restore_4" to 4,
            ),
        )
        player.experience.set(Skill.Strength, Level.experience(Skill.Strength, 80))
        player.levels.set(Skill.Strength, 80)
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
    }

    @Test
    fun `Drinks dose when boost is fully decayed`() {
        player.inventory.add("super_strength_4")
        var instruction: InteractInterface? = null
        val world = FakeWorld(execute = { _, ins ->
            instruction = ins as? InteractInterface
            true
        })

        val state = BotDrinkPotion(item = "super_strength_*", skill = Skill.Strength)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
        assertEquals(200, instruction?.itemId)
    }

    @Test
    fun `Skips drinking while boost is still active`() {
        player.inventory.add("super_strength_4")
        player.levels.set(Skill.Strength, 95)
        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        val state = BotDrinkPotion(item = "super_strength_*", skill = Skill.Strength)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }

    @Test
    fun `No-ops when no matching dose remains`() {
        player.inventory.add("whip")
        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        val state = BotDrinkPotion(item = "super_strength_*", skill = Skill.Strength)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }

    @Test
    fun `Skips drinking while drink_delay clock active`() {
        player.inventory.add("super_strength_4")
        player.start("drink_delay", 2)
        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        val state = BotDrinkPotion(item = "super_strength_*", skill = Skill.Strength)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }

    @Test
    fun `Wildcard matches lower-dose variants for re-dosing`() {
        player.inventory.add("super_strength_3")
        var instruction: InteractInterface? = null
        val world = FakeWorld(execute = { _, ins ->
            instruction = ins as? InteractInterface
            true
        })

        val state = BotDrinkPotion(item = "super_strength_*", skill = Skill.Strength)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Wait)
        assertEquals(201, instruction?.itemId)
    }

    @Test
    fun `Drinking saradomin brew increments brew_doses_since_restore`() {
        player.inventory.add("saradomin_brew_4")
        val world = FakeWorld(execute = { _, _ -> true })

        BotDrinkPotion(item = "saradomin_brew_*", skill = Skill.Constitution)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(1, player.get<Int>("brew_doses_since_restore"))
    }

    @Test
    fun `Multiple brew drinks accumulate the counter`() {
        player["brew_doses_since_restore"] = 2
        player.inventory.add("saradomin_brew_4")
        val world = FakeWorld(execute = { _, _ -> true })

        BotDrinkPotion(item = "saradomin_brew_*", skill = Skill.Constitution)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(3, player.get<Int>("brew_doses_since_restore"))
    }

    @Test
    fun `Drinking super_restore zeroes the brew counter`() {
        player["brew_doses_since_restore"] = 4
        player.inventory.add("super_restore_4")
        val world = FakeWorld(execute = { _, _ -> true })

        BotDrinkPotion(item = "super_restore_*", skill = Skill.Strength)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(0, player.get<Int>("brew_doses_since_restore"))
    }

    @Test
    fun `Drinking an unrelated potion leaves the brew counter unchanged`() {
        player["brew_doses_since_restore"] = 2
        player.inventory.add("super_strength_4")
        val world = FakeWorld(execute = { _, _ -> true })

        BotDrinkPotion(item = "super_strength_*", skill = Skill.Strength)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(2, player.get<Int>("brew_doses_since_restore"))
    }
}
