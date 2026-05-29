package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

class BotEatFoodTest {

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
                ItemDefinition(id = 100, options = arrayOf("Eat"), stringId = "shark"),
            ),
            mapOf(
                "shark" to 0,
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
    fun `Eat food when below heal percentage`() {
        player.inventory.add("shark")
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 99))
        player.levels.set(Skill.Constitution, 99)
        player.levels.drain(Skill.Constitution, multiplier = 0.6)
        var instruction: InteractInterface? = null
        val world = FakeWorld(execute = { _, ins ->
            instruction = ins as? InteractInterface
            true
        })

        val state = BotEatFood(50)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
        assertEquals(100, instruction?.itemId)
    }

    @Test
    fun `No-ops when near to full hp`() {
        player.inventory.add("whip")
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 99))
        player.levels.set(Skill.Constitution, 99)
        player.levels.drain(Skill.Constitution, multiplier = 0.1)
        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        val state = BotEatFood(60)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }

    @Test
    fun `Skips when just_ate_food clock active`() {
        player.inventory.add("shark")
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 99))
        player.levels.set(Skill.Constitution, 99)
        player.levels.drain(Skill.Constitution, multiplier = 0.6)
        player.start("just_ate_food", 2)
        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        val state = BotEatFood(50)
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }
}
