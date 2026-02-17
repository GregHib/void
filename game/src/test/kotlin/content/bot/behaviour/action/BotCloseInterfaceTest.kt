package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player

class BotCloseInterfaceTest {
    @Test
    fun `No menu open is success`() {
        val action = BotCloseInterface
        val player = Player()
        player.interfaces = Interfaces(player)
        val bot = Bot(player)
        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Executes interface close if menu is open`() {
        val action = BotCloseInterface
        val player = Player()
        player.interfaces = Interfaces(player)
        val bot = Bot(player)
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(type = "main_screen")), mapOf("menu" to 0), emptyMap())
        assertTrue(player.interfaces.open("menu"))
        var called = true
        val world = FakeWorld(
            execute = { _, _ ->
                called = true
                true
            },
        )
        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Fails if validation unsuccessful`() {
        val action = BotCloseInterface
        val player = Player()
        player.interfaces = Interfaces(player)
        val bot = Bot(player)
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(type = "main_screen")), mapOf("menu" to 0), emptyMap())
        assertTrue(player.interfaces.open("menu"))
        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))
        assertEquals(BehaviourState.Failed(Reason.Invalid("Failed to close interface")), state)
    }
}
