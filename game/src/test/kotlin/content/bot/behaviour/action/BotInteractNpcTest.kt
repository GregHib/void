package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotHasClock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractNPC

class BotInteractNpcTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        NPCs.clear()
        NPCDefinitions.clear()

        player = Player()
        bot = Bot(player)
    }

    @Test
    fun `Success condition returns success`() {
        player.start("done", 10)

        val action = BotInteractNpc(option = "Talk-to", id = "guide", success = BotHasClock("done"))

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Valid npc interaction returns running`() {
        NPCDefinitions.set(
            arrayOf(NPCDefinition(options = arrayOf("Talk-to"))),
            mapOf("guide" to 0),
        )
        NPCs.add("guide", player.tile)
        NPCs.run()

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractNPC
                true
            },
        )

        val action = BotInteractNpc("Talk-to", "guide")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Execution failure returns failed`() {
        NPCDefinitions.set(
            arrayOf(NPCDefinition(options = arrayOf("Talk-to"))),
            mapOf("guide" to 0),
        )
        NPCs.add("guide", player.tile)
        NPCs.run()

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotInteractNpc("Talk-to", "guide")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `No target without success returns failed`() {
        val action = BotInteractNpc("Talk-to", "guide")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoTarget), state)
    }
}
