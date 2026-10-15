package content.bot.behaviour.setup

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.action.BotOpenBank
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.type.Tile
import kotlin.test.*

class BankAccessTest {
    private lateinit var player: Player

    @BeforeEach fun setup() {
        Areas.clear()
        GameObjects.reset()
        player = Player()
        player.interfaces = Interfaces(player)
        player.tile = Tile(1000, 1000)
        player.mode = EmptyMode
        val ids = listOf("bank_booth_test", "bank_chest_duel_arena", "bank_chest_castle_wars", "shantay_chest", "culinaromancers_chest", "fai_varrock_bank_chest", "bank_chest_unusable")
        val options = listOf<Array<String?>>(arrayOf("Use-quickly"), arrayOf("Use", "Bank"), arrayOf("Use"), arrayOf("Open"), arrayOf("Bank"), emptyArray(), arrayOf("Search"))
        ObjectDefinitions.set(ids.mapIndexed { index, id -> ObjectDefinition(id = index, stringId = id, options = options[index]) }.toTypedArray(), ids.withIndex().associate { it.value to it.index })
    }

    @Test fun `bank access uses actual options and excludes unsupported chests`() {
        val expected = mapOf("bank_booth_test" to "Use-quickly", "bank_chest_duel_arena" to "Bank", "bank_chest_castle_wars" to "Use", "shantay_chest" to "Open")
        for ((id, option) in expected) {
            val obj = GameObjects.add(id, player.tile, collision = false)
            assertEquals(option, BankAccess.option(player, obj))
        }
        for (id in listOf("culinaromancers_chest", "fai_varrock_bank_chest", "bank_chest_unusable")) {
            assertNull(BankAccess.option(player, GameObjects.add(id, player.tile, collision = false)))
        }
    }

    @Test fun `open bank sends the correct chest interaction`() {
        for ((id, option) in listOf("bank_chest_duel_arena" to 2, "bank_chest_castle_wars" to 1, "shantay_chest" to 1)) {
            GameObjects.reset()
            GameObjects.add(id, player.tile, collision = false)
            var instruction: InteractObject? = null
            val world = FakeWorld(execute = { _, command -> instruction = command as InteractObject; true })
            assertEquals(BehaviourState.Running, BotOpenBank().update(Bot(player), world, BehaviourFrame(FakeBehaviour())))
            assertEquals(option, instruction?.option)
        }
    }

    @Test fun `no supported bank waits for the normal resolver timeout`() {
        GameObjects.add("bank_chest_unusable", player.tile, collision = false)
        val bot = Bot(player)
        assertEquals(BehaviourState.Running, BotOpenBank().update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour())))
    }

}
