package content.skill.magic.book.lunar

import WorldTest
import containsMessage
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HumidifyTest : WorldTest() {

    @Test
    fun `Fill all empty containers in one cast`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 68)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("water_rune", 3)
        player.inventory.add("fire_rune")
        player.inventory.add("vial", 2)
        player.inventory.add("bucket")
        player.inventory.add("waterskin_0")

        player.interfaceOption("lunar_spellbook", "humidify", "Cast")
        tick(3)

        assertEquals(2, player.inventory.count("vial_of_water"))
        assertEquals(1, player.inventory.count("bucket_of_water"))
        assertEquals(1, player.inventory.count("waterskin_4"))
        assertEquals(0, player.inventory.count("vial"))
        assertEquals(65.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't cast with nothing to fill`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 68)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("water_rune", 3)
        player.inventory.add("fire_rune")

        player.interfaceOption("lunar_spellbook", "humidify", "Cast")
        tick(3)

        assertTrue(player.containsMessage("You have no empty containers to fill."))
        assertEquals(1, player.inventory.count("astral_rune"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
