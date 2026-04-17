package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.condition.BotAlliesOnTile
import content.bot.behaviour.perception.BotCombatContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

class BotRepositionTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        setRandom(Random(42))
        player = Player(tile = Tile(3000, 3000))
        bot = Bot(player)
        player["bot"] = bot
    }

    @AfterEach
    fun teardown() {
        setRandom(Random.Default)
    }

    private fun ally(tile: Tile): Player = Player(tile = tile)

    @Test
    fun `Alone short-circuits to success without walking`() {
        bot.combatContext = BotCombatContext.EMPTY.copy(nearbyAllies = emptyList())

        var dispatched: Walk? = null
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is Walk) dispatched = instruction
                true
            },
        )

        val state = BotReposition().start(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertNull(dispatched)
    }

    @Test
    fun `Crowded tile walks to empty neighbor`() {
        val origin = player.tile
        bot.combatContext = BotCombatContext.EMPTY.copy(
            nearbyAllies = listOf(ally(origin), ally(origin), ally(origin)),
        )

        var dispatched: Walk? = null
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is Walk) dispatched = instruction
                true
            },
        )

        val state = BotReposition(radius = 1).start(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        val walk = dispatched
        assertNotNull(walk)
        val dx = walk!!.x - origin.x
        val dy = walk.y - origin.y
        assertTrue(dx in -1..1 && dy in -1..1 && !(dx == 0 && dy == 0))
    }

    @Test
    fun `Gated off when condition false`() {
        val origin = player.tile
        bot.combatContext = BotCombatContext.EMPTY.copy(
            nearbyAllies = listOf(ally(origin)),
        )

        var dispatched: Walk? = null
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is Walk) dispatched = instruction
                true
            },
        )
        // Condition demands ≥3 allies on tile — only 1 present → gate closes.
        val action = BotReposition(radius = 1, condition = BotAlliesOnTile(min = 3))

        val state = action.start(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertNull(dispatched)
    }

    @Test
    fun `Prefers tile with fewest allies`() {
        val origin = player.tile
        val busyNeighbor = origin.add(1, 0)
        // Crowd origin heavily, also crowd one neighbor.
        // Only one neighbor tile has zero allies: origin.add(-1, 0).
        val allies = mutableListOf<Player>()
        repeat(3) { allies.add(ally(origin)) }
        repeat(2) { allies.add(ally(busyNeighbor)) }
        for (dx in -1..1) for (dy in -1..1) {
            if (dx == 0 && dy == 0) continue
            if (dx == 1 && dy == 0) continue
            if (dx == -1 && dy == 0) continue
            allies.add(ally(origin.add(dx, dy)))
        }
        bot.combatContext = BotCombatContext.EMPTY.copy(nearbyAllies = allies)

        var dispatched: Walk? = null
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is Walk) dispatched = instruction
                true
            },
        )

        val state = BotReposition(radius = 1).start(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        val walk = dispatched
        assertNotNull(walk)
        assertEquals(origin.x - 1, walk!!.x)
        assertEquals(origin.y, walk.y)
    }

    @Test
    fun `Update returns success when tile is clear`() {
        bot.combatContext = BotCombatContext.EMPTY.copy(nearbyAllies = emptyList())

        val state = BotReposition().update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Five bots redistribute from one tile to multiple tiles`() {
        // Simulate: 5 bots all at the same tile. Each bot perceives the other 4 as allies
        // on its own tile, picks a random min-count neighbor, and steps there.
        // Acceptance: ≥3 distinct target tiles chosen across the 5 bots.
        val origin = Tile(3000, 3000)
        val bots = (0 until 5).map { Player(tile = origin) }
        val chosen = mutableListOf<Tile>()
        for (p in bots) {
            val b = Bot(p)
            p["bot"] = b
            val others = bots.filter { it !== p }
            b.combatContext = BotCombatContext.EMPTY.copy(nearbyAllies = others)
            var target: Tile? = null
            val world = FakeWorld(
                execute = { _, instruction ->
                    if (instruction is Walk) target = Tile(instruction.x, instruction.y, origin.level)
                    true
                },
            )
            val state = BotReposition(radius = 1).start(b, world, BehaviourFrame(FakeBehaviour()))
            assertEquals(BehaviourState.Running, state)
            assertNotNull(target)
            chosen.add(target!!)
        }
        val distinct = chosen.map { it.id }.toSet().size
        assertTrue(distinct >= 3, "Expected ≥3 distinct target tiles, got $distinct: $chosen")
    }
}
