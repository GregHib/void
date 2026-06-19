package content.skill.magic.book.lunar

import WorldTest
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NPCContactTest : WorldTest() {

    @Test
    fun `Contact a slayer master starts their dialogue`() {
        val player = createPlayer(Tile(100, 100))
        player.levels.set(Skill.Magic, 67)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("cosmic_rune")
        player.inventory.add("air_rune", 2)
        createNPC("turael", Tile(3000, 3000))

        player.interfaceOption("lunar_spellbook", "npc_contact", "Cast")
        tick(3)
        assertTrue(player.hasOpen("npc_contact"))
        assertEquals(63.0, player.experience.get(Skill.Magic))

        player.interfaceOption("npc_contact", "turael_head", "Turael")
        tick(5)

        assertFalse(player.hasOpen("npc_contact"))
        assertNotNull(player.dialogue)
        assertEquals(63.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Contacting Duradel reaches Lapalok his stand-in`() {
        val player = createPlayer(Tile(100, 100))
        player.levels.set(Skill.Magic, 67)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("cosmic_rune")
        player.inventory.add("air_rune", 2)
        createNPC("lapalok_shilo_village", Tile(3000, 3000))

        player.interfaceOption("lunar_spellbook", "npc_contact", "Cast")
        tick(3)
        player.interfaceOption("npc_contact", "duradel_head", "Duradel")
        tick(5)

        assertNotNull(player.dialogue)
        assertEquals(63.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Closing the interface after casting starts no dialogue`() {
        val player = createPlayer(Tile(100, 100))
        player.levels.set(Skill.Magic, 67)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("cosmic_rune")
        player.inventory.add("air_rune", 2)

        player.interfaceOption("lunar_spellbook", "npc_contact", "Cast")
        tick(3)
        player.interfaceOption("npc_contact", "close", "Close")
        tick(2)

        assertFalse(player.hasOpen("npc_contact"))
        assertEquals(0, player.inventory.count("astral_rune"))
        assertEquals(63.0, player.experience.get(Skill.Magic))
        assertNull(player.dialogue)
    }
}
