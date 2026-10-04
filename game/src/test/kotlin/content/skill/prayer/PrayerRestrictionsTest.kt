package content.skill.prayer

import WorldTest
import content.skill.prayer.list.QuickPrayers
import interfaceOption
import messages
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

class PrayerRestrictionsTest : WorldTest() {
    @Test
    fun `Entering restricted area clears prayers curses and quick prayers`() {
        val player = createPlayer(Tile(2558, 3443))
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "thick_skin")
        player.addVarbit(PrayerConfigs.ACTIVE_CURSES, "protect_item")
        player[PrayerConfigs.USING_QUICK_PRAYERS] = true
        player.tele(2555, 9844)
        tick()
        assertFalse(player.praying("thick_skin"))
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_CURSES, "protect_item"))
        assertFalse(player[PrayerConfigs.USING_QUICK_PRAYERS, false])
    }

    @Test
    fun `Regular prayers and curses show area restriction message`() {
        val player = createPlayer(Tile(2555, 9844))
        val prayers = scripts.filterIsInstance<QuickPrayers>().single()
        with(prayers) { player.togglePrayer(0, PrayerConfigs.ACTIVE_PRAYERS, false) }
        assertFalse(player.praying("thick_skin"))
        assertEquals("Prayers don't seem to work here.", player.messages.last())
        player[PrayerConfigs.PRAYERS] = "curses"
        with(prayers) { player.togglePrayer(0, PrayerConfigs.ACTIVE_CURSES, false) }
        assertFalse(player.containsVarbit(PrayerConfigs.ACTIVE_CURSES, "protect_item"))
        assertEquals("Prayers don't seem to work here.", player.messages.last())
    }

    @Test
    fun `Quick prayers are blocked but can still be selected`() {
        val player = createPlayer(Tile(2555, 9844))
        val prayers = scripts.filterIsInstance<QuickPrayers>().single()
        with(prayers) { player.togglePrayer(0, PrayerConfigs.QUICK_PRAYERS, true) }
        assertTrue(player.containsVarbit(PrayerConfigs.QUICK_PRAYERS, "thick_skin"))
        player.interfaceOption("prayer_orb", "orb", "Turn Quick Prayers On")
        tick()
        assertFalse(player[PrayerConfigs.USING_QUICK_PRAYERS, false])
        assertFalse(player.praying("thick_skin"))
        assertEquals("Prayers don't seem to work here.", player.messages.last())
    }

    @Test
    fun `Restriction ends outside the area and does not affect other floors`() {
        val player = createPlayer(Tile(2555, 9844))
        assertTrue(player.prayersBlocked())
        player.tele(2555, 9844, 1)
        assertFalse(player.prayersBlocked())
        player.tele(2558, 3443)
        with(scripts.filterIsInstance<QuickPrayers>().single()) {
            player.togglePrayer(0, PrayerConfigs.ACTIVE_PRAYERS, false)
        }
        assertTrue(player.praying("thick_skin"))
    }
}
