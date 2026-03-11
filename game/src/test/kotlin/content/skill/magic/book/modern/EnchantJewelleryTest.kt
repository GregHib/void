package content.skill.magic.book.modern

import WorldTest
import interfaceOnItem
import net.pearx.kasechange.toLowerSpaceCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.any
import world.gregs.voidps.engine.inv.inventory

class EnchantJewelleryTest : WorldTest() {

    private val data = listOf(
        listOf(Item("sapphire_ring"), Item("water_rune"), Item("cosmic_rune")) to "ring_of_recoil",
        listOf(Item("sapphire_necklace"), Item("water_rune"), Item("cosmic_rune")) to "games_necklace_8",
        listOf(Item("sapphire_bracelet"), Item("water_rune"), Item("cosmic_rune")) to "bracelet_of_clay",
        listOf(Item("sapphire_amulet"), Item("water_rune"), Item("cosmic_rune")) to "amulet_of_magic",
        listOf(Item("emerald_ring"), Item("air_rune", 3), Item("cosmic_rune")) to "ring_of_duelling_8",
        listOf(Item("emerald_necklace"), Item("air_rune", 3), Item("cosmic_rune")) to "binding_necklace",
        listOf(Item("emerald_bracelet"), Item("air_rune", 3), Item("cosmic_rune")) to "castle_wars_brace_3",
        listOf(Item("emerald_amulet"), Item("air_rune", 3), Item("cosmic_rune")) to "amulet_of_defence",
        listOf(Item("ruby_ring"), Item("fire_rune", 5), Item("cosmic_rune")) to "ring_of_forging",
        listOf(Item("ruby_necklace"), Item("fire_rune", 5), Item("cosmic_rune")) to "dig_site_pendant_5",
        listOf(Item("ruby_bracelet"), Item("fire_rune", 5), Item("cosmic_rune")) to "inoculation_brace",
        listOf(Item("ruby_amulet"), Item("fire_rune", 5), Item("cosmic_rune")) to "amulet_of_strength",
        listOf(Item("diamond_ring"), Item("earth_rune", 10), Item("cosmic_rune")) to "ring_of_life",
        listOf(Item("diamond_necklace"), Item("earth_rune", 10), Item("cosmic_rune")) to "phoenix_necklace",
        listOf(Item("diamond_bracelet"), Item("earth_rune", 10), Item("cosmic_rune")) to "forinthry_bracelet_5",
        listOf(Item("diamond_amulet"), Item("earth_rune", 10), Item("cosmic_rune")) to "amulet_of_power",
        listOf(Item("dragonstone_ring"), Item("water_rune", 15), Item("earth_rune", 15), Item("cosmic_rune")) to "ring_of_wealth",
        listOf(Item("dragon_necklace"), Item("water_rune", 15), Item("earth_rune", 15), Item("cosmic_rune")) to "skills_necklace",
        listOf(Item("dragonstone_bracelet"), Item("water_rune", 15), Item("earth_rune", 15), Item("cosmic_rune")) to "combat_bracelet",
        listOf(Item("dragonstone_amulet"), Item("water_rune", 15), Item("earth_rune", 15), Item("cosmic_rune")) to "amulet_of_glory",
        listOf(Item("onyx_ring"), Item("fire_rune", 20), Item("earth_rune", 20), Item("cosmic_rune")) to "ring_of_stone",
        listOf(Item("onyx_necklace"), Item("fire_rune", 20), Item("earth_rune", 20), Item("cosmic_rune")) to "berserker_necklace",
        listOf(Item("onyx_bracelet"), Item("fire_rune", 20), Item("earth_rune", 20), Item("cosmic_rune")) to "regen_bracelet",
        listOf(Item("onyx_amulet"), Item("fire_rune", 20), Item("earth_rune", 20), Item("cosmic_rune")) to "amulet_of_fury",
    )

    @TestFactory
    fun `Enchant jewellery`() = data.map { (items, expected) ->
        dynamicTest("Enchant ${items.first().id.toLowerSpaceCase()}") {
            val player = createPlayer()
            player.levels.set(Skill.Magic, 99)
            player.inventory.add(items)

            val item = items.first()
            val level = when (item.id.substringBefore("_")) {
                "sapphire" -> 1
                "emerald" -> 2
                "ruby" -> 3
                "diamond" -> 4
                "dragonstone", "dragon" -> 5
                "onyx" -> 6
                else -> return@dynamicTest
            }
            player.interfaceOnItem("modern_spellbook", "enchant_level_$level", item, 0)
            tick(1)

            assertFalse(player.inventory.any(items))
            assertTrue(player.inventory.contains(expected))
            assertNotEquals(0.0, player.experience.get(Skill.Magic))
        }
    }

    @TestFactory
    fun `Not enough runes`() = data.map { (list) ->
        val id = list.first().id
        dynamicTest("Not enough runes for $id") {
            val player = createPlayer()
            player.levels.set(Skill.Magic, 99)
            player.inventory.add(id)

            val level = when (id.substringBefore("_")) {
                "sapphire" -> 1
                "emerald" -> 2
                "ruby" -> 3
                "diamond" -> 4
                "dragonstone", "dragon" -> 5
                "onyx" -> 6
                else -> return@dynamicTest
            }
            player.interfaceOnItem("modern_spellbook", "enchant_level_$level", Item(id), 0)
            tick(1)

            assertEquals(1, player.inventory.count(id))
            assertEquals(0.0, player.experience.get(Skill.Magic))
        }
    }

    @TestFactory
    fun `Not high enough level`() = data.map { (items) ->
        dynamicTest("Not high enough level ${items.first().id}") {
            val player = createPlayer()
            player.inventory.add(items)
            val item = items.first()
            val skillLevel = when (item.id.substringBefore("_")) {
                "sapphire" -> 6
                "emerald" -> 26
                "ruby" -> 48
                "diamond" -> 56
                "dragonstone", "dragon" -> 67
                "onyx" -> 86
                else -> return@dynamicTest
            }
            player.levels.set(Skill.Magic, skillLevel)
            val level = when (item.id.substringBefore("_")) {
                "sapphire" -> 1
                "emerald" -> 2
                "ruby" -> 3
                "diamond" -> 4
                "dragonstone", "dragon" -> 5
                "onyx" -> 6
                else -> return@dynamicTest
            }
            player.interfaceOnItem("modern_spellbook", "enchant_level_$level", item, 0)
            tick(1)

            assertEquals(1, player.inventory.count(item.id))
            assertEquals(0.0, player.experience.get(Skill.Magic))
        }
    }
}
