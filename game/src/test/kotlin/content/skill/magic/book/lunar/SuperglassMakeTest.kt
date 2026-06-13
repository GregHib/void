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

class SuperglassMakeTest : WorldTest() {

    @Test
    fun `Fuse seaweed and sand into molten glass`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 77)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("fire_rune", 6)
        player.inventory.add("air_rune", 10)
        player.inventory.add("seaweed", 3)
        player.inventory.add("bucket_of_sand", 2)

        player.interfaceOption("lunar_spellbook", "superglass_make", "Cast")
        tick(3)

        assertTrue(player.inventory.count("molten_glass") >= 2)
        assertEquals(1, player.inventory.count("seaweed"))
        assertEquals(0, player.inventory.count("bucket_of_sand"))
        assertEquals(78.0, player.experience.get(Skill.Magic))
        assertTrue(player.experience.get(Skill.Crafting) >= 20.0)
    }

    @Test
    fun `Can't cast without ingredients`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 77)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("fire_rune", 6)
        player.inventory.add("air_rune", 10)

        player.interfaceOption("lunar_spellbook", "superglass_make", "Cast")
        tick(3)

        assertTrue(player.containsMessage("You need a bucket of sand and seaweed or soda ash to cast this spell."))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
