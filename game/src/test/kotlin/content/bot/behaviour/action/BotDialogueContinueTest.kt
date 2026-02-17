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
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractDialogue

class BotDialogueContinueTest {

    @BeforeEach
    fun setup() {
        InterfaceDefinitions.clear()
    }

    @Test
    fun `Success condition returns success`() {
        val player = Player()
        val bot = Bot(player)

        InterfaceDefinitions.set(
            arrayOf(
                InterfaceDefinition(
                    id = 0,
                    type = "main_screen",
                    components = mutableMapOf(
                        0 to InterfaceComponentDefinition(id = 0, options = arrayOf("Yes", "No")),
                    ),
                ),
            ),
            mapOf("test" to 0),
            mapOf("test:component" to 0),
        )

        player.start("test", 10)
        val action = BotDialogueContinue(
            option = "Yes",
            id = "test:component",
            success = Condition.Clock("test"),
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Invalid id format returns failed`() {
        val player = Player()
        val bot = Bot(player)

        val action = BotDialogueContinue(
            option = "Yes",
            id = "invalid",
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(
            BehaviourState.Failed(Reason.Invalid("Invalid interface id 'invalid'.")),
            state,
        )
    }

    @Test
    fun `Invalid interface id returns failed`() {
        val player = Player()
        val bot = Bot(player)

        val action = BotDialogueContinue(
            option = "Yes",
            id = "missing:component",
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(
            BehaviourState.Failed(Reason.Invalid("Invalid interface id missing:component:null.")),
            state,
        )
    }

    @Test
    fun `Invalid component returns failed`() {
        val player = Player()
        val bot = Bot(player)

        InterfaceDefinitions.set(
            arrayOf(InterfaceDefinition(type = "main_screen", id = 100)),
            mapOf("test" to 100),
            emptyMap(),
        )

        val action = BotDialogueContinue(
            option = "Yes",
            id = "test:missing",
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Fails if execution invalid`() {
        val player = Player()
        val bot = Bot(player)

        InterfaceDefinitions.set(
            arrayOf(
                InterfaceDefinition(
                    id = 100,
                    type = "main_screen",
                    components = mutableMapOf(
                        1 to InterfaceComponentDefinition(
                            id = 1,
                            options = arrayOf("Yes", "No"),
                        ),
                    ),
                ),
            ),
            mapOf("test" to 0),
            mapOf("component" to 1),
        )

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotDialogueContinue(
            option = "Yes",
            id = "test:component",
        )

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Successful execution returns wait success`() {
        val player = Player()
        val bot = Bot(player)

        InterfaceDefinitions.set(
            arrayOf(
                InterfaceDefinition(
                    id = 100,
                    type = "main_screen",
                    components = mutableMapOf(
                        1 to InterfaceComponentDefinition(
                            id = 1,
                            options = arrayOf("Yes", "No"),
                        ),
                    ),
                ),
            ),
            mapOf("test" to 0),
            mapOf("test:component" to 1),
        )

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractDialogue
                true
            },
        )

        val action = BotDialogueContinue(
            option = "Yes",
            id = "test:component",
        )

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Wait(1, BehaviourState.Success), state)
        assertTrue(called)
    }
}
