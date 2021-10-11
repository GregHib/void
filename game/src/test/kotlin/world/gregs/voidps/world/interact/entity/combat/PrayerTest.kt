package world.gregs.voidps.world.interact.entity.combat

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
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
        every { get<EnumDecoder>().get(2279) } answers { // regular prayers enum
            EnumDefinition(id = arg(0), map = HashMap((0 until 30).associateWith { it }))
        }
        every { get<StructDecoder>().get(27) } answers { // piety prayer information
            StructDefinition(arg(0), params = HashMap(mapOf(734L to "<br>Piety<br>")))
        }
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
        every { get<EnumDecoder>().get(863) } answers { // curse prayers enum
            EnumDefinition(id = arg(0), map = HashMap((0 until 20).associateWith { it }))
        }
        every { get<StructDecoder>().get(19) } answers { // turmoil prayer information
            StructDefinition(arg(0), params = HashMap(mapOf(734L to "<br>Turmoil<br>")))
        }

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