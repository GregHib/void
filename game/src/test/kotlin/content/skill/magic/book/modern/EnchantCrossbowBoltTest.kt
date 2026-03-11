package content.skill.magic.book.modern

import WorldTest
import interfaceOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class EnchantCrossbowBoltTest : WorldTest() {

    val data = listOf(
        "opal" to 3,
        "sapphire" to 5,
        "jade" to 13,
        "pearl" to 23,
        "emerald" to 26,
        "topaz" to 28,
        "ruby" to 48,
        "diamond" to 56,
        "dragon" to 67,
        "onyx" to 86,
    )

    @TestFactory
    fun `Enchant crossbow bolts`() = data.map { (type) ->
        dynamicTest("Enchant $type bolts") {
            val player = createPlayer()
            player.levels.set(Skill.Magic, 99)
            runes(player, 2)

            player.inventory.add("${type}_bolts", 50)

            player.interfaceOption("modern_spellbook", "enchant_crossbow_bolt", "Cast")
            assertEquals("enchant_crossbow_bolts", player.menu)

            player.interfaceOption("enchant_crossbow_bolts", type, "Enchant 5 stacks  of ")
            tick(4)

            assertEquals(0, player.inventory.count("cosmic_rune"))
            assertEquals(30, player.inventory.count("${type}_bolts"))
            assertEquals(20, player.inventory.count("${type}_bolts_e"))
            assertNotEquals(0.0, player.experience.get(Skill.Magic))
        }
    }

    @TestFactory
    fun `Can't enchant without level`() = data.map { (type, level) ->
        dynamicTest("Can't enchant $type bolts without level") {
            val player = createPlayer()
            player.levels.set(Skill.Magic, level)
            runes(player, 1)

            player.inventory.add("${type}_bolts", 10)

            player.interfaceOption("modern_spellbook", "enchant_crossbow_bolt", "Cast")
            assertEquals("enchant_crossbow_bolts", player.menu)

            player.interfaceOption("enchant_crossbow_bolts", type, "Enchant 5 stacks  of ")
            tick(2)

            assertEquals(1, player.inventory.count("cosmic_rune"))
            assertEquals(10, player.inventory.count("${type}_bolts"))
            assertEquals(0, player.inventory.count("${type}_bolts_e"))
            assertEquals(0.0, player.experience.get(Skill.Magic))
        }
    }

    @TestFactory
    fun `Can't enchant without bolts`() = data.map { (type, level) ->
        dynamicTest("Can't enchant $type bolts without level") {
            val player = createPlayer()
            player.levels.set(Skill.Magic, level)
            runes(player, 1)

            player.interfaceOption("modern_spellbook", "enchant_crossbow_bolt", "Cast")
            assertEquals("enchant_crossbow_bolts", player.menu)

            player.interfaceOption("enchant_crossbow_bolts", type, "Enchant 5 stacks  of ")
            tick(2)

            assertEquals(1, player.inventory.count("cosmic_rune"))
            assertEquals(0, player.inventory.count("${type}_bolts"))
            assertEquals(0, player.inventory.count("${type}_bolts_e"))
            assertEquals(0.0, player.experience.get(Skill.Magic))
        }
    }

    @TestFactory
    fun `Can't enchant without runes`() = data.map { (type, level) ->
        dynamicTest("Can't enchant $type bolts without level") {
            val player = createPlayer()
            player.levels.set(Skill.Magic, level)

            player.inventory.add("${type}_bolts", 10)

            player.interfaceOption("modern_spellbook", "enchant_crossbow_bolt", "Cast")
            assertEquals("enchant_crossbow_bolts", player.menu)

            player.interfaceOption("enchant_crossbow_bolts", type, "Enchant 5 stacks  of ")
            tick(2)

            assertEquals(0, player.inventory.count("cosmic_rune"))
            assertEquals(10, player.inventory.count("${type}_bolts"))
            assertEquals(0, player.inventory.count("${type}_bolts_e"))
            assertEquals(0.0, player.experience.get(Skill.Magic))
        }
    }

    private fun runes(player: Player, casts: Int) {
        player.inventory.add("cosmic_rune", casts)
        player.inventory.add("air_rune", 3 * casts)
        player.inventory.add("water_rune", 2 * casts)
        player.inventory.add("mind_rune", casts)
        player.inventory.add("earth_rune", 15 * casts)
        player.inventory.add("fire_rune", 20 * casts)
        player.inventory.add("soul_rune", casts)
        player.inventory.add("death_rune", casts)
        player.inventory.add("blood_rune", casts)
        player.inventory.add("nature_rune", casts)
        player.inventory.add("law_rune", 2 * casts)
    }
}
