package content.skill.magic.book.lunar

import WorldTest
import interfaceOption
import itemOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HunterKitTest : WorldTest() {

    @Test
    fun `Cast hunter kit creates a kit`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 71)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("earth_rune", 2)

        player.interfaceOption("lunar_spellbook", "hunter_kit", "Cast")
        tick(3)

        assertTrue(player.inventory.contains("hunter_kit"))
        assertEquals(70.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Open hunter kit unpacks tools`() {
        val player = createPlayer()
        player.inventory.add("hunter_kit")

        player.itemOption("Open", "hunter_kit")
        tick(2)

        assertTrue(player.inventory.contains("noose_wand"))
        assertTrue(player.inventory.contains("butterfly_net"))
        assertTrue(player.inventory.contains("bird_snare"))
        assertTrue(player.inventory.contains("rabbit_snare"))
        assertTrue(player.inventory.contains("teasing_stick"))
        assertTrue(player.inventory.contains("unlit_torch"))
        assertTrue(player.inventory.contains("box_trap"))
        assertEquals(0, player.inventory.count("hunter_kit"))
    }
}
