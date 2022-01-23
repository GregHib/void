package world.gregs.voidps.world.interact.entity.combat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption

internal class PrayerTest : WorldMock() {

    private lateinit var floorItems: FloorItems

    @BeforeEach
    fun start() {
        floorItems = get()
    }

    @Test
    fun `Active prayers drain prayer points`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player")
        player.experience.set(Skill.Prayer, Double.MAX_VALUE)

        player.interfaceOption("prayer_list", "regular_prayers", optionIndex = 0, slot = 27)
        assertTrue(player.hasEffect("prayer_piety"))
        tickIf { player.levels.get(Skill.Prayer) > 0 }

        assertEquals(0, player.levels.get(Skill.Prayer))
        assertFalse(player.hasEffect("prayer_piety"))
    }

    @Test
    fun `Active curses drain prayer points`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player")
        player.experience.set(Skill.Prayer, Double.MAX_VALUE)
        player.setVar(PrayerConfigs.PRAYERS, "curses")

        player.interfaceOption("prayer_list", "regular_prayers", optionIndex = 0, slot = 19)
        assertTrue(player.hasEffect("prayer_turmoil"))
        tickIf { player.levels.get(Skill.Prayer) > 0 }

        assertEquals(0, player.levels.get(Skill.Prayer))
        assertFalse(player.hasEffect("prayer_turmoil"))
    }

}