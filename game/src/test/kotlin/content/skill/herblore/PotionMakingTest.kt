package content.skill.herblore

import WorldTest
import itemOnItem
import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals

class PotionMakingTest : WorldTest() {

    private val herbs = listOf(
        "clean_guam",
        "clean_marrentill",
        "clean_tarromin",
        "clean_harralander",
        "clean_ranarr",
        "clean_irit",
        "clean_avantoe",
        "clean_kwuarm",
        "clean_cadantine",
        "clean_dwarf_weed",
        "clean_torstol",
        "clean_lantadyme",
        "clean_toadflax",
        "clean_snapdragon",
        "clean_wergali",
        "clean_spirit_weed",
    )

    private val potions = listOf(
        listOf("guam_potion_unf", "eye_of_newt", "attack_potion_3"),
        listOf("marrentill_potion_unf", "unicorn_horn_dust", "antipoison_3"),
        listOf("tarromin_potion_unf", "limpwurt_root", "strength_potion_3"),
        listOf("harralander_potion_unf", "red_spiders_eggs", "restore_potion_3"),
        listOf("harralander_potion_unf", "chocolate_dust", "energy_potion_3"),
        listOf("harralander_potion_unf", "desert_goat_horn", "combat_potion_3"),
        listOf("ranarr_potion_unf", "white_berries", "defence_potion_3"),
        listOf("ranarr_potion_unf", "snape_grass", "prayer_potion_3"),
        listOf("irit_potion_unf", "eye_of_newt", "super_attack_3"),
        listOf("avantoe_potion_unf", "mort_myre_fungus", "super_energy_3"),
        listOf("kwuarm_potion_unf", "limpwurt_root", "super_strength_3"),
        listOf("snapdragon_potion_unf", "red_spiders_eggs", "super_restore_3"),
        listOf("cadantine_potion_unf", "white_berries", "super_defence_3"),
        listOf("lantadyme_potion_unf", "dragon_scale_dust", "antifire_3"),
        listOf("dwarf_weed_potion_unf", "wine_of_zamorak", "super_ranging_potion_3"),
        listOf("torstol_potion_unf", "jangerberries", "zamorak_brew_3"),
        listOf("toadflax_potion_unf", "crushed_nest", "saradomin_brew_3"),
        listOf("prayer_potion_3", "wyvern_bonemeal", "super_prayer_3"),
        listOf("super_energy_3", "papaya_fruit", "recover_special_3"),
        listOf("extreme_attack_3", "extreme_strength_3", "extreme_defence_3", "extreme_magic_3", "extreme_ranging_3", "clean_torstol", "overload_3"),
    )

    @TestFactory
    fun `Create potion`() = potions.map { items ->
        dynamicTest("Create ${items.last().toSentenceCase()}") {
            val player = createPlayer()
            player.levels.set(Skill.Herblore, 99)
            for (item in items.dropLast(1)) {
                player.inventory.add(item)
            }

            player.itemOnItem(0, 1)
            tick(2)

            assertEquals(1, player.inventory.count(items.last()))
            for (item in items.dropLast(1)) {
                assertEquals(0, player.inventory.count(item))
            }
            assertNotEquals(0.0, player.experience.get(Skill.Herblore))
        }
    }

    @TestFactory
    fun `Create unfinished potion`() = herbs.map { herb ->
        dynamicTest("Create unfinished ${herb.removePrefix("clean_")}") {
            val player = createPlayer()
            player.levels.set(Skill.Herblore, 99)
            player.inventory.add(herb)
            player.inventory.add("vial_of_water")

            player.itemOnItem(0, 1)
            tick(2)

            assertEquals(1, player.inventory.count("${herb.removePrefix("clean_")}_potion_unf"))
            assertEquals(0, player.inventory.count(herb))
            assertEquals(0, player.inventory.count("vial_of_water"))
        }
    }
}