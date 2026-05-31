package content.bot.behaviour.action

import WorldTest
import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotSkillLevel
import content.skill.prayer.PrayerConfigs
import content.skill.prayer.isCurses
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels

class BotPrayTest : WorldTest() {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = createPlayer()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
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

        val state = BotPray("protect_from_melee", BotSkillLevel(Skill.Prayer, min = 1)).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertTrue(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Gated condition false deactivates prayer when on`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee", refresh = false)

        val state = BotPray("protect_from_melee", BotSkillLevel(Skill.Prayer, min = 9999)).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Gated condition false leaves prayer off`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)

        val state = BotPray("protect_from_melee", BotSkillLevel(Skill.Prayer, min = 9999)).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Gated no prayer points returns success silently`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        player.levels.set(Skill.Prayer, 70)
        player.levels.drain(Skill.Prayer, 70)

        val state = BotPray("protect_from_melee", BotSkillLevel(Skill.Prayer, min = 1)).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_PRAYERS, "protect_from_melee"))
    }

    @Test
    fun `Curse activation switches prayer book and writes to active curses`() {
        player.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 95))
        player.levels.set(Skill.Prayer, 95)

        val state = BotPray("turmoil").update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertEquals("curses", player[PrayerConfigs.PRAYERS, ""])
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
