package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotAlliesOnTile
import content.skill.prayer.PrayerConfigs
import content.skill.prayer.isCurses
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.data.config.PrayerDefinition
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels

class BotPrayTest {

    private lateinit var bot: Bot
    private lateinit var player: Player
    private lateinit var prayerDefinitions: PrayerDefinitions

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))

        VariableDefinitions.set(
            mapOf(
                PrayerConfigs.ACTIVE_PRAYERS to VariableDefinition.VarbitDefinition(
                    id = 0,
                    values = BitwiseValues(listOf("protect_from_melee", "piety")),
                    default = null,
                    persistent = false,
                    transmit = false,
                ),
                PrayerConfigs.ACTIVE_CURSES to VariableDefinition.VarbitDefinition(
                    id = 1,
                    values = BitwiseValues(listOf("turmoil", "soul_split", "berserker", "deflect_melee")),
                    default = null,
                    persistent = false,
                    transmit = false,
                ),
                PrayerConfigs.PRAYERS to VariableDefinition.CustomVariableDefinition(
                    values = world.gregs.voidps.engine.client.variable.StringValues,
                    default = "normal",
                    persistent = false,
                ),
            ),
        )

        prayerDefinitions = PrayerDefinitions().apply {
            definitions = mapOf(
                "protect_from_melee" to PrayerDefinition(index = 17, level = 43, stringId = "protect_from_melee"),
                "piety" to PrayerDefinition(index = 25, level = 70, stringId = "piety"),
                "turmoil" to PrayerDefinition(index = 19, level = 95, isCurse = true, stringId = "turmoil"),
                "deflect_melee" to PrayerDefinition(index = 9, level = 71, isCurse = true, stringId = "deflect_melee"),
            )
        }
        startKoin {
            modules(
                module {
                    single { prayerDefinitions }
                },
            )
        }
    }

    @AfterEach
    fun stop() {
        stopKoin()
        VariableDefinitions.clear()
    }

    @Test
    fun `Activates prayer and returns success`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)

        val state = BotPray("protect_from_melee").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertTrue(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Already active prayer short-circuits to success`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee", refresh = false)

        val state = BotPray("protect_from_melee").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Insufficient level fails`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 40))
        player.levels.set(Skill.Prayer, 40)

        val state = BotPray("protect_from_melee").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
        assertTrue((state as BehaviourState.Failed).reason is Reason.Invalid)
    }

    @Test
    fun `No prayer points returns success silently to avoid spamming reactive failures`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)
        player.levels.drain(Skill.Prayer, 70)

        val state = BotPray("protect_from_melee").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Unknown prayer id fails`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 99))
        player.levels.set(Skill.Prayer, 99)

        val state = BotPray("not_a_prayer").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `Gated condition true activates prayer`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)

        val state = BotPray("protect_from_melee", BotAlliesOnTile(min = 0)).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertTrue(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Gated condition false deactivates prayer when on`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee", refresh = false)

        val state = BotPray("protect_from_melee", BotAlliesOnTile(min = 5)).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Gated condition false leaves prayer off`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)

        val state = BotPray("protect_from_melee", BotAlliesOnTile(min = 5)).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Gated no prayer points returns success silently`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)
        player.levels.drain(Skill.Prayer, 70)

        val state = BotPray("protect_from_melee", BotAlliesOnTile(min = 0)).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Curse activation switches prayer book and writes to active curses`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 95))
        player.levels.set(Skill.Prayer, 95)

        val state = BotPray("turmoil").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertEquals("curses", player.get(PrayerConfigs.PRAYERS, ""))
        assertTrue(player.containsVarbit(PrayerConfigs.ACTIVE_CURSES, "turmoil"))
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "turmoil"))
    }

    @Test
    fun `Normal prayer in curses mode flips back to normal book`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)
        player[PrayerConfigs.PRAYERS] = "curses"

        val state = BotPray("piety").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(player.isCurses())
        assertTrue(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "piety"))
    }

    @Test
    fun `Insufficient curse level still switches book then fails`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 80))
        player.levels.set(Skill.Prayer, 80)

        val state = BotPray("turmoil").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
        assertEquals("curses", player.get(PrayerConfigs.PRAYERS, ""))
    }
}
