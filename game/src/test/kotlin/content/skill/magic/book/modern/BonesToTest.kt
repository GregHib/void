package content.skill.magic.book.modern

import WorldTest
import interfaceOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class BonesToTest : WorldTest() {

    @ParameterizedTest
    @ValueSource(strings = ["bananas", "peaches"])
    fun `Convert all bones and big bones`(type: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 60)
        player.inventory.add("nature_rune", 2)
        player.inventory.add("water_rune", 4)
        player.inventory.add("earth_rune", 4)
        player.inventory.add("bones")
        player.inventory.add("big_bones")

        player.interfaceOption("modern_spellbook", "bones_to_$type", "Cast")
        tick(1)

        assertEquals(0, player.inventory.count("bones"))
        assertEquals(0, player.inventory.count("big_bones"))
        if (type == "bananas") {
            assertEquals(2, player.inventory.count("banana"))
            assertEquals(1, player.inventory.count("nature_rune"))
            assertEquals(2, player.inventory.count("water_rune"))
            assertEquals(2, player.inventory.count("earth_rune"))
            assertEquals(25.0, player.experience.get(Skill.Magic))
        } else {
            assertEquals(2, player.inventory.count("peach"))
            assertEquals(0, player.inventory.count("nature_rune"))
            assertEquals(0, player.inventory.count("water_rune"))
            assertEquals(0, player.inventory.count("earth_rune"))
            assertEquals(35.5, player.experience.get(Skill.Magic))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["bananas", "peaches"])
    fun `Can't cast without runes`(type: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 60)
        player.inventory.add("nature_rune", 2)
        player.inventory.add("water_rune", 1)
        player.inventory.add("earth_rune", 4)
        player.inventory.add("bones")
        player.inventory.add("big_bones")

        player.interfaceOption("modern_spellbook", "bones_to_$type", "Cast")
        tick(1)

        assertEquals(1, player.inventory.count("bones"))
        assertEquals(1, player.inventory.count("big_bones"))
        assertEquals(2, player.inventory.count("nature_rune"))
        assertEquals(1, player.inventory.count("water_rune"))
        assertEquals(4, player.inventory.count("earth_rune"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
        assertEquals(0, player.inventory.count(if (type == "bananas") "banana" else "peach"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["bananas", "peaches"])
    fun `Can't cast without level`(type: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 14)
        player.inventory.add("nature_rune", 2)
        player.inventory.add("water_rune", 4)
        player.inventory.add("earth_rune", 4)
        player.inventory.add("bones")
        player.inventory.add("big_bones")

        player.interfaceOption("modern_spellbook", "bones_to_$type", "Cast")
        tick(1)

        assertEquals(1, player.inventory.count("bones"))
        assertEquals(1, player.inventory.count("big_bones"))
        assertEquals(2, player.inventory.count("nature_rune"))
        assertEquals(4, player.inventory.count("water_rune"))
        assertEquals(4, player.inventory.count("earth_rune"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
        assertEquals(0, player.inventory.count(if (type == "bananas") "banana" else "peach"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["bananas", "peaches"])
    fun `Can't cast without bones`(type: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 10)
        player.inventory.add("nature_rune", 2)
        player.inventory.add("water_rune", 4)
        player.inventory.add("earth_rune", 4)

        player.interfaceOption("modern_spellbook", "bones_to_$type", "Cast")
        tick(1)

        assertEquals(2, player.inventory.count("nature_rune"))
        assertEquals(4, player.inventory.count("water_rune"))
        assertEquals(4, player.inventory.count("earth_rune"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
        assertEquals(0, player.inventory.count(if (type == "bananas") "banana" else "peach"))
    }
}
