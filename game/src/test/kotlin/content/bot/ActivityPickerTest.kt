package content.bot

import FakeRandom
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.activity.RandomPicker
import content.bot.behaviour.activity.RepeatPreviousPicker
import content.bot.behaviour.activity.SameSkillPicker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.setRandom

class ActivityPickerTest {

    private val mining = BotActivity("mining", produces = setOf("skill:mining"))
    private val copper = BotActivity("copper", produces = setOf("skill:mining"))
    private val fishing = BotActivity("fishing", produces = setOf("skill:fishing"))
    private val idle = BotActivity("idle")
    private val all = listOf(mining, copper, fishing).associateBy { it.id }
    private var unavailable = emptySet<String>()
    private fun eligible(activity: BotActivity?) = activity != null && activity.id !in unavailable

    private var roll = 0

    @BeforeEach
    fun setup() {
        roll = 0
        Settings.load(mapOf("bots.sameSkillChance" to "100"))
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = roll.coerceAtMost(until - 1)
        })
    }

    @AfterEach
    fun teardown() {
        Settings.clear()
    }

    private fun bot(last: BotActivity? = null) = Bot(Player(accountName = "bot")).also {
        it.available.addAll(all.keys)
        it.lastSuccessful = last
    }

    @Test
    fun `Same skill picker favours activities training the last successful skill`() {
        val bot = bot(last = fishing)

        assertEquals(fishing, SameSkillPicker.pick(bot, all, idle, ::eligible))
    }

    @Test
    fun `Same skill picker skips unavailable activities`() {
        unavailable = setOf("fishing")

        assertNull(SameSkillPicker.pick(bot(last = fishing), all, idle, ::eligible))
    }

    @Test
    fun `Same skill picker defers when chance fails`() {
        roll = 50
        Settings.load(mapOf("bots.sameSkillChance" to "50"))

        assertNull(SameSkillPicker.pick(bot(last = mining), all, idle, ::eligible))
    }

    @Test
    fun `Same skill picker defers without a previous success`() {
        assertNull(SameSkillPicker.pick(bot(), all, idle, ::eligible))
    }

    @Test
    fun `Same skill picker defers for activities without a skill`() {
        assertNull(SameSkillPicker.pick(bot(last = idle), all, idle, ::eligible))
    }

    @Test
    fun `Repeat previous picker ignores idle and blocked activities`() {
        val bot = bot()
        bot.previous = idle
        assertNull(RepeatPreviousPicker.pick(bot, all, idle, ::eligible))
        bot.previous = mining
        assertEquals(mining, RepeatPreviousPicker.pick(bot, all, idle, ::eligible))
        unavailable = setOf("mining")
        assertNull(RepeatPreviousPicker.pick(bot, all, idle, ::eligible))
    }

    @Test
    fun `Random picker only picks activities with requirements met`() {
        unavailable = setOf("mining", "copper")

        assertEquals(fishing, RandomPicker.pick(bot(), all, idle, ::eligible))
    }
}
